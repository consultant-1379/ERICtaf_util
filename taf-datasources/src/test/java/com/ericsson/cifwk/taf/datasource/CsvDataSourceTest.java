package com.ericsson.cifwk.taf.datasource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import sun.net.www.protocol.test.TestConnection;

public class CsvDataSourceTest {

    private CsvDataSource dataSource;
    private ConfigurationSource reader;

    @Before
    public void setUp() {
        dataSource = new CsvDataSource();
        reader = mock(PropertiesReader.class);
        when(reader.getProperty("delimeter")).thenReturn(",");
    }

    @Test
    public void shouldReadFromCsv() {
        when(reader.getProperty("location")).thenReturn("calculator.csv");
        dataSource.init(reader);
        Iterator<DataRecord> iterator = dataSource.iterator();
        int[][] expectation = { { 1, 2, 3 }, { 2, 2, 4 }, { 3, 3, 6 } };
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
    public void shouldReadUsingUri() {
        when(reader.getProperty("uri")).thenReturn("test://location.csv");
        TestConnection.setValue("x,y,z\n1,2,3\n-1,-2,-3\n");
        dataSource.init(reader);

        Iterator<DataRecord> iterator = dataSource.iterator();
        int[][] expectation = { { 1, 2, 3 }, { -1, -2, -3 } };
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
    public void shouldReadUsingLocation() {
        when(reader.getProperty("location")).thenReturn("test://test.com/location.csv");
        TestConnection.setValue("x,y,z\n1,2,3\n-1,-2,-3\n");
        dataSource.init(reader);

        Iterator<DataRecord> iterator = dataSource.iterator();
        int[][] expectation = { { 1, 2, 3 }, { -1, -2, -3 } };
        for (int[] expected : expectation) {
            assertTrue(iterator.hasNext());
            DataRecord data = iterator.next();
            assertThat((String) data.getFieldValue("x"), is(Integer.toString(expected[0])));
            assertThat((String) data.getFieldValue("y"), is(Integer.toString(expected[1])));
            assertThat((String) data.getFieldValue("z"), is(Integer.toString(expected[2])));
        }
        assertThat(iterator.hasNext(), is(false));
    }
}
