package com.ericsson.cifwk.taf.swt.agent.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.MatchResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ericsson.cifwk.taf.swt.agent.MethodResponse;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.taf.swt.agent.MethodInvocation;
import com.ericsson.cifwk.taf.swt.agent.ObjectLocator;

@RunWith(MockitoJUnitRunner.class)
public class WidgetHandlerTest {

    @Mock
    private ObjectLocator objectLocator;

    @InjectMocks
    private WidgetHandler widgetHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private MatchResult matches;

    private BigDecimal complexObject = new BigDecimal(123);

    private ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

    @Before
    public void setUp() throws IOException {
        when(objectLocator.put(complexObject)).thenReturn("456");
        when(matches.group(1)).thenReturn("123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleWidgetNotFound() throws ServletException, IOException {
        widgetHandler.handle(request, response, matches);
    }

    @Test
    public void handle() throws ServletException, IOException {
        widgetHandler = spy(widgetHandler);
        when(response.getWriter()).thenReturn(new PrintWriter(responseContent));
        when(objectLocator.get("123")).thenReturn(complexObject);
        MethodInvocation invocation = new MethodInvocation("toString", new String[] {}, new String[] {});
        doReturn(invocation).when(widgetHandler).getEntity(request);
        widgetHandler.handle(request, response, matches);
        assertEquals("{\"value\":\"123\",\"type\":\"java.lang.String\"}", new String(responseContent.toByteArray()));
    }

    @Test
    public void toResponse() {
        assertResponse("java.lang.Integer", "123", widgetHandler.toResponse(123));
        assertResponse("java.lang.Long", "123", widgetHandler.toResponse(123L));
        assertResponse("java.lang.String", "123",  widgetHandler.toResponse("123"));
        assertResponse("java.math.BigDecimal", "456", widgetHandler.toResponse(complexObject));
    }

    private void assertResponse(String expectedType, String expectedValue, MethodResponse actual) {
        assertEquals(expectedType, actual.getType());
        assertEquals(expectedValue, actual.getValue());
    }

    @Test
    public void temp() {
        System.out.println(Integer.parseInt("1363780029"));
    }

    @Test
    public void getFirstPublicType() {
        assertEquals(Iterator.class, widgetHandler.getFirstPublicType(new ArrayList<Object>().iterator().getClass()));
        assertEquals(Label.class, widgetHandler.getFirstPublicType(SwtLabel.class));
        assertEquals(Button.class, widgetHandler.getFirstPublicType(SwtButtonToggle.class));
        assertEquals(Button.class, widgetHandler.getFirstPublicType(SwtButtonToolbar.class));
        assertEquals(CheckBox.class, widgetHandler.getFirstPublicType(SwtCheckBox.class));
        assertEquals(Label.class, widgetHandler.getFirstPublicType(SwtLabel.class));
        assertEquals(Link.class, widgetHandler.getFirstPublicType(SwtLink.class));
        assertEquals(MenuItem.class, widgetHandler.getFirstPublicType(SwtMenuItem.class));
        assertEquals(RadioButton.class, widgetHandler.getFirstPublicType(SwtRadioButton.class));
        assertEquals(RadioButton.class, widgetHandler.getFirstPublicType(SwtRadioButtonToolbar.class));
        assertEquals(Select.class, widgetHandler.getFirstPublicType(SwtSelect.class));
        assertEquals(Select.class, widgetHandler.getFirstPublicType(SwtSelectCCombo.class));
        assertEquals(Table.class, widgetHandler.getFirstPublicType(SwtTable.class));
        assertEquals(TextBox.class, widgetHandler.getFirstPublicType(SwtTextBox.class));
        assertEquals(UiComponent.class, widgetHandler.getFirstPublicType(SwtControl.class));
        assertEquals(UiComponent.class, widgetHandler.getFirstPublicType(SwtUiComponent.class));
        assertEquals(UiComponent.class, widgetHandler.getFirstPublicType(UiComponentAdapter.class));
        assertEquals(UiComponent.class, widgetHandler.getFirstPublicType(WidgetWrapper.class));
    }

}
