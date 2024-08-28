package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class VT100ParserStateTest {

    private Action execute = mock(Action.class);
    private Action print = mock(Action.class);
    private Action transitionTo = mock(Action.class);
    private Action any_transitionTo = mock(Action.class);
    private Action ignore = mock(Action.class);
    private Action esc_dispatch = mock(Action.class);
    private Action csi_dispatch = mock(Action.class);
    private Action collect = mock(Action.class);
    private Action param = mock(Action.class);

    private VT100ParserAction action = mock(VT100ParserAction.class);

    VT100ParserState state() {
        return new VT100ParserState(action);
    }

    @Before
    public void setUp() throws Exception {
        when(action.execute()).thenReturn(execute);
        when(action.print()).thenReturn(print);
        when(action.transitionTo(org.mockito.Matchers.any(State.class))).thenReturn(any_transitionTo);
        when(action.ignore()).thenReturn(ignore);
        when(action.esc_dispatch()).thenReturn(esc_dispatch);
        when(action.csi_dispatch()).thenReturn(csi_dispatch);
        when(action.collect()).thenReturn(collect);
        when(action.param()).thenReturn(param);
    }

    void assertNoMoreInteractions(){
        verifyNoMoreInteractions(execute);
        verifyNoMoreInteractions(print);
        verifyNoMoreInteractions(transitionTo);
        verifyNoMoreInteractions(any_transitionTo);
        verifyNoMoreInteractions(ignore);
        verifyNoMoreInteractions(esc_dispatch);
        verifyNoMoreInteractions(csi_dispatch);
        verifyNoMoreInteractions(collect);
        verifyNoMoreInteractions(param);
    }

    @Test
    public void onGROUND_shouldExecute_x00x17_x19_x1Cx1F() throws Exception {
        State state = state().GROUND;
        int count = process(state, between(0x00, 0x17), equalTo(0x19), between(0x1C, 0x1F));
        verify(execute, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onGROUND_shouldPrint_x20x7F() throws Exception {
        State state = state().GROUND;
        int count = process(state, between(0x20, 0x7F));
        verify(print, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void fromANYWHERE_shouldExecuteAndTransite_to_GROUND() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("GROUND"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().ANYWHERE;
        int count = process(state, equalTo(0x18), equalTo(0x1A));
        verify(execute, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void fromANYWHERE__x1B_shouldTransiteto_ESCAPE() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("ESCAPE"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().ANYWHERE;
        int count = process(state, equalTo(0x1B));
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPE_shouldExecute_x00x17_x19_x1Cx1F() throws Exception {
        State state = state().ESCAPE;
        int count = process(state, between(0x00, 0x17), equalTo(19), between(0x1C, 0x1F));
        verify(execute, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPE_shouldIgnore_x7F() throws Exception {
        State state = state().ESCAPE;
        int count = process(state, equalTo(0x7F));
        verify(ignore, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPE_shouldDipatchAndTransiteto_GROUND() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("GROUND"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().ESCAPE;
        int count = process(state, between(0x30, 0x4F), between(0x51, 0x57), equalTo(0x59), equalTo(0x5A), equalTo(0x5C), between(0x60, 0x7E));
        verify(esc_dispatch, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPTE_shouldCollectTransite_to_ESCAPE_INTERMEDIATE() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("ESCAPE_INTERMEDIATE"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().ESCAPE;
        int count = process(state, between(0x20, 0x2F));
        verify(collect, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPTE_shouldTransiteTo_CSI_ENTRY() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_ENTRY"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().ESCAPE;
        int count = process(state, equalTo(0x5B));
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPE_INTERMEDIATE_shouldExecute_x00x17_x19_x1Cx1F() throws Exception {
        State state = state().ESCAPE_INTERMEDIATE;
        int count = process(state, between(0x00, 0x17), equalTo(19), between(0x1C, 0x1F));
        verify(execute, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void oESCAPE_INTERMEDIATE_shouldIgnore_x7F() throws Exception {
        State state = state().ESCAPE_INTERMEDIATE;
        int count = process(state, equalTo(0x7F));
        verify(ignore, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPE_INTERMEDIATE_shouldCollect_x20x27() throws Exception {
        State state = state().ESCAPE_INTERMEDIATE;
        int count = process(state,  between(0x20, 0x2F));
        verify(collect, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onESCAPE_INTERMEDIATE_shouldDispatchTransite_to_GROUND() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("GROUND"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().ESCAPE_INTERMEDIATE;
        int count = process(state, between(0x30, 0x7E));
        verify(esc_dispatch, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_ENTRY_shouldExecute_x00x17_x19_x1Cx1F() throws Exception {
        State state = state().CSI_ENTRY;
        int count = process(state, between(0x00, 0x17), equalTo(19), between(0x1C, 0x1F));
        verify(execute, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_ENTRY_shouldIgnore_x7F() throws Exception {
        State state = state().CSI_ENTRY;
        int count = process(state, equalTo(0x7F));
        verify(ignore, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_ENTRY_x40x7E_shouldDispatchAndTransiteTo_GROUND() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("GROUND"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_ENTRY;
        int count = process(state, between(0x40, 0x7E));
        verify(csi_dispatch, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_ENTRY_x30x39_x3B_shouldParamAndTransiteTo_CSI_PARAM() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_PARAM"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_ENTRY;
        int count = process(state, between(0x30, 0x39), equalTo(0x3B));
        verify(param, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_ENTRY_x3Cx3F_shouldCollectAndTransiteTo_CSI_PARAM() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_PARAM"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_ENTRY;
        int count = process(state, between(0x3C, 0x3F));
        verify(collect, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_ENTRY_x3A_shouldTransiteTo_CSI_IGNORE() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_IGNORE"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_ENTRY;
        int count = process(state, equalTo(0x3A));
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_ENTRY_x20x2F_shouldCollectAndTransiteTo_CSI_INTERMEDIATE() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_INTERMEDIATE"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_ENTRY;
        int count = process(state, between(0x20, 0x2F));
        verify(collect, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_PARAM_shouldExecute_x00x17_x19_x1Cx1F() throws Exception {
        State state = state().CSI_PARAM;
        int count = process(state, between(0x00, 0x17), equalTo(19), between(0x1C, 0x1F));
        verify(execute, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_PARAM_shouldIgnore_x7F() throws Exception {
        State state = state().CSI_PARAM;
        int count = process(state, equalTo(0x7F));
        verify(ignore, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_PARAM_shouldParam_x30x39_x3B() throws Exception {
        State state = state().CSI_PARAM;
        int count = process(state, between(0x30, 0x39), equalTo(0x38));
        verify(param, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_PARAM_x40x7F_shouldDispatchAndTransiteTo_GROUND() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("GROUND"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_PARAM;
        int count = process(state, between(0x40, 0x7E));
        verify(csi_dispatch, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }


    @Test
    public void oCSI_PARAM_x3A_x3Cx3F_shouldTransiteTo_CSI_IGNORE() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_IGNORE"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_PARAM;
        int count = process(state, equalTo(0x3A), between(0x3C, 0x3F));
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void oCSI_PARAM_x20x2F_shouldCollectAndTransiteTo_CSI_INTERMEDIATE() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_INTERMEDIATE"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_PARAM;
        int count = process(state, between(0x20, 0x2F));
        verify(collect, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_IGNORE_shouldExecute_x00x17_x19_x1Cx1F() throws Exception {
        State state = state().CSI_IGNORE;
        int count = process(state, between(0x00, 0x17), equalTo(19), between(0x1C, 0x1F));
        verify(execute, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_IGNORE_shouldIgnore_x7F() throws Exception {
        State state = state().CSI_IGNORE;
        int count = process(state, equalTo(0x7F));
        verify(ignore, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_IGNORE_x40x7F_shouldTransiteTo_GROUND() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("GROUND"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_IGNORE;
        int count = process(state, between(0x40, 0x7E));
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_INTERMEDIATE_shouldExecute_x00x17_x19_x1Cx1F() throws Exception {
        State state = state().CSI_INTERMEDIATE;
        int count = process(state, between(0x00, 0x17), equalTo(19), between(0x1C, 0x1F));
        verify(execute, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_INTERMEDIATE_shouldIgnore_x7F() throws Exception {
        State state = state().CSI_INTERMEDIATE;
        int count = process(state, equalTo(0x7F));
        verify(ignore, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_INTERMEDIATE_shouldCollect_x20x2F() throws Exception {
        State state = state().CSI_INTERMEDIATE;
        int count = process(state, between(0x20, 0x2F));
        verify(collect, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_INTERMEDIATE_x40x7F_shouldDispatchAndTransiteTo_GROUND() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("GROUND"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_INTERMEDIATE;
        int count = process(state, between(0x40, 0x7E));
        verify(csi_dispatch, times(count)).perform();
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    @Test
    public void onCSI_INTERMEDIATE_x30x3F_shouldTransiteTo_CSI_IGNORE() throws Exception {
        Matcher<State> matcher = hasProperty("name", Matchers.equalTo("CSI_IGNORE"));
        when(action.transitionTo(argThat(matcher))).thenReturn(transitionTo);
        State state = state().CSI_INTERMEDIATE;
        int count = process(state, between(0x30, 0x3F));
        verify(transitionTo, times(count)).perform();
        assertNoMoreInteractions();
    }

    char[] between(int i1, int i2) {
        char[] chars = new char[i2 - i1 + 1];
        for (int i = i1, pos = 0; i <= i2; i++, pos++) {
            chars[pos] = (char) i;
        }
        return chars;
    }

    char[] equalTo(int i) {
        return new char[]{(char) i};
    }


    int process(State state, char[]... param) {
        int count = 0;
        for (char[] chars : param) {
            for (char c : chars) {
                count++;
                state.process(c);
            }
        }
        return count;
    }

}
