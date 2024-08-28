package com.ericsson.cifwk.taf.scenario.impl.datasource;


import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import java.util.Iterator;


public class FilteredDataSourceInfo {

    public static int getFilteredDataSourceSize(String dataProviderName,String columnName,String data) {

        int count=0;
        TestDataSource<DataRecord> fromDataProviderNew=TafDataSources.fromTafDataProviderForSize(dataProviderName);
        Iterator itr = fromDataProviderNew.iterator();
        while(itr.hasNext()){
            DataRecord dataRecord = (DataRecord)itr.next();
            String buffer = (String)dataRecord.getFieldValue(columnName);
            if (buffer != null && buffer.contains(data)){
                count++;
            }
        }
        return count;
    }

}
