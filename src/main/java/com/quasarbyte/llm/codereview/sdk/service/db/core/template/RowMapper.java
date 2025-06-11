package com.quasarbyte.llm.codereview.sdk.service.db.core.template;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for mapping rows of a ResultSet to objects.
 */
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
