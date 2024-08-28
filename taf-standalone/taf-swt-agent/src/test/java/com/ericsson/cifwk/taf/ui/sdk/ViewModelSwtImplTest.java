package com.ericsson.cifwk.taf.ui.sdk;

import static org.junit.Assert.*;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ViewModelSwtImplTest {

    private ViewModelSwtImpl viewModel;

    private CustomTabItem tabItem;

    @Before
    public void setUp() {
        viewModel = new ViewModelSwtImpl(null);
        tabItem = new CustomTabItem();
    }

    @Test
    public void wrapToSwtBotComponent() {
        assertEquals(SWTBotTabItem.class, viewModel.wrapToSwtBotComponent(tabItem, SWTBotTabItem.class.getName()).getClass());
    }

    @Test
    public void getSwtBotWrapperClassName() {
        assertEquals(SWTBotCTabItem.class.getName(), viewModel.getSwtBotWrapperClassName(org.eclipse.swt.custom.CTabItem.class));
    }

    @Test
    @Ignore
    public void getSwtBotWrapperForCustomComponents() {
        assertEquals(SWTBotCTabItem.class.getName(), viewModel.getSwtBotWrapperClassName(MyCTabItem.class));
    }

    private static class TabItem {

    }

    private static class CustomTabItem extends TabItem {

    }

    private static class SWTBotTabItem {

        @SuppressWarnings("unused")
        public SWTBotTabItem(TabItem target) {
        }

        @SuppressWarnings("unused")
        public SWTBotTabItem(TabItem target, String description) {
        }

    }

    private static class MyCTabItem extends org.eclipse.swt.custom.CTabItem {

        public MyCTabItem(CTabFolder arg0, int arg1) {
            super(arg0, arg1);
        }

    }

}
