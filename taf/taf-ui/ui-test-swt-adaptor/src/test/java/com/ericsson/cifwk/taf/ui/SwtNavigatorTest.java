package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.taf.ui.sdk.ViewModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SwtNavigatorTest {

    @Mock
    private WidgetProxyFactory proxyFactory;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private SwtNavigator swtNavigator = new SwtNavigator("host", 8080);

    @Test
    public void getView() {
        swtNavigator.getView("SWT Window");
        verify(proxyFactory).create(ViewModel.class, "window:SWT Window");
    }

    @Test
    public void getViews() throws IOException {
        swtNavigator.getViews();
        verify(restClient).getViews();
    }

    @Test
    public void close() throws IOException {
        swtNavigator.reset();
        verify(restClient).reset();
        verify(proxyFactory).reset();
    }

}
