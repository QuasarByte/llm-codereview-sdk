package com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceRuntimeException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.ParameterSource;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.RowMapper;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JDBCTemplateImplTest {

    @Mock
    private DBConnectionManager dbConnectionManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData metaData;

    private JDBCTemplateImpl jdbcTemplate;

    @Test
    void constructorShouldThrowExceptionForNullConnectionManager() {
        assertThrows(NullPointerException.class, () -> new JDBCTemplateImpl(null));
    }

    @Test
    void queryShouldReturnListOfMappedObjects() throws Exception {

        jdbcTemplate = new JDBCTemplateImpl(dbConnectionManager);
        when(dbConnectionManager.openConnection()).thenReturn(connection);

        // Arrange
        String sql = "SELECT * FROM test WHERE id = ?";
        Long id = 1L;

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("Test1", "Test2");

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        // Act
        List<String> results = jdbcTemplate.query(sql, rowMapper, id);

        // Assert
        assertEquals(2, results.size());
        assertEquals("Test1", results.get(0));
        assertEquals("Test2", results.get(1));

        verify(preparedStatement).setObject(1, id);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void queryForObjectShouldReturnOptionalEmpty() throws Exception {

        jdbcTemplate = new JDBCTemplateImpl(dbConnectionManager);
        when(dbConnectionManager.openConnection()).thenReturn(connection);

        // Arrange
        String sql = "SELECT * FROM test WHERE id = ?";
        Long id = 999L;

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        // Act
        Optional<String> result = jdbcTemplate.queryForObject(sql, rowMapper, id);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void updateShouldReturnAffectedRowCount() throws Exception {

        jdbcTemplate = new JDBCTemplateImpl(dbConnectionManager);
        when(dbConnectionManager.openConnection()).thenReturn(connection);

        // Arrange
        String sql = "UPDATE test SET name = ? WHERE id = ?";
        String name = "Updated";
        Long id = 1L;

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        int result = jdbcTemplate.update(sql, name, id);

        // Assert
        assertEquals(1, result);
        verify(preparedStatement).setObject(1, name);
        verify(preparedStatement).setObject(2, id);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void insertAndReturnKeyShouldReturnGeneratedKey() throws Exception {

        jdbcTemplate = new JDBCTemplateImpl(dbConnectionManager);
        when(dbConnectionManager.openConnection()).thenReturn(connection);

        // Arrange
        String sql = "INSERT INTO test (name) VALUES (?)";
        String name = "New Test";

        when(connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(123L);

        // Act
        Long result = jdbcTemplate.insertAndReturnKey(sql, Long.class, name);

        // Assert
        assertEquals(123L, result);
        verify(preparedStatement).setObject(1, name);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void namedParameterQueryShouldWork() throws Exception {

        jdbcTemplate = new JDBCTemplateImpl(dbConnectionManager);
        when(dbConnectionManager.openConnection()).thenReturn(connection);

        // Arrange
        String sql = "SELECT * FROM test WHERE name = :name AND active = :active";
        ParameterSource params = ParameterSources.of("name", "Test", "active", true);

        when(connection.prepareStatement("SELECT * FROM test WHERE name = ? AND active = ?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("Test");

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        // Act
        List<String> results = jdbcTemplate.query(sql, params, rowMapper);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Test", results.get(0));
        verify(preparedStatement).setObject(1, "Test");
        verify(preparedStatement).setObject(2, true);
    }

    @Test
    void shouldHandleSQLExceptionProperly() throws Exception {

        jdbcTemplate = new JDBCTemplateImpl(dbConnectionManager);
        when(dbConnectionManager.openConnection()).thenReturn(connection);

        // Arrange
        String sql = "SELECT * FROM test";
        SQLException sqlException = new SQLException("Database error");

        when(connection.prepareStatement(sql)).thenThrow(sqlException);

        // Act & Assert
        PersistenceRuntimeException exception = assertThrows(PersistenceRuntimeException.class,
                () -> jdbcTemplate.queryForList(sql, null, null));

        assertEquals("Failed to execute query: " + sql, exception.getMessage());
        assertEquals(sqlException, exception.getCause());
    }
}
