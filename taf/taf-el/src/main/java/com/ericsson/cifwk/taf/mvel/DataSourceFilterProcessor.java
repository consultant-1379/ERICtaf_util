package com.ericsson.cifwk.taf.mvel;

import com.ericsson.cifwk.taf.datasource.DataRecord;

import java.util.Map;

public class DataSourceFilterProcessor {
    private DataSourceFilterProcessor() {
    }

    public static boolean applyFilter(DataRecord dataRecord, String filterExpression) {
        return applyFilter(dataRecord.getAllFields(), filterExpression);
    }

    public static boolean applyFilter(Map<String, Object> allTestMethodParams, String filterExpression) {
        if (filterExpression == null || filterExpression.trim().length() == 0) {
            return true;
        }

        return TafMVELProcessor.eval(filterExpression, allTestMethodParams, Boolean.class);
    }
}
