package com.ericsson.cifwk.taf.mvel;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Predicate;

public class DataRecordStringFilterPredicate implements Predicate<DataRecord> {
    private String filter;

    public DataRecordStringFilterPredicate(String filter) {
        this.filter = filter;
    }

    @Override
    public boolean apply(DataRecord dataRecord) {
        return DataSourceFilterProcessor.applyFilter(dataRecord, filter);
    }

}
