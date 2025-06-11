package com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceRuntimeException;
import com.quasarbyte.llm.codereview.sdk.exception.db.TooManyRowsException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.ParameterSource;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.RowMapper;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.StatementCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core JDBCTemplate implementation providing lightweight database operations.
 */
public class JDBCTemplateImpl implements JDBCTemplate {

    private static final Logger logger = LoggerFactory.getLogger(JDBCTemplateImpl.class);
    // Secure pattern that only matches valid parameter names (alphanumeric and underscore, must start with a letter)
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile(":([a-zA-Z][a-zA-Z0-9_]*)");

    private final DBConnectionManager dbConnectionManager;

    public JDBCTemplateImpl(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = Objects.requireNonNull(dbConnectionManager, "dbConnectionManager must not be null");
    }

    // Query operations with positional parameters
    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        logger.debug("Executing query: {} with {} parameters", sql, params.length);

        try (Connection conn = dbConnectionManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                logger.debug("Query returned {} rows", results.size());
                return results;
            }
        } catch (Exception e) {
            throw new PersistenceRuntimeException("Failed to execute query: " + sql, e);
        }
    }

    @Override
    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new TooManyRowsException("Query returned " + results.size() + " rows, expected 0 or 1");
        }
        return Optional.of(results.get(0));
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object... params) {
        return query(sql, this::mapRowToMap, params);
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... params) {
        Optional<Map<String, Object>> result = queryForObject(sql, this::mapRowToMap, params);
        return result.orElseThrow(() -> new PersistenceRuntimeException("Query returned no rows"));
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... params) {
        return queryForObject(sql, (rs, rowNum) -> getColumnValue(rs, 1, requiredType), params)
                .orElseThrow(() -> new PersistenceRuntimeException("Query returned no rows"));
    }

    // Named parameter operations
    @Override
    public <T> List<T> query(String sql, ParameterSource parameterSource, RowMapper<T> rowMapper) {
        ParsedSql parsedSql = parseNamedParameterSql(sql, parameterSource);
        return query(parsedSql.sql, rowMapper, parsedSql.parameters);
    }

    @Override
    public <T> Optional<T> queryForObject(String sql, ParameterSource parameterSource, RowMapper<T> rowMapper) {
        ParsedSql parsedSql = parseNamedParameterSql(sql, parameterSource);
        return queryForObject(parsedSql.sql, rowMapper, parsedSql.parameters);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, ParameterSource parameterSource) {
        ParsedSql parsedSql = parseNamedParameterSql(sql, parameterSource);
        return queryForList(parsedSql.sql, parsedSql.parameters);
    }

    @Override
    public Map<String, Object> queryForMap(String sql, ParameterSource parameterSource) {
        ParsedSql parsedSql = parseNamedParameterSql(sql, parameterSource);
        return queryForMap(parsedSql.sql, parsedSql.parameters);
    }

    // Update operations
    @Override
    public int update(String sql, Object... params) {
        logger.debug("Executing update: {} with {} parameters", sql, params.length);

        try (Connection conn = dbConnectionManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            int rowsAffected = stmt.executeUpdate();
            logger.debug("Update affected {} rows", rowsAffected);
            return rowsAffected;
        } catch (Exception e) {
            throw new PersistenceRuntimeException("Failed to execute update: " + sql, e);
        }
    }

    @Override
    public int update(String sql, ParameterSource parameterSource) {
        ParsedSql parsedSql = parseNamedParameterSql(sql, parameterSource);
        return update(parsedSql.sql, parsedSql.parameters);
    }

    // Insert operations with key generation
    @Override
    public <T> T insertAndReturnKey(String sql, Class<T> keyType, Object... params) {
        logger.debug("Executing insert with key generation: {} with {} parameters", sql, params.length);

        try (Connection conn = dbConnectionManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(stmt, params);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new PersistenceRuntimeException("Insert failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    T key = getColumnValue(generatedKeys, 1, keyType);
                    logger.debug("Insert generated key: {}", key);
                    return key;
                } else {
                    throw new PersistenceRuntimeException("Insert failed to generate key");
                }
            }
        } catch (Exception e) {
            throw new PersistenceRuntimeException("Failed to execute insert: " + sql, e);
        }
    }

    @Override
    public <T> T insertAndReturnKey(String sql, ParameterSource parameterSource, Class<T> keyType) {
        ParsedSql parsedSql = parseNamedParameterSql(sql, parameterSource);
        return insertAndReturnKey(parsedSql.sql, keyType, parsedSql.parameters);
    }

    // Batch operations
    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        logger.debug("Executing batch update: {} with {} batches", sql, batchArgs.size());

        try (Connection conn = dbConnectionManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Object[] args : batchArgs) {
                setParameters(stmt, args);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            logger.debug("Batch update completed with {} results", results.length);
            return results;
        } catch (Exception e) {
            throw new PersistenceRuntimeException("Failed to execute batch update: " + sql, e);
        }
    }

    @Override
    public int[] batchUpdateWithParameterSources(String sql, List<ParameterSource> batchParameterSources) {
        List<Object[]> batchArgs = new ArrayList<>();
        ParsedSql firstParsed = null;

        for (ParameterSource parameterSource : batchParameterSources) {
            ParsedSql parsedSql = parseNamedParameterSql(sql, parameterSource);
            if (firstParsed == null) {
                firstParsed = parsedSql;
            }
            batchArgs.add(parsedSql.parameters);
        }

        return batchUpdate(firstParsed != null ? firstParsed.sql : sql, batchArgs);
    }

    // Execute operations
    @Override
    public void execute(String sql) {
        logger.debug("Executing SQL: {}", sql);

        try (Connection conn = dbConnectionManager.openConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            logger.debug("SQL execution completed");
        } catch (Exception e) {
            throw new PersistenceRuntimeException("Failed to execute SQL: " + sql, e);
        }
    }

    @Override
    public <T> T execute(StatementCallback<T> action) {
        logger.debug("Executing statement callback");

        try (Connection conn = dbConnectionManager.openConnection();
             Statement stmt = conn.createStatement()) {

            T result = action.doInStatement(stmt);
            logger.debug("Statement callback completed");
            return result;
        } catch (Exception e) {
            throw new PersistenceRuntimeException("Failed to execute statement callback", e);
        }
    }

    /**
     * Safely sets parameters on PreparedStatement with type validation and null handling.
     * Prevents SQL injection by using proper parameter binding.
     */
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int paramIndex = i + 1;
            
            if (param == null) {
                // Explicitly set NULL with proper SQL type
                stmt.setObject(paramIndex, null);
            } else {
                // Use setObject for type-safe parameter binding
                // This ensures proper escaping and type conversion
                stmt.setObject(paramIndex, param);
            }
        }
        
        logger.trace("Set {} parameters on PreparedStatement", params.length);
    }

    private Map<String, Object> mapRowToMap(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> row = new HashMap<>();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }

        return row;
    }

    @SuppressWarnings("unchecked")
    private <T> T getColumnValue(ResultSet rs, int columnIndex, Class<T> requiredType) throws SQLException {
        Object value = rs.getObject(columnIndex);

        if (value == null) {
            return null;
        }

        if (requiredType.isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        // Handle basic type conversions
        if (requiredType == String.class) {
            return (T) value.toString();
        } else if (requiredType == Long.class && value instanceof Number) {
            return (T) Long.valueOf(((Number) value).longValue());
        } else if (requiredType == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        } else if (requiredType == Boolean.class) {
            if (value instanceof Boolean) {
                return (T) value;
            } else if (value instanceof Number) {
                return (T) Boolean.valueOf(((Number) value).intValue() != 0);
            } else {
                return (T) Boolean.valueOf(value.toString());
            }
        }

        throw new PersistenceRuntimeException("Cannot convert value of type " + value.getClass().getName() +
                " to required type " + requiredType.getName());
    }

    /**
     * Securely parses SQL with named parameters and converts to a prepared statement with positional parameters.
     * This method prevents SQL injection by:
     * 1. Validating parameter names against a strict pattern
     * 2. Using SQL-aware parsing to avoid false matches in comments and strings
     * 3. Using ordered replacement to maintain parameter-to-placeholder mapping
     * 4. Ensuring all parameters are bound through PreparedStatement
     */
    private ParsedSql parseNamedParameterSql(String sql, ParameterSource parameterSource) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new PersistenceRuntimeException("SQL cannot be null or empty");
        }
        
        if (parameterSource == null) {
            throw new PersistenceRuntimeException("ParameterSource cannot be null");
        }

        StringBuilder parsedSqlBuilder = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        int i = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        
        while (i < sql.length()) {
            char c = sql.charAt(i);
            char next = (i + 1 < sql.length()) ? sql.charAt(i + 1) : '\0';
            
            // Handle state transitions
            if (!inSingleQuote && !inDoubleQuote && !inLineComment && !inBlockComment) {
                // Not inside any string or comment - check for SQL constructs and parameters
                if (c == '\'' && !isEscaped(sql, i)) {
                    inSingleQuote = true;
                    parsedSqlBuilder.append(c);
                } else if (c == '"' && !isEscaped(sql, i)) {
                    inDoubleQuote = true;
                    parsedSqlBuilder.append(c);
                } else if (c == '-' && next == '-') {
                    inLineComment = true;
                    parsedSqlBuilder.append(c);
                } else if (c == '/' && next == '*') {
                    inBlockComment = true;
                    parsedSqlBuilder.append(c);
                } else if (c == ':' && Character.isLetter(next)) {
                    // Found a potential parameter - parse it
                    ParameterParseResult result = parseParameterAtPosition(sql, i, parameterSource);
                    if (result.parameterName != null) {
                        // Valid parameter found
                        parsedSqlBuilder.append("?");
                        parameters.add(result.parameterValue);
                        i = result.nextIndex - 1; // -1 because loop will increment
                    } else {
                        // Not a valid parameter, keep the colon
                        parsedSqlBuilder.append(c);
                    }
                } else {
                    parsedSqlBuilder.append(c);
                }
            } else {
                // Inside string or comment - just copy character and update state
                parsedSqlBuilder.append(c);
                
                if (inSingleQuote && c == '\'' && !isEscaped(sql, i)) {
                    inSingleQuote = false;
                } else if (inDoubleQuote && c == '"' && !isEscaped(sql, i)) {
                    inDoubleQuote = false;
                } else if (inLineComment && (c == '\n' || c == '\r')) {
                    inLineComment = false;
                } else if (inBlockComment && c == '*' && next == '/') {
                    inBlockComment = false;
                    // Skip the '/' as well
                    parsedSqlBuilder.append('/');
                    i++;
                }
            }
            
            i++;
        }
        
        String finalSql = parsedSqlBuilder.toString();
        
        // Log parameter count for debugging (without exposing values)
        logger.debug("Parsed SQL with {} named parameters converted to {} positional parameters", 
                     countValidParameters(sql), parameters.size());
        
        return new ParsedSql(finalSql, parameters.toArray());
    }
    
    /**
     * Validates parameter names to ensure they follow secure naming conventions.
     * Only allows alphanumeric characters and underscores, must start with a letter.
     */
    private boolean isValidParameterName(String paramName) {
        if (paramName == null || paramName.isEmpty()) {
            return false;
        }
        
        // Check length limit to prevent extremely long parameter names
        if (paramName.length() > 64) {
            return false;
        }
        
        // Must start with a letter, followed by letters, digits, or underscores
        return paramName.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }
    
    /**
     * Checks if a character at the given position is escaped (preceded by an odd number of backslashes).
     */
    private boolean isEscaped(String sql, int position) {
        if (position == 0) {
            return false;
        }
        
        int backslashCount = 0;
        int i = position - 1;
        
        while (i >= 0 && sql.charAt(i) == '\\') {
            backslashCount++;
            i--;
        }
        
        // Character is escaped if preceded by an odd number of backslashes
        return backslashCount % 2 == 1;
    }
    
    /**
     * Parses a parameter starting at the given position.
     * Returns information about the parameter or null if it's not valid.
     */
    private ParameterParseResult parseParameterAtPosition(String sql, int startPos, ParameterSource parameterSource) {
        if (startPos >= sql.length() || sql.charAt(startPos) != ':') {
            return new ParameterParseResult(null, null, startPos + 1);
        }
        
        int nameStart = startPos + 1;
        int nameEnd = nameStart;
        
        // Find the end of the parameter name using the same pattern as the regex
        while (nameEnd < sql.length()) {
            char c = sql.charAt(nameEnd);
            if (Character.isLetterOrDigit(c) || c == '_') {
                nameEnd++;
            } else {
                break;
            }
        }
        
        if (nameEnd == nameStart) {
            // No parameter name found
            return new ParameterParseResult(null, null, startPos + 1);
        }
        
        String paramName = sql.substring(nameStart, nameEnd);
        
        // Validate parameter name
        if (!isValidParameterName(paramName)) {
            throw new PersistenceRuntimeException("Invalid parameter name: " + paramName);
        }
        
        // Check if parameter exists
        if (!parameterSource.hasValue(paramName)) {
            throw new PersistenceRuntimeException("No value provided for parameter: " + paramName);
        }
        
        Object paramValue = parameterSource.getValue(paramName);
        
        return new ParameterParseResult(paramName, paramValue, nameEnd);
    }
    
    /**
     * Counts valid named parameters in SQL for validation and logging purposes.
     * Uses the same SQL-aware parsing logic to avoid counting parameters in comments/strings.
     */
    private int countValidParameters(String sql) {
        int count = 0;
        int i = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        
        while (i < sql.length()) {
            char c = sql.charAt(i);
            char next = (i + 1 < sql.length()) ? sql.charAt(i + 1) : '\0';
            
            if (!inSingleQuote && !inDoubleQuote && !inLineComment && !inBlockComment) {
                if (c == '\'' && !isEscaped(sql, i)) {
                    inSingleQuote = true;
                } else if (c == '"' && !isEscaped(sql, i)) {
                    inDoubleQuote = true;
                } else if (c == '-' && next == '-') {
                    inLineComment = true;
                } else if (c == '/' && next == '*') {
                    inBlockComment = true;
                } else if (c == ':' && Character.isLetter(next)) {
                    // Found a potential parameter - validate it
                    Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(sql.substring(i));
                    if (matcher.lookingAt()) {
                        String paramName = matcher.group(1);
                        if (isValidParameterName(paramName)) {
                            count++;
                        }
                        i += matcher.end() - 1; // Skip the parameter name
                    }
                }
            } else {
                if (inSingleQuote && c == '\'' && !isEscaped(sql, i)) {
                    inSingleQuote = false;
                } else if (inDoubleQuote && c == '"' && !isEscaped(sql, i)) {
                    inDoubleQuote = false;
                } else if (inLineComment && (c == '\n' || c == '\r')) {
                    inLineComment = false;
                } else if (inBlockComment && c == '*' && next == '/') {
                    inBlockComment = false;
                    i++; // Skip the '/'
                }
            }
            
            i++;
        }
        
        return count;
    }

    /**
     * Result of parameter parsing operation.
     */
    private static class ParameterParseResult {
        final String parameterName;
        final Object parameterValue;
        final int nextIndex;
        
        ParameterParseResult(String parameterName, Object parameterValue, int nextIndex) {
            this.parameterName = parameterName;
            this.parameterValue = parameterValue;
            this.nextIndex = nextIndex;
        }
    }

    private static class ParsedSql {
        final String sql;
        final Object[] parameters;

        ParsedSql(String sql, Object[] parameters) {
            this.sql = sql;
            this.parameters = parameters;
        }
    }
}
