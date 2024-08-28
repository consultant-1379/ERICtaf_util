/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.data.DataHandler;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CustomDelimiterTest {
    
    private ConfigurationSource reader  = mock(PropertiesReader.class);
    private CsvDataSource dataSource;
    
    @After
    public void tearDown() throws IOException{
        DataHandler.unsetAttribute("taf.csv.delimiter");
        dataSource.close();
    }
    
    @Test
    public void shouldReadCsvWithDelimiterPropertySpecified() {
        DataHandler.setAttribute("taf.csv.delimiter", "%");
        dataSource = new CsvDataSource();
        when(reader.getProperty("location")).thenReturn("custom_delimiter.csv");
        dataSource.init(reader);
        Iterator<DataRecord> iterator = dataSource.iterator();
        int[][] expectation = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        for (int[] expected : expectation) {
            assertTrue(iterator.hasNext());
            DataRecord data = iterator.next();
            assertThat((String) data.getFieldValue("x"), is(Integer.toString(expected[0])));
            assertThat((String) data.getFieldValue("y"), is(Integer.toString(expected[1])));
            assertThat((String) data.getFieldValue("z"), is(Integer.toString(expected[2])));
        }
        assertThat(iterator.hasNext(), is(false));
        
    }
    
    @Test
    public void shouldReadCsvWithDataProviderDelimiterPropertySpecified(){
        dataSource = new CsvDataSource();
        when(reader.getProperty("location")).thenReturn("custom_data_provider_delimiter.csv");
        when(reader.getProperty("delimiter")).thenReturn("|");
        dataSource.init(reader);
        Iterator<DataRecord> iterator = dataSource.iterator();
        int[][] expectation = {{1,2,3},{4,5,6},{7,8,9}};
        for(int[] expected :expectation){
            assertTrue(iterator.hasNext());
            DataRecord data = iterator.next();
            assertThat((String) data.getFieldValue("x"), is(Integer.toString(expected[0])));
            assertThat((String) data.getFieldValue("y"), is(Integer.toString(expected[1])));
            assertThat((String) data.getFieldValue("z"), is(Integer.toString(expected[2])));
        }
    }

}
