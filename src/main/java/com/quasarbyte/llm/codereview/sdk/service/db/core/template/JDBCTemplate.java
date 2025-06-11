package com.quasarbyte.llm.codereview.sdk.service.db.core.template;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core JDBCTemplate interface providing lightweight database operations.
 */
public interface JDBCTemplate {

    // Query operations
    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params);

    <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... params);

    List<Map<String, Object>> queryForList(String sql, Object... params);

    Map<String, Object> queryForMap(String sql, Object... params);

    <T> T queryForObject(String sql, Class<T> requiredType, Object... params);

    // Named parameter operations
    <T> List<T> query(String sql, ParameterSource parameterSource, RowMapper<T> rowMapper);

    <T> Optional<T> queryForObject(String sql, ParameterSource parameterSource, RowMapper<T> rowMapper);

    List<Map<String, Object>> queryForList(String sql, ParameterSource parameterSource);

    Map<String, Object> queryForMap(String sql, ParameterSource parameterSource);

    // Update operations
    int update(String sql, Object... params);

    int update(String sql, ParameterSource parameterSource);

    // Insert operations with key generation
    <T> T insertAndReturnKey(String sql, Class<T> keyType, Object... params);

    <T> T insertAndReturnKey(String sql, ParameterSource parameterSource, Class<T> keyType);

    // Batch operations
    int[] batchUpdate(String sql, List<Object[]> batchArgs);

    int[] batchUpdateWithParameterSources(String sql, List<ParameterSource> batchParameterSources);

    // Execute operations
    void execute(String sql);

    <T> T execute(StatementCallback<T> action);
}
