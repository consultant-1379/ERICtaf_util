package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * TestDataSourceFormatterTest
 * <p>
 * Utility class for formatting the TestDataSource<DataRecord> data structures so that it can be printed in a Tabular format
 */
public class TestDataSourceFormatterTest {

    @Test
    public void testDataSourceFormatStringTypes() {
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("Column1", "Big Sentence");
        map.put("Column2", "b");

        Map<String, Object> map1 = Maps.newLinkedHashMap();
        map1.put("Column1", "c");
        map1.put("Column2", "c");

        data.add(map);
        data.add(map1);

        TestDataSource dataSource = TestDataSourceFactory.createDataSource(data);
        String expectedString = ""
                + "********************\n"
                + "Column1      Column2 \n"
                + "Big Sentence b       \n"
                + "c            c       \n"
                + "********************\n";

        String formattedString = TestDataSourceFormatter.format(dataSource);
        assertTrue(formattedString.equals(expectedString));

    }

    @Test
    public void testDataSourceFormatDiffTypes() {
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        ArrayList<String> data2 = new ArrayList<>();
        data2.add("x");
        data2.add("y");
        data2.add("zzzzzzzzzzzzzzz");
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("Column1", "Big Sentence");
        map.put("Column2", data2);

        Map<String, Object> map1 = Maps.newLinkedHashMap();
        map1.put("Column1", "c");
        map1.put("Column2", "c");

        data.add(map);
        data.add(map1);

        TestDataSource dataSource = TestDataSourceFactory.createDataSource(data);
        String expectedString = ""
                + "************************************\n"
                + "Column1      Column2                 \n"
                + "Big Sentence [x, y, zzzzzzzzzzzzzzz] \n"
                + "c            c                       \n"
                + "************************************\n";

        String formattedString = TestDataSourceFormatter.format(dataSource);
        assertTrue(formattedString.equals(expectedString));

    }

    @Test
    public void testLengthTextWrap() {

        TestDataSourceFormatter.setMaxWidth(16);
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("Column1", "Big Sentence needs to be tested");
        map.put("Column2", "b");

        Map<String, Object> map1 = Maps.newLinkedHashMap();
        map1.put("Column1", "c");
        map1.put("Column2", "TAF stands for Test Automation Framework");

        data.add(map);
        data.add(map1);

        TestDataSource dataSource = TestDataSourceFactory.createDataSource(data);

        String expectedString = ""
                +"*********************************\n"
                +"Column1          Column2          \n"
                +"Big Sentence nee b                \n"
                +"ds to be tested  \n"
                +"c                TAF stands for T \n"
                +"                 est Automation F \n"
                +"                 ramework         \n"
                +"*********************************\n";

        String formattedString = TestDataSourceFormatter.format(dataSource);

        assertTrue(formattedString.equals(expectedString));

    }

    @Test
    public void testEmptyDataSource() {

        TestDataSource dataSource = TestDataSourceFactory.createDataSource();
        String formattedString = TestDataSourceFormatter.format(dataSource);
        assertTrue(formattedString.equals(""));

    }

    @Test
    public void testDataSourcehavingNoColumns() {

        TestDataSource dataSource = TestDataSourceFactory.createDataSource(new HashMap<String, Object>());
        String formattedString = TestDataSourceFormatter.format(dataSource);
        assertTrue(formattedString.equals(""));
    }

    @Test
    public void testDataSourcehavingNullValues() {

        Map<String, Object> data = new HashMap<>();
        data.put(null, null);
        data.put("x", null);
        TestDataSource dataSource = TestDataSourceFactory.createDataSource(data);
        String formattedString = TestDataSourceFormatter.format(dataSource);        

        String expectedString = ""
                +"*********\n"
                +"null x    \n" 
                +"null null \n" 
                +"*********\n";

        assertTrue(formattedString.equals(expectedString));
    }

}
