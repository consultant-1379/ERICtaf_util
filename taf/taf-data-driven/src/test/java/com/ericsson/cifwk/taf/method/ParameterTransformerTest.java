package com.ericsson.cifwk.taf.method;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ParameterTransformerTest {

    private enum MyENUM{
        A("hello"),B("goodbye");

        private final String value;

        MyENUM(String value){
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Test
    public void shouldTransformEnum() {
        MyENUM mine = ParameterTransformer.map("A", MyENUM.class);
        assertEquals("hello", mine.getValue());
    }

    @Test
    public void shouldDefaultForNullEnum() {
        MyENUM mine = ParameterTransformer.map(null, MyENUM.class);
        assertEquals(null, mine);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNonExistentEnumConstant(){
        MyENUM mine = ParameterTransformer.map("C", MyENUM.class);
    }

    @Test
    public void shouldReturnDefaultValueForNullPrimitive(){
        boolean result = ParameterTransformer.map(null, boolean.class);
        assertEquals(false, result);
    }

    @Test
    public void shouldReturnDefaultValueForNullBoolean(){
        boolean result = ParameterTransformer.map(null, Boolean.class);
        assertEquals(false, result);
    }

    @Test
    public void shouldReturnDefaultValueForNullString(){
        String result = ParameterTransformer.map(null, String.class);
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnValueForEmptyString(){
        String result = ParameterTransformer.map("", String.class);
        assertEquals("", result);
    }

    @Test
    public void shouldConvertStringToInteger() {
        Integer result = ParameterTransformer.map("2", Integer.class);
        assertEquals(new Integer(2), result);
    }

    @Test
    public void shouldReplaceKeyWithValueWholeParameter(){
        System.setProperty("key","value");
        String result = ParameterTransformer.map("${key}", String.class);
        assertEquals("value",result);
    }

    @Test
    public void shouldReplaceKeyWithValuePartialParameter(){
        System.setProperty("key","value");
        String result = ParameterTransformer.map("my${key}", String.class);
        assertEquals("myvalue",result);
    }

    @Test
    public void shouldNotReplaceNonExistentProperty(){
        String result = ParameterTransformer.map("my${nextKey}", String.class);
        assertEquals("my${nextKey}",result);
    }

    @Test
    public void shouldConvertStringToArray() {
        int[] result = ParameterTransformer.map("1,2,3", int[].class);
        assertTrue(Arrays.equals(result, new int[] { 1, 2, 3 }));
    }

    @Test
    public void shouldConvertStringToArrayWithKeys() {
        System.setProperty("data", "1");
        String[] result = ParameterTransformer.map("${data},2,3", String[].class);
        assertTrue(Arrays.equals(result, new String[] { "1", "2", "3" }));
    }

    @Test
    public void shouldConvertStringWithSpecialCharactersToArray() {
        String[] result = ParameterTransformer.map("123/B,*,.,(,),\\,865/A,a:b,c;d,g|h,^[1-9]\\d*\\sinstance\\(s\\).*", String[].class);
        assertTrue(Arrays.equals(result, new String[] { "123/B","*",".","(",")","\\","865/A","a:b","c;d","g|h", "^[1-9]\\d*\\sinstance\\(s\\).*" }));
    }

    @Test
    public void shouldConvertNullToObject() {
        Object result = ParameterTransformer.map(null, Object.class);
        assertThat(result, nullValue());
    }

    @Test
    public void shouldConvert2Zero_forNumber() {
        int result = ParameterTransformer.map(null, int.class);
        assertThat(result, is(0));
    }

    @Test
    public void shouldTransformEmptyMap() {
        Map result = ParameterTransformer.map(Maps.newHashMap(), Map.class);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void shouldTransformMapWithSubstitution() {
        System.setProperty("myId", "1");
        final Map<Object, Object> map = Maps.newHashMap();
        map.put("id", "${myId}");
        map.put("name", "x");
        final Map<String, String> result = ParameterTransformer.map(map, HashMap.class);
        assertThat(result.get("id"), equalTo("1"));
        assertThat(result.get("name"), equalTo("x"));
    }

    @Test
    public void shouldTransformToBean() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", "1");
        map.put("name", "x");

        BeanPropertyContainer container = new BeanPropertyContainer(map);
        CustomBean bean = ParameterTransformer.map(container, CustomBean.class);

        assertThat(bean.getId(), equalTo(1));
        assertThat(bean.getName(), equalTo("x"));
    }

    @Test
    public void clarifyColumnHeaderIsNull() throws Exception {
        Object test = new Object() {
            public void test(@Input("x") String x, @Output("y") String y) {
            }
        };
        Iterator<DataRecord> dataRecords = data("nullColumn", item(1, 2));
        List<Parameter> parameters = Parameter.parametersFor(test.getClass().getMethods()[0]);
        while (dataRecords.hasNext()) {
            DataRecord dr = dataRecords.next();
            final LinkedHashMap<String, DataRecord> dataSourcesRecords = new LinkedHashMap<>();
            dataSourcesRecords.put("dataSourceName", dr);

            Function<Parameter, Object> func = ParameterTransformer.forDataRecords(dataSourcesRecords);
            for (Parameter parameter : parameters) {
                func.apply(parameter);
            }
        }
    }

    @Test
    public void shouldTransform_Parameters2Values() throws Exception {
        Object test = new Object() {
            public void test(@Input("x") int x, @Input("y") int y,
                    @Input("z") int z) {
            }
        };
        Iterator<DataRecord> dataRecords = data("test", item(1, 2, 3),
                item(2, 3, 5), item(3, 4, 7), item(4, 5, 9));

        List<Parameter> parameters = Parameter.parametersFor(test.getClass()
                .getMethods()[0]);
        int i = 0;
        while (dataRecords.hasNext()) {
            DataRecord dr = dataRecords.next();
            i++;
            final LinkedHashMap<String, DataRecord> dataSourcesRecords = new LinkedHashMap<>();
            dataSourcesRecords.put("dataSourceName", dr);
            List<Object> transform = Lists.transform(parameters,
                    ParameterTransformer.forDataRecords(dataSourcesRecords));
            Object[] params = transform.toArray();
            assertThat((int) params[0], is(i));
            assertThat((int) params[1], is(i + 1));
            assertThat((int) params[2], is(i + i + 1));
        }
    }

    @Test
    public void shouldTransform_DataSources2Values() throws Exception {
        Object test = new Object() {
            public void test(@Input("x") int x, @Input("y") int y,
                    @Input("z") int z) {
            }
        };
        Iterator<DataRecord> dataRecords = data("1",
                                                   item(1, 2, 3),
                                                   item(2, 3, 5),
                                                   item(3, 4, 7),
                                                   item(4, 5, 9)
        );
        LinkedHashMap<String, Iterator<DataRecord>> dataSources = new LinkedHashMap<>();
        dataSources.put("dataSourceName", dataRecords);
        Iterator<Object[]> transform = ParameterTransformer.transform(
                dataSources, test.getClass().getMethods()[0]);

        int i = 1;
        while (transform.hasNext()) {
            Object[] params = transform.next();
            assertThat((int) params[0], is(i));
            assertThat((int) params[1], is(i + 1));
            assertThat((int) params[2], is(i + i + 1));
            i++;
        }
    }

    @Test
    public void shouldTransform_DataRecord2Values() throws Exception {
        Object test = new Object() {
            public void test(@Input("a") MyDataRecord myDataRecord) {
            }
        };

        Iterator<DataRecord> dataRecords = data("a",
                item(1, 2, 3),
                item(2, 3, 4),
                item(3, 4, 5),
                item(4, 5, 6)
        );

        LinkedHashMap<String, Iterator<DataRecord>> dataSources = new LinkedHashMap<>();
        dataSources.put("a",dataRecords);

        Iterator<Object[]> transform = ParameterTransformer.transform(
                dataSources, test.getClass().getMethods()[0]);
        int i = 1;


        while (transform.hasNext()) {
            Object[] params = transform.next();
            assertThat(((MyDataRecord)params[0]).getX(), is(i));
            assertThat(((MyDataRecord)params[0]).getY(), is(i+1));
            assertThat(((MyDataRecord)params[0]).getZ(), is(i+2));
            i++;
        }
    }

    @Test
    public void shouldTransform_MultipleDataSources2Values() throws Exception {
        Object test = new Object() {
            public void test(@Input("x") int x, @Input("y") int y,
                             @Input("z") int z) {
            }
        };
        Iterator<DataRecord> d1 = data("a", items("x", 1, 2, 3, 4));
        Iterator<DataRecord> d2 = data("b", items("y", 2, 3, 4, 5));
        Iterator<DataRecord> d3 = data("c", items("z", 3, 5));

        LinkedHashMap<String, Iterator<DataRecord>> dataSources = new LinkedHashMap<>();
        dataSources.put("dataSourceName1", d1);
        dataSources.put("dataSourceName2", d2);
        dataSources.put("dataSourceName3", d3);

        Iterator<Object[]> transform = ParameterTransformer.transform(
                dataSources, test.getClass().getMethods()[0]);
        int i = 1;
        while (transform.hasNext()) {
            Object[] params = transform.next();
            assertThat((int) params[0], is(i));
            assertThat((int) params[1], is(i + 1));
            assertThat((int) params[2], is(i + i + 1));
            i++;
        }
        assertThat(i, is(3));
    }

    @Test
    public void shouldUseAllowedCharactersWhenConvertingToStringArray(){
        Object test = new Object() {
            public void test(@Input("nodes.nodes") String[] nodes) {
            }
        };
        Iterator<DataRecord> d1 = data("nodes", items("nodes",
                "ieatnetsimv016-01_LTE01ERBS00001,ieatnetsimv016-01-LTE01ERBS00005," +
                        "ieatnetsimv016-01.LTE01ERBS00009,ieatnetsimv016-01 LTE01ERBS00008,ieatnetsimv016-01:LTE01ERBS00004"));

        LinkedHashMap<String, Iterator<DataRecord>> dataSources = new LinkedHashMap<>();
        dataSources.put("nodes", d1);
        Iterator<Object[]> transform = ParameterTransformer.transform(dataSources, test.getClass().getMethods()[0]);
        while (transform.hasNext()) {
            Object[] params = transform.next();
            String[] param = (String[]) params[0];
            assertThat(param.length, is(5));
            assertThat(param[0],is("ieatnetsimv016-01_LTE01ERBS00001"));
            assertThat(param[1],is("ieatnetsimv016-01-LTE01ERBS00005"));
            assertThat(param[2],is("ieatnetsimv016-01.LTE01ERBS00009"));
            assertThat(param[3],is("ieatnetsimv016-01 LTE01ERBS00008"));
            assertThat(param[4],is("ieatnetsimv016-01:LTE01ERBS00004"));
        }

    }

    @Test
    public void shouldWorkWitCustomRecords() {
        Object test = new Object() {
            public void test(@Input("data-record") CustomRecord record) {
            }
        };
        Iterator<DataRecord> dataRecords = data("data-record", item(1, 2, 3));
        List<Parameter> parameters = Parameter.parametersFor(test.getClass().getMethods()[0]);
        final LinkedHashMap<String, DataRecord> dataSourcesRecords = new LinkedHashMap<>();

        while (dataRecords.hasNext()) {
            dataSourcesRecords.put("data-record", dataRecords.next());
        }

        CustomRecord result = (CustomRecord) ParameterTransformer.forDataRecords(dataSourcesRecords).apply(parameters.get(0));

        assertThat(result, notNullValue());
        assertThat(result.getX(), equalTo("1"));
        assertThat(result.getY(), equalTo("2"));
        assertThat(result.getZ(), equalTo("3"));
    }

    private ArrayList<Map<String, Object>> items(String name, Object... values) {
        ArrayList<Map<String, Object>> list = new ArrayList<>(values.length);
        for (Object value : values) {
            HashMap<String, Object> item = new HashMap<>();
            item.put(name, value);
            list.add(item);
        }
        return list;
    }

    Iterator<DataRecord> data(final String name, Map<String, Object>... items) {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : items) {
            list.add(item);
        }
        return data(name, list);
    }

    Iterator<DataRecord> data(final String name, ArrayList<Map<String, Object>> list) {
        return Iterators.transform(list.iterator(), new Function<Map<String, Object>, DataRecord>() {
            public DataRecord apply(Map<String, Object> row) {
                return new DataRecordImpl(row);
            }
        });
    }

    private Map<String, Object> item(int x, int y, int z) {
        Map<String, Object> items = new HashMap<>();
        items.put("x", x);
        items.put("y", y);
        items.put("z", z);
        return items;
    }

    private Map<String, Object> item(int x, int z) {
        Map<String, Object> items = new HashMap<>();
        items.put("aa", null);
        items.put("bb", null);
        return items;
    }

    public static class CustomBean {
        private Integer id;
        private String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static interface CustomRecord extends DataRecord {
        String getX();
        String getY();
        String getZ();
    }

}

