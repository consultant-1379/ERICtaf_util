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

import com.ericsson.cifwk.taf.datasource.BeanTransformer;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BeanTransformerTest {

    @Test
    public void transformToDataRecord(){
        List<String> field3 = new ArrayList<>();
        field3.add("value1");
        field3.add("value2");

        TransformedBean bean = new TransformedBean("value35", 10, field3, "Non-DR field");

        MyDataRecord mdr = BeanTransformer.transformTo(MyDataRecord.class, bean);

        assertThat(mdr.getField1(), equalTo("value35"));
        assertThat(mdr.getField2(), equalTo(10));
        assertThat(mdr.getField3().get(0),equalTo("value1"));
        assertThat(mdr.getField3().get(1),equalTo("value2"));
        assertThat(mdr.getAllFields().get("field4").toString(),equalTo("Non-DR field"));
        assertThat(mdr.getField5(),equalTo(null));
    }

    @Test
     public void transformBeanToDataRecordAndBack(){
        TransformedBean bean = new TransformedBean("value35", 10);
        MyDataRecord mdr = BeanTransformer.transformTo(MyDataRecord.class, bean);

        assertThat(mdr.getField1(), equalTo("value35"));
        assertThat(mdr.getField2(), equalTo(10));

        TransformedBean beanB = mdr.transformTo(TransformedBean.class);
        assertThat(bean.getField1(), equalTo("value35"));
        assertThat(bean.getField2(), equalTo(10));
    }

    @Test
    public void transformDataRecordToBeanAndBack(){
        DataRecordImpl fromDataRecord = new DataRecordImpl();
        fromDataRecord.setFieldValue("field1","value1");
        fromDataRecord.setFieldValue("field2",7);

        TransformedBean bean = fromDataRecord.transformTo(TransformedBean.class);
        assertThat(bean.getField1(), equalTo("value1"));
        assertThat(bean.getField2(), equalTo(7));

        MyDataRecord mdr = BeanTransformer.transformTo(MyDataRecord.class, bean);

        assertThat(mdr.getField1(), equalTo("value1"));
        assertThat(mdr.getField2(), equalTo(7));
    }

    private interface MyDataRecord extends DataRecord {
        String getField1();
        int getField2();
        List<String> getField3();
        String getField5();
    }

    public static class TransformedBean {

        private String field1;
        private int field2;
        private List<String> field3;
        private String field4;

        /**
         * @param field1
         * @param field2
         * @param field3
         * @param field4
         */
        public TransformedBean(String field1, int field2, List<String> field3, String field4) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.field4 = field4;
        }

        public TransformedBean(String field1, int field2) {
            this.field1 = field1;
            this.field2 = field2;
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

        /**
         * @return field3
         */
        public List<String> getField3() {
            return field3;
        }

        /**
         * @param field3 the field3 to set
         */
        public void setField3(List<String> field3) {
            this.field3 = field3;
        }

        public String getField4() {
            return field4;
        }

        public void setField4(String field4) {
            this.field4 = field4;
        }
    }
}
