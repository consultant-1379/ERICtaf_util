package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestNgMock;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import org.junit.Before;
import org.junit.Test;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericsson.cifwk.taf.TestNgMock.runTestNg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class TafGuiceModuleTest {

    static final Map<Integer, InternalOperator> operatorMap = new ConcurrentHashMap<>();
    public static final int THREAD_COUNT = 2;

    @Before
    public void setUp() {
        operatorMap.clear();
    }

    @Test
    public void shouldUseVUserScope() {
        TestNgMock.FailureListener failureListener = new TestNgMock.FailureListener();
        runTestNg(VUseScopeTest.class, new TestNGAnnotationTransformer(), failureListener);

        assertThat(failureListener.isFailed(), is(Boolean.FALSE));

        assertThat(operatorMap.size(), equalTo(THREAD_COUNT));
        assertThat(operatorMap.get(0), not(sameInstance(operatorMap.get(1))));
    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class BaseTest {
    }

    @org.testng.annotations.Test(enabled = false, groups = { "mock" })
    public static class VUseScopeTest extends BaseTest {
        @Inject
        Provider<VUserOperator> provider;

        @org.testng.annotations.Test(timeOut = 500)
        public void testOne() throws Exception {

            for (int i = 0; i < THREAD_COUNT; i++) {
                Thread thread = createThread(i);
                thread.start();
            }
            Thread.sleep(300); // NOSONAR
        }

        private Thread createThread(final int vuser) {

            return new Thread(new Runnable() {

                @Override
                public void run() {
                    TafTestContext.initialize(vuser);
                    int actual = TafTestContext.getContext().getVUser();
                    assertThat(actual, equalTo(vuser));
                    VUserOperator operator1 = provider.get();
                    Object state = new Object();
                    operator1.setState(state);

                    VUserOperator operator2 = provider.get();
                    assertThat(operator2.getState(), sameInstance(state));
                    assertThat(operator1, sameInstance(operator2));
                    assertThat(operator1.internal, sameInstance(operator2.internal));
                    operator1.check(vuser);
                    operatorMap.put(vuser, operator1.internal);
                }
            });
        }

    }

    @Operator
    @VUserScoped
    public static class VUserOperator {

        @Inject
        public InternalOperator internal;

        private int vuser;
        private Object state;

        public void check(int vuser) {
            this.vuser = vuser;
        }

        public int getVuser() {
            return vuser;
        }

        public void setState(Object state) {
            this.state = state;
        }

        public Object getState() {
            return state;
        }
    }

    @Operator
    @VUserScoped
    public static class InternalOperator {
        private Object state;

        public void setState(Object state) {
            this.state = state;
        }

        public Object getState() {
            return state;
        }
    }
}
