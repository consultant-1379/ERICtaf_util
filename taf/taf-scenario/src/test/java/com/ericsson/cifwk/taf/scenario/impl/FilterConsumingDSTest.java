package com.ericsson.cifwk.taf.scenario.impl;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.google.common.base.Predicate;
import org.junit.Test;

import java.util.Iterator;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;

public class FilterConsumingDSTest {
    @Test
    public void testName() throws Exception {
        TestDataSource<DataRecord> dataSource = TestDataSourceFactory.createDataSource();
        dataSource.addRecord().setField("nodeId", "1");
        dataSource.addRecord().setField("nodeId", "2");
        dataSource.addRecord().setField("nodeId", "3");

        TestDataSource<DataRecord> sharedDs = shared(dataSource);

        TestDataSource<DataRecord> filter1 = TafDataSources.filter(sharedDs, new Predicate<DataRecord>() {
            @Override
            public boolean apply(DataRecord node) {
                return node.getFieldValue("nodeId").equals("3");
            }
        });

        TestDataSource<DataRecord> filter2 = TafDataSources.filter(sharedDs, new Predicate<DataRecord>() {
            @Override
            public boolean apply(DataRecord node) {
                return node.getFieldValue("nodeId").equals("1");
            }
        });

        System.out.println("filter1 iteration:");
        Iterator<DataRecord> iterator = filter1.iterator();
        while (iterator.hasNext()) {
            DataRecord next = iterator.next();
            System.out.println(next);
        }

        System.out.println("filter2 iteration:");
        iterator = filter2.iterator();
        while (iterator.hasNext()) {
            DataRecord next = iterator.next();
            System.out.println(next);
        }
    }
}
