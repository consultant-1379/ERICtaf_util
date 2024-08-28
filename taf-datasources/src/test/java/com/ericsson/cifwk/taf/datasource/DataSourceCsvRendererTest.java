package com.ericsson.cifwk.taf.datasource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.CharArrayWriter;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;

public class DataSourceCsvRendererTest {

    @Test
    public void shouldWrite() {
        DataSourceCsvRenderer renderer = new DataSourceCsvRenderer();
        List<Map<String, Object>> rows = Lists.newArrayList();
        Map<String, Object> data = Maps.newHashMap();
        data.put("x", 1);
        data.put("y", 2);
        data.put("z", 3);
        rows.add(data);
        TestDataSource<DataRecord> dataSource = new TestDataSourceImpl(rows);

        CharArrayWriter out = new CharArrayWriter();
        renderer.render(out, dataSource);
        assertThat(out.toString(), isOneOf("z,y,x\r\n3,2,1\r\n", "x,y,z\r\n1,2,3\r\n"));
    }

}
