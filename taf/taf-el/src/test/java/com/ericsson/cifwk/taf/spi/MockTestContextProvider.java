package com.ericsson.cifwk.taf.spi;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 14/03/2016
 */
public class MockTestContextProvider implements TestContextProvider {

    TestContext mockContext = new MockTestContext();

    @Override
    public TestContext get() {
        return mockContext;
    }

    @Override
    public void initialize(TestContext testContext) {
        mockContext = testContext;
    }

    @Override
    public boolean isContextInitialized() {
        return mockContext != null;
    }

    @Override
    public void removeContext() {
        mockContext = null;
    }

    @Override
    public void initialize(int vUser) {
        mockContext = new MockTestContext(vUser);
    }

    private static class MockTestContext implements TestContext {

        private int vUser;

        private Map<String, Object> attributes = new HashMap<>();

        public MockTestContext(int vUser) {
            this.vUser = vUser;
        }

        public MockTestContext() {
            this(0);
        }

        @Override
        public int getVUser() {
            return vUser;
        }

        @Override
        public void setAttribute(String key, Object value) {
            attributes.put(key,value);
        }

        @Override
        public <T> T getAttribute(String key) {
            return (T) attributes.get(key);
        }

        @Override
        public Map<String, Object> getAllAttributes() {
            return attributes;
        }

        @Override
        public void removeAttribute(String key) {
            attributes.remove(key);
        }

        @Override
        public void clearAttributes() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public TestDataSource<DataRecord> dataSource(String name) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public <T extends DataRecord> TestDataSource<T> dataSource(String name, Class<T> dataType) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Map<String, TestDataSource<DataRecord>> getAllDataSources() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean doesDataSourceExist(String dataSourceName) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void removeDataSource(String dataSourceName) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void addDataSource(String dataSourceName, TestDataSource<? extends DataRecord> dataSource) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public TestContext createContextForVUser(int vUser) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public <T> T getOperator(String key) {
            return null;
        }

        @Override
        public void setOperator(String key, Object value) {

        }
    }
}
