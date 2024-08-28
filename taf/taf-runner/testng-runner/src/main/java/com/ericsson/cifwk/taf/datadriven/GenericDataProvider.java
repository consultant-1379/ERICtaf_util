package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataProviders;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.eventbus.Priority;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.management.TafContext;
import com.ericsson.cifwk.taf.method.ParameterTransformer;
import com.ericsson.cifwk.taf.mvel.DataRecordStringFilterPredicate;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.ericsson.cifwk.taf.testapi.TestSuite;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.internal.ConstructorOrMethod;

// TODO: how to eliminate it?
import sun.reflect.MethodAccessor; // NOSONAR

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a central TestNG Data Provider for Data Driven feature of TAF. Data
 * Provider selection is resolved here.
 */
public final class GenericDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericDataProvider.class);
    private static final String PARAMETER_DATA_PROVIDER = "taf.data-provider";
    private static final int SUBSCRIPTION_PRIORITY = 52;

    public static final String ERROR_MSG_FAILED_TO_INITIALISE_DATA_PROVIDER = "Test skipped for [%s], failed to initialise @DataProvider, exception is:";
    public static final String ERROR_MSG_CANT_WRAP_TEST_METHOD = "Cant't wrap test method for [%s], exception is:";

    // synchronized because multiple vusers are initializing data providers
    @DataProvider(name = "default")
    public static synchronized Iterator<Object[]> getDataProvider(final Method method, final ITestContext context) {
        try {
            Preconditions.checkNotNull(method);
            ISuite suite = context.getSuite();
            LinkedHashMap<String, Iterator<DataRecord>> iterators = getDataIterators(method, suite);
            return ParameterTransformer.transform(iterators, method);
        } catch (Exception ex) {
            String msg = String.format(ERROR_MSG_FAILED_TO_INITIALISE_DATA_PROVIDER, method);
            LOGGER.error(msg, ex);
            return getErrorIterators(method, ex);
        }
    }

    static Iterator<Object[]> getErrorIterators(final Method method, final Exception ex) {
        wrapTestMethod(method, ex);
        Object[] parameters = ObjectArrays.newArray(Object.class, method.getParameterTypes().length);
        return Arrays.asList(
                new Object[][]{parameters}
        ).iterator();
    }

    static void wrapTestMethod(final Method method, final Exception ex) {
        try {
            Field fMethodAccessor = Method.class.getDeclaredField("methodAccessor");
            fMethodAccessor.setAccessible(true);
            fMethodAccessor.set(method, new MethodAccessor() {
                @Override
                public Object invoke(Object o, Object[] objects) throws InvocationTargetException {
                    throw Throwables.propagate(ex);
                }
            });
        } catch (Exception e) {
            String msg = String.format(ERROR_MSG_CANT_WRAP_TEST_METHOD, method);
            LOGGER.error(msg, e);
            LOGGER.error(msg, ex);
        }
    }

    static LinkedHashMap<String, Iterator<DataRecord>> getDataIterators(final Method method, final ISuite suite) {
        ISuiteDataSourceContext context = new ISuiteDataSourceContext(suite, method);
        LinkedHashMap<String, Iterator<DataRecord>> iterators = new LinkedHashMap<>();

        if (isDataProviderOnSuiteLevel(suite) && method.isAnnotationPresent(DataDriven.class)) {
            String dataProviderName = suite.getParameter(PARAMETER_DATA_PROVIDER);
            if (LOGGER.isInfoEnabled()) {
                String msg = String.format("For method [%s] used the DataProvider [%s],defined in suite.xml",
                        method, dataProviderName);
                LOGGER.info(msg);
            }
            String filter = method.getAnnotation(DataDriven.class).filter();
            TestDataSource dataSource = provideDataSource(method, context, dataProviderName, filter);
            iterators.put(dataProviderName, dataSource.iterator());
        } else {
            Collection<DataDriven> dataDrivenAnnotations = getDataDrivenAnnotations(method);
            for (DataDriven annotation : dataDrivenAnnotations) {
                String dataSourceName = annotation.name();
                String filter = annotation.filter();
                TestDataSource dataSource = provideDataSource(method, context, dataSourceName, filter);
                iterators.put(dataSourceName, dataSource.iterator());
            }
        }

        return iterators;
    }

    private static TestDataSource provideDataSource(Method method, ISuiteDataSourceContext context, String dataSourceName, String filter) {
        DataRecordStringFilterPredicate predicate = null;
        if (!Strings.isNullOrEmpty(filter)) {
            predicate = new DataRecordStringFilterPredicate(filter);
        }

        List<DataSourceAdapter> dataSourceAdapters = ServiceRegistry.getAllDataSourceAdapters();
        for (DataSourceAdapter dataSourceAdapter : dataSourceAdapters) {
            Optional<TestDataSource<DataRecord>> optionalDataSource =
                    dataSourceAdapter.provide(dataSourceName, method, context, predicate, DataRecord.class);
            if (optionalDataSource.isPresent()) {
                //TestDataSource<DataRecord> dataSource = optionalDataSource.get();
                //TafContext.setDataSource(dataSourceName, dataSource);
                //return dataSource;
                return optionalDataSource.get();
            }
        }
        throw new IllegalArgumentException(getMissingDataSourceMessage(method, dataSourceName, filter, dataSourceAdapters));
    }

    @VisibleForTesting
    static String getMissingDataSourceMessage(Method method, String dataSourceName, String filter, List<DataSourceAdapter> dataSourceAdapters) {
        List<String> dataSourceClassNames = Lists.newArrayList(Iterables.transform(dataSourceAdapters, new Function<DataSourceAdapter, String>() {
            @Override
            public String apply(DataSourceAdapter adapter) {
                return adapter.getClass().getName();
            }
        }));
        String listOfAdapterClasses = Joiner.on(",").skipNulls().join(dataSourceClassNames);
        return String.format("Failed to find an applicable data source for " +
                        "{method='%s', data source name='%s', filter='%s'}. List of adapters that were tried to apply: [%s]",
                method, dataSourceName, filter, listOfAdapterClasses);
    }

    static Collection<DataDriven> getDataDrivenAnnotations(Method method) {
        Map<String, DataDriven> annotations = new HashMap<>();
        if (method.isAnnotationPresent(DataDriven.class)) {
            DataDriven annotation = method.getAnnotation(DataDriven.class);
            String name = annotation.name();
            annotations.put(name, annotation);
        }
        if (method.isAnnotationPresent(DataProviders.class)) {
            for (DataDriven annotation : method.getAnnotation(DataProviders.class).value()) {
                String name = annotation.name();
                if (annotations.containsKey(name)) {
                    String msg = String.format("For [%s] same name [%s] usage in the different DataDriven annotations", method, name);
                    throw new IllegalArgumentException(msg);
                }
                annotations.put(name, annotation);
            }
        }
        return annotations.values();
    }


    private static boolean isDataProviderOnSuiteLevel(ISuite suite) {
        String dataProviderName = suite.getParameter(PARAMETER_DATA_PROVIDER);
        return dataProviderName != null && !dataProviderName.trim().isEmpty();
    }


    @Subscribe
    @Priority(SUBSCRIPTION_PRIORITY)
    public void onSuiteFinish(TestGroupFinishedEvent testGroupEvent) {
        List<IInvokedMethod> methods = new ArrayList<>();
        TestSuite testSuite = (TestSuite) testGroupEvent.getTestGroup();
        ISuite testNgSuite = testSuite.getSuite();
        methods.addAll(testNgSuite.getAllInvokedMethods());
        for (IInvokedMethod invokedMethod : methods) {
            ConstructorOrMethod constructorOrMethod = invokedMethod.getTestMethod().getConstructorOrMethod();
            Method method = constructorOrMethod.getMethod();
            ISuiteDataSourceContext context = new ISuiteDataSourceContext(testNgSuite, method);
            releaseDataSources(context);
        }
    }

    public static void releaseDataSources(DataSourceContext context) {
        Map<String, TestDataSource> dataSources = context.getDataSources();
        if (dataSources == null) {
            return;
        }

        for (Map.Entry<String, TestDataSource> entry : dataSources.entrySet()) {
            try {
                TestDataSource dataSource = entry.getValue();
                dataSource.close();
            } catch (Exception e) {
                LOGGER.error(String.format("Failed to close data source [%s], since the exception [%s] was thrown",
                        entry.getKey(), e.getMessage()), e);
            }
            dataSources.remove(entry.getKey());
        }
    }
}
