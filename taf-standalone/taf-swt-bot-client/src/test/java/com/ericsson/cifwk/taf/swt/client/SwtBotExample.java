package com.ericsson.cifwk.taf.swt.client;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.Label;
import com.ericsson.cifwk.taf.ui.sdk.Select;
import com.ericsson.cifwk.taf.ui.sdk.ViewModel;

import static org.junit.Assert.assertEquals;

public class SwtBotExample {

    public static void main(String[] args) {
        SwtBotNavigator swtBotNavigator = new SwtBotNavigator("localhost", 8080);
        System.out.println(swtBotNavigator.getViews());

        SWTBot swtBot = swtBotNavigator.getSwtBot("SWT Application");
        SWTBotText text = swtBot.textWithLabel("Input");
        text.setText("It works again");

        ViewModel view = swtBotNavigator.getView("SWT Application");
        UiComponent viewComponent = view.getViewComponent("{type = 'org.eclipse.ui.forms.widgets.Hyperlink', nativeWidget = true, index = 0}");
        Hyperlink hyperlink = swtBotNavigator.as(viewComponent, Hyperlink.class);
        assertEquals("Hyperlink text", hyperlink.getText());

        System.out.println("Success!");
    }

    public static void swtBotExample() {
        SwtBotNavigator swtBotNavigator = new SwtBotNavigator("atvts827.athtem.eei.ericsson.se", 7777);
        System.out.println(swtBotNavigator.getViews());
        ViewModel view = swtBotNavigator.getView("OSS Common Explorer - valid configuration");
        UiComponent viewComponent = view.getViewComponent("{type = 'org.eclipse.swt.custom.CTabItem', mnemonicText = 'BSIM', initActions = ['activate']}");
        viewComponent = view
                .getViewComponent("{type = 'org.eclipse.swt.custom.CTabItem', mnemonicText = 'BSIM', wrapperType = 'org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem', initActions = ['activate']}");

        Select select = view.getSelect("#0");
        select.selectByValue("1");
        System.out.println(select.getText());

        Label label = view.getLabel("#6");
        System.out.println(label.getText());

        Button button = view.getButton("New Node:hover");
        button.click();

        System.out.println(view.getLabel("#6").getText());
        Button addNodesButton = view.getButton("Add Nodes");
        addNodesButton.click();
    }

}
