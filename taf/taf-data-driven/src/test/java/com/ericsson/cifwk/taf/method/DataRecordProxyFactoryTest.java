package com.ericsson.cifwk.taf.method;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.ericsson.cifwk.taf.method.DataRecordProxyFactory.createProxy;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DataRecordProxyFactoryTest {

    private DataRecord dataRecord;
    private DataRecord partialDataRecord;
    private DataRecord propertyDataRecord;

    @Before
    public void setUp() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", "-123");
        data.put("name", "example");
        data.put("stuff", "1,2,3");
        data.put("camelCase", "ok");
        data.put("bool", true);
        dataRecord = new DataRecordImpl(data);

        Map<String, Object> partialData = new HashMap<>();
        partialData.put("id", "-123");
        partialData.put("name", "example");
        partialDataRecord = new DataRecordImpl(partialData);

        Map<String, Object> propertyData = new HashMap<>();
        propertyData.put("id", "${data}");
        propertyData.put("name", "1");
        propertyDataRecord = new DataRecordImpl(propertyData);
    }

    @Test
    public void shouldCreateProxy() throws Exception {
        CustomDataRecord proxy = DataRecordProxyFactory.createProxy(dataRecord, CustomDataRecord.class);

        assertThat(proxy.getId(), equalTo(-123));
        assertThat(proxy.getName(), equalTo("example"));
        assertThat(proxy.getStuff(), equalTo(new String[]{"1", "2", "3"}));
        assertThat(proxy.getCamelCase(), equalTo("ok"));
        assertThat(proxy.getAllFields().size(), equalTo(5));
        assertThat(proxy.toString(), containsString("name"));
        assertTrue(proxy.getBool());
        assertTrue(proxy.isBool());
    }

    @Test
    public void shouldCreateNoProxy() throws Exception {
        DataRecord proxy = createProxy(dataRecord, DataRecord.class);

        assertThat(proxy.getAllFields().size(), equalTo(5));
    }

    @Test
    public void shouldReplacePropertyInValue(){
        System.setProperty("data", "blah");
        DataRecord dr = createProxy(propertyDataRecord, DataRecord.class);
        assertThat(dr.getFieldValue("id").toString(), equalTo("blah"));
    }

    @Test
    public void shouldReplacePropertyInAllValues(){
        System.setProperty("data", "blah");
        CustomDataRecord custom = createProxy(propertyDataRecord, CustomDataRecord.class);
        assertThat(custom.getAllFields().get("id").toString(), equalTo("blah"));
    }

    @Test
    public void equalsContract() {

        // the same instance
        CustomDataRecord proxy = createProxy(dataRecord, CustomDataRecord.class);
        assertEquals(proxy, proxy);

        // different instance, but the same data
        CustomDataRecord proxy2 = createProxy(dataRecord, CustomDataRecord.class);
        assertEquals(proxy, proxy2);

        // different data
        CustomDataRecord partialDataProxy = createProxy(partialDataRecord, CustomDataRecord.class);
        assertNotEquals(proxy, partialDataProxy);
    }

    @Test
    public void equalsContractForDifferentTypes() {

        // different type, the same data
        CustomDataRecord partialDataProxy = createProxy(partialDataRecord, CustomDataRecord.class);
        MinimalDataRecord differentTypeProxy = createProxy(partialDataRecord, MinimalDataRecord.class);
        assertEquals(partialDataProxy, differentTypeProxy);
    }

    @Test
    public void equalsContractForNullableBoolean() {

        // different type, the same data, but 'null' Boolean gets converted to 'false'
        MinimalDataRecord differentTypeProxy = createProxy(partialDataRecord, MinimalDataRecord.class);
        MinimalDataRecordWithoutBoolean partialDataProxy = createProxy(partialDataRecord, MinimalDataRecordWithoutBoolean.class);
        assertNotEquals(partialDataProxy, differentTypeProxy);
    }

    @Test
    public void hashCodeContract() {

        // the same instance
        CustomDataRecord proxy = createProxy(dataRecord, CustomDataRecord.class);
        assertEquals(proxy.hashCode(), proxy.hashCode());

        // different instance, but the same data
        CustomDataRecord proxy2 = createProxy(dataRecord, CustomDataRecord.class);
        assertEquals(proxy.hashCode(), proxy2.hashCode());

        // different data
        CustomDataRecord partialDataProxy = createProxy(partialDataRecord, CustomDataRecord.class);
        assertNotEquals(proxy.hashCode(), partialDataProxy.hashCode());
    }

    @Test
    public void hashCodeContractForDifferentTypes() {

        // different type, the same data
        CustomDataRecord partialDataProxy = createProxy(partialDataRecord, CustomDataRecord.class);
        MinimalDataRecord differentTypeProxy = createProxy(partialDataRecord, MinimalDataRecord.class);
        assertEquals(partialDataProxy.hashCode(), differentTypeProxy.hashCode());
    }

    @Test
    public void hashCodeContractForNullableBoolean() {

        // different type, the same data, but 'null' Boolean gets converted to 'false'
        MinimalDataRecord differentTypeProxy = createProxy(partialDataRecord, MinimalDataRecord.class);
        MinimalDataRecordWithoutBoolean partialDataProxy = createProxy(partialDataRecord, MinimalDataRecordWithoutBoolean.class);
        assertNotEquals(partialDataProxy.hashCode(), differentTypeProxy.hashCode());
    }
    
    @Test
    public void equalsHashCodeForWrongBean() {
        WrongBean bean = createWrongBean("should be boolean return type");
        WrongBean bean2 = createWrongBean("value should be taken into account");

        // wrong property is not taken into account
        assertNotEquals(bean, bean2);
        assertNotEquals(bean.hashCode(), bean2.hashCode());
    }

    private WrongBean createWrongBean(String property) {
        Map<String, Object> data = new HashMap<>();
        data.put("regularAccessor", "OK");
        data.put("wrongAccessor", property);
        DataRecord dataRecord = new DataRecordImpl(data);
        return createProxy(dataRecord, WrongBean.class);
    }

    /**
     * Testing equals() / hashCode() methods performance of
     * 100 data records containing 30 fields.
     */
    @Test
    public void performanceTest() {

        // preparing data for single data record
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            data.put("string" + i, "value" + i);
        }
        DataRecord dataRecord = new DataRecordImpl(data);

        // creating single proxy
        LongDataRecord proxy = createProxy(dataRecord, LongDataRecord.class);

        // existing data record should be separate object instance,
        // otherwise adding operation doesn't call equals() method
        Set<LongDataRecord> set = new HashSet<>();
        set.add(createProxy(dataRecord, LongDataRecord.class));


        // starting timer
        long now = System.nanoTime();

        // adding the same data record to set forces both hashcode() and equals() methods to be called
        for (int i = 0; i < 100; i++) {
            set.add(proxy);
        }

        // checking timer
        long passed = (System.nanoTime() - now) / 1_000_000;
        assertTrue("Expected to be completed in 1000 ms, but was in " + passed, passed < 1800);
    }

    public interface CustomDataRecord extends DataRecord {

        int getId();

        String getName();

        String[] getStuff();

        String getCamelCase();

        boolean getBool();

        boolean isBool();

    }

    public interface MinimalDataRecord extends DataRecord {

        Integer getId();

        String getName();

        Boolean getBool();

        Boolean isBool();

    }

    public interface MinimalDataRecordWithoutBoolean extends DataRecord {

        Integer getId();

        String getName();

    }

    public interface WrongBean extends DataRecord {

        String getRegularAccessor();

        String isWrongAccessor();

    }

    public interface LongDataRecord extends DataRecord {

        String getString0();
        String getString1();
        String getString2();
        String getString3();
        String getString4();
        String getString5();
        String getString6();
        String getString7();
        String getString8();
        String getString9();

        String getString10();
        String getString11();
        String getString12();
        String getString13();
        String getString14();
        String getString15();
        String getString16();
        String getString17();
        String getString18();
        String getString19();

        String getString20();
        String getString21();
        String getString22();
        String getString23();
        String getString24();
        String getString25();
        String getString26();
        String getString27();
        String getString28();
        String getString29();
    }

}
