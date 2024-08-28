package com.ericsson.cifwk.taf.datasource;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class CsvDataSourceITest {

	@Before
	public void setUp() {
		System.clearProperty(CsvDataSource.DELIMITER_ATTRIBUTE);
	}

	@Test
	public void testDefaultDelimiter() {
        CsvDataSource dataSource = initDataSource("itest_default_delimiter");

        for (DataRecord data : dataSource) {
            assertThat(data.getFieldValue("num1").toString(), is("1"));
            assertThat(data.getFieldValue("num2").toString(), is("2"));
            assertThat(data.getFieldValue("num3").toString(), is("3"));

            assertThat(data.getFieldValue("text1").toString(), is("a,B"));
            assertThat(data.getFieldValue("text2").toString(), is("c"));
            assertThat(data.getFieldValue("text3").toString(), is("d"));
        }
	}

    @Test
	public void testCustomDelimiter() {
		System.setProperty("taf.csv.delimiter", ":");

        CsvDataSource dataSource = initDataSource("custom_delimiter_itest");

        for (DataRecord data : dataSource) {
            assertThat(data.getFieldValue("num1").toString(), is("1"));
            assertThat(data.getFieldValue("num2").toString(), is("2"));
            assertThat(data.getFieldValue("num3").toString(), is("3"));

            assertThat(data.getFieldValue("text1").toString(), is("a:B"));
            assertThat(data.getFieldValue("text2").toString(), is("c"));
            assertThat(data.getFieldValue("text3").toString(), is("d"));
            assertThat(data.getFieldValue("text4").toString(), is("a,B"));
        }
	}

    @Test
    public void shouldProvideIndependentIterators() {
        CsvDataSource dataSource = initDataSource("simple");

        Iterator<DataRecord> iterator1 = dataSource.iterator();
        Iterator<DataRecord> iterator2 = dataSource.iterator();

        assertThat(iterator1.next(), notNullValue());
        assertThat(iterator1.next(), notNullValue());
        assertThat(iterator1.hasNext(), is(false));

        assertThat(iterator2.next(), notNullValue());
        assertThat(iterator2.next(), notNullValue());
        assertThat(iterator2.hasNext(), is(false));
    }

    private CsvDataSource initDataSource(String name) {
        CsvDataSource dataSource = new CsvDataSource();
        ConfigurationSource reader = new PropertiesReader(name);
        dataSource.init(reader);
        return dataSource;
    }

}