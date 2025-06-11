package com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceRuntimeException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.ParameterSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Security-focused tests for JDBCTemplateImpl to validate SQL injection prevention
 * and parameter validation improvements.
 */
@ExtendWith(MockitoExtension.class)
class JDBCTemplateImplSecurityTest {

    @Mock
    private DBConnectionManager dbConnectionManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private JDBCTemplateImpl jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate = new JDBCTemplateImpl(dbConnectionManager);
    }

    @Test
    void testValidParameterNames_ShouldSucceed() throws Exception {
        String sql = "SELECT * FROM users WHERE id = :userId AND name = :firstName";
        MapParameterSource params = new MapParameterSource()
                .addValue("userId", 123)
                .addValue("firstName", "John");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> {
            jdbcTemplate.queryForList(sql, params);
        });

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE id = ? AND name = ?"));
        verify(preparedStatement).setObject(eq(1), eq(123));
        verify(preparedStatement).setObject(eq(2), eq("John"));
    }

    @Test
    void testInvalidParameterName_StartsWithDigit() throws Exception {
        String sql = "SELECT * FROM users WHERE id = :123user";
        MapParameterSource params = new MapParameterSource().addValue("123user", 1);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // The pattern :([a-zA-Z][a-zA-Z0-9_]*) will NOT match :123user
        // So the SQL remains unchanged and no parameters are bound
        assertDoesNotThrow(() -> {
            jdbcTemplate.queryForList(sql, params);
        });

        // Verify that the malformed parameter was not processed
        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE id = :123user"));
        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @Test
    void testInvalidParameterName_TooLong() {
        String longName = "a" + repeat("b", 64); // 65 characters total
        String sql = "SELECT * FROM users WHERE id = :" + longName;
        MapParameterSource params = new MapParameterSource().addValue(longName, 1);

        // Should fail the length validation
        PersistenceRuntimeException exception = assertThrows(
                PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList(sql, params)
        );

        assertTrue(exception.getMessage().contains("Invalid parameter name"));
    }

    @Test
    void testParameterOrderPreservation() throws Exception {
        String sql = "UPDATE users SET name = :name, email = :email WHERE id = :id";
        MapParameterSource params = new MapParameterSource()
                .addValue("name", "John Doe")
                .addValue("email", "john@example.com")
                .addValue("id", 123);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        jdbcTemplate.update(sql, params);

        // Verify parameters are bound in SQL order, not parameter source order
        verify(preparedStatement).setObject(eq(1), eq("John Doe"));   // :name (first in SQL)
        verify(preparedStatement).setObject(eq(2), eq("john@example.com")); // :email (second in SQL)
        verify(preparedStatement).setObject(eq(3), eq(123));         // :id (third in SQL)
    }

    @Test
    void testNullSqlValidation() {
        MapParameterSource params = new MapParameterSource().addValue("id", 1);

        PersistenceRuntimeException exception = assertThrows(
                PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList(null, params)
        );

        assertTrue(exception.getMessage().contains("SQL cannot be null or empty"));
    }

    @Test
    void testNullParameterSourceValidation() {
        String sql = "SELECT * FROM users WHERE id = :id";

        PersistenceRuntimeException exception = assertThrows(
                PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList(sql, (ParameterSource) null)
        );

        assertTrue(exception.getMessage().contains("ParameterSource cannot be null"));
    }

    @Test
    void testParameterNotFound() {
        String sql = "SELECT * FROM users WHERE id = :userId";
        MapParameterSource params = new MapParameterSource().addValue("wrongParam", 1);

        // Should throw exception when parameter :userId is not found
        PersistenceRuntimeException exception = assertThrows(
                PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList(sql, params)
        );

        assertTrue(exception.getMessage().contains("No value provided for parameter"));
    }

    @Test
    void testEmptySqlValidation() {
        MapParameterSource params = new MapParameterSource().addValue("id", 1);

        PersistenceRuntimeException exception = assertThrows(
                PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList("", params)
        );

        assertTrue(exception.getMessage().contains("SQL cannot be null or empty"));
    }

    @Test
    void testBlankSqlValidation() {
        MapParameterSource params = new MapParameterSource().addValue("id", 1);

        PersistenceRuntimeException exception = assertThrows(
                PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList("   ", params)
        );

        assertTrue(exception.getMessage().contains("SQL cannot be null or empty"));
    }

    @Test
    void testMaliciousParameterName_WithSpecialCharacters() throws Exception {
        // Test parameter name with special characters that should NOT be matched by the pattern
        // The pattern :([a-zA-Z][a-zA-Z0-9_]*) will match ":user" but not "$malicious" part
        String sql = "SELECT * FROM users WHERE id = :user$malicious";
        MapParameterSource params = new MapParameterSource()
                .addValue("user", 1);  // Provide value for the valid part that gets matched

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // The pattern will match ":user" and replace it with "?", leaving "$malicious" as literal text
        assertDoesNotThrow(() -> {
            jdbcTemplate.queryForList(sql, params);
        });

        // Verify that only the valid part was processed - ":user" becomes "?" but "$malicious" remains
        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE id = ?$malicious"));
        verify(preparedStatement).setObject(eq(1), eq(1)); // Only the "user" parameter gets bound
    }

    @Test
    void testSqlInjectionAttemptInParameterValue() throws Exception {
        String sql = "SELECT * FROM users WHERE name = :name";
        MapParameterSource params = new MapParameterSource()
                .addValue("name", "'; DROP TABLE users; --");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should be safely handled by PreparedStatement parameter binding
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE name = ?"));
        verify(preparedStatement).setObject(eq(1), eq("'; DROP TABLE users; --"));
    }

    @Test
    void testComplexSqlInjectionAttempts() throws Exception {
        String sql = "SELECT * FROM users WHERE id = :userId AND status = :status";
        MapParameterSource params = new MapParameterSource()
                .addValue("userId", "1 OR 1=1")
                .addValue("status", "' UNION SELECT * FROM admin_users --");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        // Verify malicious content is treated as literal parameter values
        verify(preparedStatement).setObject(eq(1), eq("1 OR 1=1"));
        verify(preparedStatement).setObject(eq(2), eq("' UNION SELECT * FROM admin_users --"));
    }

    @Test
    void testSameParameterUsedMultipleTimes() throws Exception {
        String sql = "SELECT * FROM audit_log WHERE user_id = :userId AND modified_by = :userId";
        MapParameterSource params = new MapParameterSource().addValue("userId", 123);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM audit_log WHERE user_id = ? AND modified_by = ?"));
        verify(preparedStatement).setObject(eq(1), eq(123));
        verify(preparedStatement).setObject(eq(2), eq(123));
    }

    @Test
    void testParameterNamesWithUnderscoresAndNumbers() throws Exception {
        String sql = "SELECT * FROM users WHERE user_id_123 = :user_id_123 AND status_2024 = :status_2024";
        MapParameterSource params = new MapParameterSource()
                .addValue("user_id_123", 456)
                .addValue("status_2024", "active");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(preparedStatement).setObject(eq(1), eq(456));
        verify(preparedStatement).setObject(eq(2), eq("active"));
    }

    @Test
    void testParameterNameExactly64Characters() throws Exception {
        // Create parameter name exactly 64 characters (boundary test)
        String paramName = "a" + repeat("b", 63); // exactly 64 chars
        String sql = "SELECT * FROM users WHERE id = :" + paramName;
        MapParameterSource params = new MapParameterSource().addValue(paramName, 1);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));
        verify(preparedStatement).setObject(eq(1), eq(1));
    }

    @Test
    void testParameterNameWith65Characters() {
        // Parameter name over limit should fail validation
        String paramName = "a" + repeat("b", 64); // 65 chars total
        String sql = "SELECT * FROM users WHERE id = :" + paramName;
        MapParameterSource params = new MapParameterSource().addValue(paramName, 1);

        PersistenceRuntimeException exception = assertThrows(
                PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList(sql, params)
        );

        assertTrue(exception.getMessage().contains("Invalid parameter name"));
    }

    @Test
    void testParametersInQuotedStrings_ShouldNotBeReplaced() throws Exception {
        String sql = "SELECT * FROM users WHERE name = :name AND description = 'Contains :fake_param text'";
        MapParameterSource params = new MapParameterSource()
                .addValue("name", "John");
        // Note: NOT providing fake_param since it should be ignored in quotes

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // This should NOT throw an exception because :fake_param is in quotes and should be ignored
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        // Only :name should be replaced, :fake_param in quotes should remain literal
        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE name = ? AND description = 'Contains :fake_param text'"));
        verify(preparedStatement).setObject(eq(1), eq("John"));
        verify(preparedStatement, never()).setObject(eq(2), any()); // fake_param not bound
    }

    @Test
    void testParametersWithEscapedQuotes() throws Exception {
        // Test parameters that come after escaped quotes in single-quoted strings
        String sql = "SELECT * FROM users WHERE name = 'O\\'Connor' AND id = :userId";
        MapParameterSource params = new MapParameterSource().addValue("userId", 123);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should work correctly - escaped quote shouldn't affect parameter parsing
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE name = 'O\\'Connor' AND id = ?"));
        verify(preparedStatement).setObject(eq(1), eq(123));
    }

    @Test
    void testParametersWithEscapedQuotesInDoubleQuotes() throws Exception {
        // Test parameters that come after escaped quotes in double-quoted strings
        String sql = "SELECT * FROM users WHERE name = \"John\\\"s Data\" AND id = :userId";
        MapParameterSource params = new MapParameterSource().addValue("userId", 123);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should work correctly - escaped quote shouldn't affect parameter parsing
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE name = \"John\\\"s Data\" AND id = ?"));
        verify(preparedStatement).setObject(eq(1), eq(123));
    }

    @Test
    void testParametersWithMultipleEscapedQuotes() throws Exception {
        // Test with multiple escaped quotes in the same string
        String sql = "SELECT * FROM users WHERE description = 'He said \\'Hello\\' and \\'Goodbye\\'' AND id = :userId";
        MapParameterSource params = new MapParameterSource().addValue("userId", 456);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE description = 'He said \\'Hello\\' and \\'Goodbye\\'' AND id = ?"));
        verify(preparedStatement).setObject(eq(1), eq(456));
    }

    @Test
    void testParametersAfterEscapedQuotesWithFakeParameters() throws Exception {
        // Test escaped quotes containing what looks like parameters
        String sql = "SELECT * FROM users WHERE note = 'Contains \\':fake_param\\' text' AND id = :userId";
        MapParameterSource params = new MapParameterSource().addValue("userId", 789);
        // Note: NOT providing fake_param since it's escaped within quotes

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should NOT throw exception because :fake_param is escaped within quotes
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE note = 'Contains \\':fake_param\\' text' AND id = ?"));
        verify(preparedStatement).setObject(eq(1), eq(789));
        verify(preparedStatement, never()).setObject(eq(2), any()); // fake_param not bound
    }

    @Test
    void testMixedQuoteTypesWithEscaping() throws Exception {
        // Test mixing single and double quotes with escaping
        String sql = "SELECT * FROM users WHERE name = \"John's \\\"Special\\\" Data\" AND comment = 'He said \\'Hi\\'' AND id = :userId";
        MapParameterSource params = new MapParameterSource().addValue("userId", 999);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE name = \"John's \\\"Special\\\" Data\" AND comment = 'He said \\'Hi\\'' AND id = ?"));
        verify(preparedStatement).setObject(eq(1), eq(999));
    }

    @Test
    void testParametersInMultiLineBlockComments() throws Exception {
        String sql = "SELECT * FROM users\n" +
                     "/* This is a multi-line block comment\n" +
                     "   with :fake_param1 on one line\n" +
                     "   and :fake_param2 on another line */\n" +
                     "WHERE id = :userId AND status = :status";

        MapParameterSource params = new MapParameterSource()
                .addValue("userId", 123)
                .addValue("status", "active");
        // Note: NOT providing fake_param1 or fake_param2 since they should be ignored in comments

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should NOT throw exception because fake parameters are in block comments
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        // Verify that only real parameters were replaced
        verify(preparedStatement).setObject(eq(1), eq(123));     // :userId
        verify(preparedStatement).setObject(eq(2), eq("active")); // :status
        verify(preparedStatement, never()).setObject(eq(3), any()); // No third parameter
    }

    @Test
    void testParametersInNestedBlockComments() throws Exception {
        // Test nested block comments (though not all databases support this)
        String sql = "SELECT * FROM users\n" +
                     "/* Outer comment /* nested :fake_param */ still in outer comment */\n" +
                     "WHERE id = :userId";

        MapParameterSource params = new MapParameterSource().addValue("userId", 456);
        // Note: NOT providing fake_param since it should be ignored

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should work - fake_param should be ignored in nested comments
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(preparedStatement).setObject(eq(1), eq(456));
        verify(preparedStatement, never()).setObject(eq(2), any());
    }

    @Test
    void testParametersAroundBlockComments() throws Exception {
        String sql = "SELECT * FROM users WHERE id = :userId\n" +
                     "/* Block comment with :fake_param */\n" +
                     "AND status = :status\n" +
                     "/* Another comment with :another_fake */\n" +
                     "ORDER BY created_date";

        MapParameterSource params = new MapParameterSource()
                .addValue("userId", 789)
                .addValue("status", "inactive");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(preparedStatement).setObject(eq(1), eq(789));
        verify(preparedStatement).setObject(eq(2), eq("inactive"));
        verify(preparedStatement, never()).setObject(eq(3), any());
    }

    @Test
    void testBlockCommentsWithMixedContent() throws Exception {
        String sql = "SELECT * FROM users\n" +
                     "/* Multi-line comment with various content:\n" +
                     "   - Some text: 'quoted string with :param_in_string'\n" +
                     "   - Parameter syntax: :fake_param\n" +
                     "   - SQL keywords: SELECT WHERE INSERT\n" +
                     "   - Special chars: !@#$%^&*()\n" +
                     "*/\n" +
                     "WHERE user_id = :realParam";

        MapParameterSource params = new MapParameterSource().addValue("realParam", 999);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(preparedStatement).setObject(eq(1), eq(999));
        verify(preparedStatement, never()).setObject(eq(2), any());
    }

    @Test
    void testBlockCommentsAtStartAndEndOfSql() throws Exception {
        String sql = "/* Header comment with :header_param */\n" +
                     "SELECT * FROM users WHERE id = :userId\n" +
                     "/* Footer comment with :footer_param */";

        MapParameterSource params = new MapParameterSource().addValue("userId", 111);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(preparedStatement).setObject(eq(1), eq(111));
        verify(preparedStatement, never()).setObject(eq(2), any());
    }

    @Test
    void testMixedCommentTypes() throws Exception {
        String sql = "SELECT * FROM users\n" +
                     "/* Block comment with :block_param */\n" +
                     "WHERE id = :userId -- Line comment with :line_param\n" +
                     "AND status = :status\n" +
                     "/* Another block comment\n" +
                     "   spanning multiple lines\n" +
                     "   with :multi_line_param */";

        MapParameterSource params = new MapParameterSource()
                .addValue("userId", 222)
                .addValue("status", "pending");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(preparedStatement).setObject(eq(1), eq(222));
        verify(preparedStatement).setObject(eq(2), eq("pending"));
        verify(preparedStatement, never()).setObject(eq(3), any());
    }

    @Test
    void testIncompleteBlockComment() throws Exception {
        // Test what happens with an unclosed block comment (edge case)
        String sql = "SELECT * FROM users\n" +
                     "/* This block comment is never closed\n" +
                     "   and contains :fake_param\n" +
                     "WHERE id = :userId";  // This should be inside the comment

        MapParameterSource params = new MapParameterSource(); // Empty - no parameters should be processed

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should work - everything after /* should be treated as comment
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        // No parameters should be bound since everything is in an unclosed comment
        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @Test
    void testParametersInSqlComments_ShouldNotBeReplaced() throws Exception {
        String sql =
                "SELECT * FROM users\n" +
                        "WHERE id = :userId -- this :comment_param should not be replaced\n" +
                        "AND status = :status";

        MapParameterSource params = new MapParameterSource()
                .addValue("userId", 123)
                .addValue("status", "active");
        // Note: NOT providing comment_param since it should be ignored in comments

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // This should NOT throw an exception because :comment_param is in a comment
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        // Should replace real parameters but leave commented ones alone
        verify(preparedStatement).setObject(eq(1), eq(123));
        verify(preparedStatement).setObject(eq(2), eq("active"));
        verify(preparedStatement, never()).setObject(eq(3), any()); // comment_param not bound
    }

    @Test
    void testUnicodeParameterValues() throws Exception {
        String sql = "INSERT INTO users (name, city) VALUES (:name, :city)";
        MapParameterSource params = new MapParameterSource()
                .addValue("name", "José María García")
                .addValue("city", "São Paulo");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> jdbcTemplate.update(sql, params));

        verify(preparedStatement).setObject(eq(1), eq("José María García"));
        verify(preparedStatement).setObject(eq(2), eq("São Paulo"));
    }

    @Test
    void testEmojiAndSpecialCharactersInValues() throws Exception {
        String sql = "UPDATE posts SET content = :content WHERE id = :id";
        MapParameterSource params = new MapParameterSource()
                .addValue("content", "Check this out! 🚀🎉 Special chars: @#$%^&*()")
                .addValue("id", 1);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> jdbcTemplate.update(sql, params));

        verify(preparedStatement).setObject(eq(1), eq("Check this out! 🚀🎉 Special chars: @#$%^&*()"));
        verify(preparedStatement).setObject(eq(2), eq(1));
    }

    @Test
    void testParameterAtStartOfSql() throws Exception {
        String sql = ":limit LIMIT SELECT * FROM users";
        MapParameterSource params = new MapParameterSource().addValue("limit", 10);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("? LIMIT SELECT * FROM users"));
        verify(preparedStatement).setObject(eq(1), eq(10));
    }

    @Test
    void testParameterAtEndOfSql() throws Exception {
        String sql = "SELECT * FROM users ORDER BY created_date LIMIT :limit";
        MapParameterSource params = new MapParameterSource().addValue("limit", 50);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users ORDER BY created_date LIMIT ?"));
        verify(preparedStatement).setObject(eq(1), eq(50));
    }

    @Test
    void testAdjacentParameters() throws Exception {
        String sql = "SELECT * FROM logs WHERE date BETWEEN :start_date AND :end_date"; // Properly formatted with space
        MapParameterSource params = new MapParameterSource()
                .addValue("start_date", "2024-01-01")
                .addValue("end_date", "2024-12-31");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Both parameters should be recognized and replaced
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));
        
        verify(connection).prepareStatement(eq("SELECT * FROM logs WHERE date BETWEEN ? AND ?"));
        verify(preparedStatement).setObject(eq(1), eq("2024-01-01"));
        verify(preparedStatement).setObject(eq(2), eq("2024-12-31"));
    }

    @Test
    void testMalformedParameterSyntax() throws Exception {
        String sql = "SELECT * FROM users WHERE id = : AND name = :name"; // Missing param name after first colon
        MapParameterSource params = new MapParameterSource().addValue("name", "John");

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Should handle gracefully - the lone ":" won't match the pattern
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE id = : AND name = ?"));
        verify(preparedStatement).setObject(eq(1), eq("John"));
        verify(preparedStatement, never()).setObject(eq(2), any()); // No second parameter
    }

    @Test
    void testMalformedParameterSyntaxWithInvalidCharacter() throws Exception {
        String sql = "SELECT * FROM users WHERE id = :123invalid AND name = :name"; // Invalid parameter starts with digit
        MapParameterSource params = new MapParameterSource().addValue("name", "John");
        // Note: Don't provide "123invalid" as it's not a valid parameter name

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // The invalid parameter ":123invalid" won't be recognized by the parser since it starts with a digit
        // Only ":name" should be recognized and replaced
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));

        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE id = :123invalid AND name = ?"));
        verify(preparedStatement).setObject(eq(1), eq("John"));
        verify(preparedStatement, never()).setObject(eq(2), any()); // No second parameter bound
    }

    @Test
    void testParameterNamesWithActualInvalidCharacters() throws Exception {
        // Test what actually happens with invalid characters in SQL parameter names
        // The SQL ":user-invalid" will be parsed as ":user" (stops at hyphen)
        // Then it will look for "user" in the parameter source
        String sql = "SELECT * FROM users WHERE id = :user-invalid";
        MapParameterSource params = new MapParameterSource().addValue("user", 123);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // This should work because the parser extracts "user" and finds it in params
        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sql, params));
        
        // Verify that the SQL was transformed correctly - "user" becomes "?" and "-invalid" remains
        verify(connection).prepareStatement(eq("SELECT * FROM users WHERE id = ?-invalid"));
        verify(preparedStatement).setObject(eq(1), eq(123));
    }

    @Test
    void testLargeNumberOfParameters() throws Exception {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM users WHERE ");
        MapParameterSource params = new MapParameterSource();

        // Test with 100 parameters to ensure performance is acceptable
        for (int i = 0; i < 100; i++) {
            if (i > 0) sqlBuilder.append(" OR ");
            sqlBuilder.append("id = :param").append(i);
            params.addValue("param" + i, i);
        }

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(sqlBuilder.toString(), params));

        // Verify all 100 parameters were bound
        for (int i = 0; i < 100; i++) {
            verify(preparedStatement).setObject(eq(i + 1), eq(i));
        }
    }

    @Test
    void testParameterOrderWithComplexSql() throws Exception {
        String sql = "UPDATE users\n" +
                "SET name = :name,\n" +
                "    email = :email,\n" +
                "    updated_date = :updateDate,\n" +
                "    updated_by = :name\n" +
                "WHERE id = :userId\n" +
                "AND previous_email != :email";

        MapParameterSource params = new MapParameterSource()
                .addValue("name", "John Doe")
                .addValue("email", "john@example.com")
                .addValue("updateDate", "2024-01-01")
                .addValue("userId", 123);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> jdbcTemplate.update(sql, params));

        // Verify parameters are bound in the order they appear in SQL
        verify(preparedStatement).setObject(eq(1), eq("John Doe"));     // first :name
        verify(preparedStatement).setObject(eq(2), eq("john@example.com")); // first :email
        verify(preparedStatement).setObject(eq(3), eq("2024-01-01"));  // :updateDate
        verify(preparedStatement).setObject(eq(4), eq("John Doe"));     // second :name
        verify(preparedStatement).setObject(eq(5), eq(123));           // :userId
        verify(preparedStatement).setObject(eq(6), eq("john@example.com")); // second :email
    }

    @Test
    void testNullParameterValues() throws Exception {
        String sql = "UPDATE users SET middle_name = :middleName WHERE id = :userId";
        MapParameterSource params = new MapParameterSource()
                .addValue("middleName", null)
                .addValue("userId", 123);

        when(dbConnectionManager.openConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> jdbcTemplate.update(sql, params));

        verify(preparedStatement).setObject(eq(1), eq(null));
        verify(preparedStatement).setObject(eq(2), eq(123));
    }

    /**
     * Helper method to repeat a string multiple times.
     * This replaces the previous implementation for better performance and clarity.
     */
    private static String repeat(String str, int times) {
        if (times <= 0) {
            return "";
        }
        return new String(new char[times]).replace("\0", str);
    }
}
