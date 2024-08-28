/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.ui.sdk;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SwtUiComponentTest {

    private SwtUiComponent component;

    @Before
    public void setUp() {
        component = spy(new SwtUiComponent(null));
    }

    @Test
    public void sendKeys() {
        final List<Character> chars = Lists.newArrayList();
        final List<KeyStroke> keyStrokes = Lists.newArrayList();
        doAnswer(new Answer<Object>(){
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                chars.add((Character)invocation.getArguments()[0]);
                return null;
            }
        }).when(component).pressShortcut(anyChar());
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                keyStrokes.add((KeyStroke) invocation.getArguments()[0]);
                return null;
            }
        }).when(component).pressShortcut(any(KeyStroke.class));

        String deleteCharacter = "\u007F";
        component.sendKeys("Ok", "\r\n", "o", "K", deleteCharacter);
        assertEquals(Arrays.asList('O', 'k', '\r', '\n', 'o', 'K'), chars);
        assertEquals(1, keyStrokes.size());
        assertEquals("DEL", keyStrokes.get(0).toString());
    }

}
