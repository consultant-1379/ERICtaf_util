package com.ericsson.cifwk.taf;

import static com.ericsson.cifwk.meta.API.Quality.*;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.execution.InitialTestContext;

import java.util.Map;

/**
 * <p>TAF test context holder.</p>
 * <p>Each vUser has its own context.</p>
 */
@API(Stable)
public final class TafTestContext {

    private TafTestContext() {}

    private static final InheritableThreadLocal<Boolean> contextInitialized = new InheritableThreadLocal<>();
    private static final InheritableThreadLocal<TestContext> context = new InheritableThreadLocal<TestContext>() {
        @Override
        protected TestContext initialValue() {
            /*
            Because context is initialized on first access, you can initialize context as simple as TafTestContext.getContext().getVUser().
            Context is inherited in child threads, so initialization point affects if attributes and dataSources will be shared.
            It could be extremely counter intuitive what accessing context by indirect ways like evaluating MVEL in TafMVELProcessor may
            change attribute and dataSource visibility.
            This will be partially fixed by CIP-9307.
            To avoid such cases, before accessing context it should be checked by isInitialized().
             */
            contextInitialized.set(Boolean.TRUE);
            return new InitialTestContext();
        }
    };

    private static final TestContext CONTEXT = new TestContext() {
        @Override
        public int getVUser() {
            return context.get().getVUser();
        }

        @Override
        public void setAttribute(String key, Object value) {
            context.get().setAttribute(key, value);
        }

        @Override
        public <T> T getAttribute(String key) {
            return context.get().getAttribute(key);
        }

        @Override
        public Map<String, Object> getAllAttributes() {
            return context.get().getAllAttributes();
        }

        @Override
        public void removeAttribute(String key) {
            context.get().removeAttribute(key);
        }

        @Override
        public <T> T getOperator(String key) {
            return context.get().getOperator(key);
        }

        @Override
        public void setOperator(String key, Object value) {
            context.get().setOperator(key, value);
        }

        @Override
        public TestDataSource dataSource(String name) {
            return context.get().dataSource(name);
        }

        @Override
        public <T extends DataRecord> TestDataSource<T> dataSource(String name, Class<T> dataType) {
            return context.get().dataSource(name, dataType);
        }

        @Override
        public Map<String, TestDataSource<DataRecord>> getAllDataSources() {
            return context.get().getAllDataSources();
        }

        @Override
        public boolean doesDataSourceExist(String dataSourceName) {
            return context.get().doesDataSourceExist(dataSourceName);
        }

        @Override
        public void removeDataSource(String dataSourceName) {
            context.get().removeDataSource(dataSourceName);
        }

        @Override
        public TestContext createContextForVUser(int vUser) {
            return context.get().createContextForVUser(vUser);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void addDataSource(String dataSourceName, TestDataSource dataSource) {
            context.get().addDataSource(dataSourceName, dataSource);
        }

        @Override
        public int hashCode() {
            return context.get().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return context.get().equals(obj);
        }

        @Override
        public void clearAttributes() {
            context.get().clearAttributes();
        }
    };

    /**
     * Initializes the context for defined vUser
     * @param vUser vUser ID
     */
    public static void initialize(final int vUser) {
        TestContext testContext = new InitialTestContext(vUser);
        initialize(testContext);
    }

    /**
     * Replaces current thread context with existing object.
     * @param testContext initial context to set
     */
    public static void initialize(final TestContext testContext) {
        context.set(testContext);
        contextInitialized.set(Boolean.TRUE);
    }

    /**
     * Detaches context form the current thread
     */
    public static void remove() {
        context.remove();
        contextInitialized.set(Boolean.FALSE);
    }

    /**
     * Returns the context
     */
    public static TestContext getContext() {
        return CONTEXT;
    }

    /**
     * Returns true if context initialized
     */
    public static Boolean isInitialized() {
        return Boolean.TRUE.equals(contextInitialized.get());
    }
}
