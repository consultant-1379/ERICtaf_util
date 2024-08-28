package com.ericsson.cifwk.taf.eiffel;

import com.ericsson.cifwk.taf.eiffel.testng.ExecutionEventHolder;
import com.ericsson.duraci.configuration.EiffelConfiguration;
import com.ericsson.duraci.datawrappers.Environment;
import com.ericsson.duraci.datawrappers.LogReference;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         01/03/2016
 */
public class EiffelAdapterTest {

    private EiffelAdapter adapter;
    MessageSender messageSender;

    @Before
    public void setUp() {
        messageSender = mock(MessageSender.class);
        EiffelConfiguration eiffelConfigurationMock = mock(EiffelConfiguration.class);
        adapter = new EiffelAdapter(eiffelConfigurationMock, messageSender);
    }

    @Test
    public void shouldGetLogReferenceMap() {
        adapter = spy(adapter);
        doReturn(null).doReturn("http://www.log.se/121/").
                when(adapter).getLogUrl();
        assertEquals(0, adapter.getLogReferenceMap().size());
        Map<String, LogReference> logReferenceMap = adapter.getLogReferenceMap();
        assertEquals(1, logReferenceMap.size());
        assertEquals("http://www.log.se/121/", logReferenceMap.get(EiffelAdapter.DEFAULT_LOG_MAPPING).getUri());
    }

    @Test
    public void shouldMapEnvironmentParameters() throws Exception {
        Map<String, Object> params = new LinkedHashMap<>();
        Object obj = new Object();
        params.put("key1", "value1");
        params.put("key2", obj);
        params.put("key3", null);

        List<Environment> envParams = adapter.mapEnvironmentParameters(params);

        assertThat(envParams.get(0).getName(), is("key1"));
        assertThat(envParams.get(0).getValue(), is("value1"));

        assertThat(envParams.get(1).getName(), is("key2"));
        assertThat(envParams.get(1).getValue(), is(obj.toString()));

        assertThat(envParams.get(2).getName(), is("key3"));
        assertThat(envParams.get(2).getValue(), is("null"));

        assertThat(adapter.mapEnvironmentParameters(Collections.<String, Object>emptyMap()),
                Matchers.empty());
    }

    @Test
    public void testToStringSafely() throws Exception {
        assertThat(adapter.toStringSafely("notNull"), is("notNull"));
        assertThat(adapter.toStringSafely(null), is("null"));
    }

    @Test
    public void testFireTestCaseFinished() throws Exception {
        ExecutionEventHolder.setTestCaseEvent(new ExecutionEvent(null, null));
        adapter = spy(adapter);

        adapter.fireTestCaseFinished(ResultCode.SUCCESS, Collections.<String, Object>emptyMap());

        verify(adapter).sendMessage(Mockito.any(EiffelMessage.class));
    }
}