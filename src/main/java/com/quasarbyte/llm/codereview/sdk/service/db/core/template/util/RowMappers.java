package com.quasarbyte.llm.codereview.sdk.service.db.core.template.util;

import com.quasarbyte.llm.codereview.sdk.service.db.core.template.RowMapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class providing common RowMapper implementations.
 */
public final class RowMappers {
    
    private RowMappers() {
        // Utility class
    }
    
    /**
     * RowMapper for String values from the first column.
     */
    public static final RowMapper<String> STRING = (rs, rowNum) -> rs.getString(1);
    
    /**
     * RowMapper for Long values from the first column.
     */
    public static final RowMapper<Long> LONG = (rs, rowNum) -> {
        long value = rs.getLong(1);
        return rs.wasNull() ? null : value;
    };
    
    /**
     * RowMapper for Integer values from the first column.
     */
    public static final RowMapper<Integer> INTEGER = (rs, rowNum) -> {
        int value = rs.getInt(1);
        return rs.wasNull() ? null : value;
    };
    
    /**
     * RowMapper for Boolean values from the first column.
     */
    public static final RowMapper<Boolean> BOOLEAN = (rs, rowNum) -> {
        boolean value = rs.getBoolean(1);
        return rs.wasNull() ? null : value;
    };
    
    /**
     * RowMapper for BigDecimal values from the first column.
     */
    public static final RowMapper<BigDecimal> BIG_DECIMAL = (rs, rowNum) -> rs.getBigDecimal(1);
    
    /**
     * RowMapper for LocalDateTime values from the first column.
     */
    public static final RowMapper<LocalDateTime> LOCAL_DATE_TIME = (rs, rowNum) -> {
        Timestamp timestamp = rs.getTimestamp(1);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    };
    
    /**
     * RowMapper that maps a row to a Map<String, Object>.
     */
    public static final RowMapper<Map<String, Object>> MAP = (rs, rowNum) -> {
        Map<String, Object> row = new HashMap<>();
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rs.getMetaData().getColumnLabel(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }
        return row;
    };
    
    /**
     * Create a RowMapper for the specified type from the first column.
     */
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> forClass(Class<T> type) {
        if (String.class.equals(type)) {
            return (RowMapper<T>) STRING;
        } else if (Long.class.equals(type)) {
            return (RowMapper<T>) LONG;
        } else if (Integer.class.equals(type)) {
            return (RowMapper<T>) INTEGER;
        } else if (Boolean.class.equals(type)) {
            return (RowMapper<T>) BOOLEAN;
        } else if (BigDecimal.class.equals(type)) {
            return (RowMapper<T>) BIG_DECIMAL;
        } else if (LocalDateTime.class.equals(type)) {
            return (RowMapper<T>) LOCAL_DATE_TIME;
        } else {
            return (rs, rowNum) -> {
                Object value = rs.getObject(1);
                return type.cast(value);
            };
        }
    }
    
    /**
     * Create a RowMapper that extracts a single column by name.
     */
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> column(String columnName, Class<T> type) {
        return (rs, rowNum) -> {
            Object value = rs.getObject(columnName);
            if (value == null) {
                return null;
            }
            if (type.isAssignableFrom(value.getClass())) {
                return (T) value;
            }
            return type.cast(value);
        };
    }
    
    /**
     * Create a RowMapper that extracts a single column by index.
     */
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> column(int columnIndex, Class<T> type) {
        return (rs, rowNum) -> {
            Object value = rs.getObject(columnIndex);
            if (value == null) {
                return null;
            }
            if (type.isAssignableFrom(value.getClass())) {
                return (T) value;
            }
            return type.cast(value);
        };
    }
}
