package com.ericsson.cifwk.taf.osgi.agent;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServletRouteTest {

    private ServletRoute route;

    @Before
    public void setUp() throws Exception {
        Pattern path = Pattern.compile("/root/([a-z\\.]+)", Pattern.CASE_INSENSITIVE);
        route = new ServletRoute("GET", path);
    }

    @Test
    public void testValidMatch() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/root/com.example.Class");

        MatchResult matches = route.match(request);
        assertNotNull(matches);
        assertEquals("com.example.Class", matches.group(1));
    }

    @Test
    public void testInvalidMatch() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/root/com.123.Class");

        MatchResult matches = route.match(request);
        assertNull(matches);
    }

}
