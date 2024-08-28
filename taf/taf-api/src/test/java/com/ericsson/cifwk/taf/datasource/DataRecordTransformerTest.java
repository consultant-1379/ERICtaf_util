package com.ericsson.cifwk.taf.datasource;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class DataRecordTransformerTest {

    @Test
    public void transformToBean(){
        List<String> field3 = new ArrayList<>();
        field3.add("value1");
        field3.add("value2");

        DataRecordImpl fromDataRecord = new DataRecordImpl();
        fromDataRecord.setFieldValue("field1","value1");
        fromDataRecord.setFieldValue("field2",7);
        fromDataRecord.setFieldValue("field3",field3);
        fromDataRecord.setFieldValue("field5","Non-Bean Field");
        
        TransformedBean bean = fromDataRecord.transformTo(TransformedBean.class);
        assertThat(bean.getField1(), equalTo("value1"));
        assertThat(bean.getField2(), equalTo(7));
        assertThat(bean.getField3().get(0), equalTo("value1"));
        assertThat(bean.getField3().get(1), equalTo("value2"));
        assertThat(bean.getField4(),equalTo(null));
    }

    public static class TransformedBean {

        private String field1;
        private int field2;
        private List<String> field3;
        private Object field4;

        /**
         * @param field1
         * @param field2
         * @param field3
         * @param field4
         */
        public TransformedBean(String field1, int field2, List<String> field3, Object field4) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.field4 = field4;
        }

        public TransformedBean() {
        }
        /**
         * @return the field1
         */
        public String getField1() {
            return field1;
        }
        /**
         * @param field1 the field1 to set
         */
        public void setField1(String field1) {
            this.field1 = field1;
        }
        /**
         * @return the field2
         */
        public int getField2() {
            return field2;
        }
        /**
         * @param field2 the field2 to set
         */
        public void setField2(int field2) {
            this.field2 = field2;
        }


        public List<String> getField3() {
            return field3;
        }

        public void setField3(List<String> field3) {
            this.field3 = field3;
        }

        public Object getField4() {
            return field4;
        }

        public void setField4(Object field4) {
            this.field4 = field4;
        }
    }
}
