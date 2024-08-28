package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.taf.swt.client.SelectionAndInputView;
import com.ericsson.cifwk.taf.ui.DesktopWindow;
import com.ericsson.cifwk.taf.ui.SwtSelectors;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.CheckBox;
import com.ericsson.cifwk.taf.ui.sdk.Label;
import com.ericsson.cifwk.taf.ui.sdk.MenuItem;
import com.ericsson.cifwk.taf.ui.sdk.Table;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;
import com.ericsson.cifwk.taf.ui.sdk.ViewModel;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static java.util.Collections.sort;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TafUiTest extends AbstractPaxTest {

    private ViewModel view;

    private DesktopWindow window;

    @Before
    public void setUp() {
        window = swtNavigator.getWindowByTitle("SWT Application");
        assertNotNull(window);
        view = window.getGenericView();
    }

    @After
    public void tearDown() {
        swtNavigator.close();
    }

    @Test
    public void basicTest() {
        TextBox textBox = view.getTextBox("#0");
        System.out.println(textBox.toString());
        assertEquals("Delete me", textBox.getText());

        // json selectors
        String json = SwtSelectors.forType("org.eclipse.swt.widgets.Text").withText("Delete me").inCurrentWindow().toJson();
        UiComponent uiComponent = view.getViewComponent(json);
        assertThat(uiComponent.getId(), startsWith("sample.text.input"));
    }

    @Test
    public void getViewComponent() {
        UiComponent viewComponent = view.getViewComponent("#0", TextBox.class);
        assertNotNull(viewComponent);
    }

    @Test
    public void customModelTest() throws InterruptedException {
        SelectionAndInputView view = window.getView(SelectionAndInputView.class);
        assertNotNull(view.textBox);
        assertEquals("Delete me", view.textBox.getText());
        assertNull(view.notFoundSelection);
        assertNotNull(view.selection);

        // testing returned object type
        CheckBox boundCheckBox = view.getCheckBox();
        assertNotNull(boundCheckBox);
        assertFalse(boundCheckBox.isSelected());
        boundCheckBox.select();
        assertTrue(boundCheckBox.isSelected());

        // List of components
        assertNotNull(view.allButtons);
        assertEquals(2, view.allButtons.size());
        verifyComponentLabels(view.allButtons, "OK", "Cancel");

        assertNotNull(view.oneOfManyButtons);
        assertThat(view.oneOfManyButtons.getText(), Matchers.isIn(new String[] { "OK", "Cancel" }));
    }

    @Test
    public void checkBoxTest() throws InterruptedException {
        CheckBox checkBox = view.getCheckBox("#0");
        assertNotNull(checkBox);
        assertFalse(checkBox.isSelected());
        checkBox.select();
        assertTrue(checkBox.isSelected());
    }

    @Test
    public void shouldSearchByData() {
        UiComponent uiComponent = view.getViewComponent("{type = 'org.eclipse.swt.widgets.Text', data = 'BigChunkOfData22'}");
        assertNull(uiComponent);
        uiComponent = view.getViewComponent("{type = 'org.eclipse.swt.widgets.Text', data = 'BigChunkOfData'}");
        assertNotNull(uiComponent);
        assertEquals("Delete me", uiComponent.getText());
        TextBox siteTextBox = view.getViewComponent("{type = 'org.eclipse.swt.widgets.Text', data = 'BigChunkOfData'}", TextBox.class);
        assertNotNull(siteTextBox);
        assertEquals("Delete me", siteTextBox.getText());
    }

    @Test
    public void shouldSearchByDataProperty() {
        UiComponent uiComponent = view
                .getViewComponent("{type = 'org.eclipse.swt.widgets.Text', dataProperties = {'customId': 'firstName', 'org.eclipse.swtbot.widget.key': 'WRONG KEY'}}");
        assertNull(uiComponent);
        uiComponent = view.getViewComponent("{type = 'org.eclipse.swt.widgets.Text', dataProperties = {}}");
        assertNotNull(uiComponent);
        uiComponent = view
                .getViewComponent("{type = 'org.eclipse.swt.widgets.Text', dataProperties = {'customId': 'firstName', 'org.eclipse.swtbot.widget.key': 'sample.text.input'}}");
        assertNotNull(uiComponent);
        assertEquals("Delete me", uiComponent.getText());
        assertEquals("firstName", uiComponent.getProperty("customId"));
        assertEquals("sample.text.input", uiComponent.getProperty("org.eclipse.swtbot.widget.key"));
    }

    @Test
    public void table() {

        // get column names
        Table table = view.getViewComponent("{type = 'org.eclipse.swt.widgets.Table', index = 0}", Table.class);
        assertNotNull(table);
        assertEquals("Property", table.getColumnNames().get(0));
        assertEquals("Value", table.getColumnNames().get(1));

        // get cell
        assertEquals("Item 0:0", table.getCell(0, "Property"));
        assertEquals("Item 0:1", table.getCell(0, "Value"));
        assertEquals("Item 1:0", table.getCell(1, "Property"));
        assertEquals("Item 1:1", table.getCell(1, "Value"));

        // get row index
        assertEquals(0, table.getRowIndex("Item 0:0", "Property"));
        assertEquals(0, table.getRowIndex("Item 0:1", "Value"));
        assertEquals(1, table.getRowIndex("Item 1:0", "Property"));
        assertEquals(1, table.getRowIndex("Item 1:1", "Value"));

        // get row count
        assertEquals(4, table.getRowCount());

        // select
        table.select(1);
        assertEquals(1, table.getSelectedRowCount());

        // deselect
        table.deselect();
        assertEquals(0, table.getSelectedRowCount());
    }

    @Test
    public void contextMenu() {
        Label lastSelected = view.getLabel("Input");
        UiComponent contextMenu = lastSelected.getMenuItem("Edit...");
        UiComponent subMenu = contextMenu.getMenuItem("Copy");
        subMenu.click();
        assertEquals("Copy", lastSelected.getText());
    }

    @Test
    public void windowMenu() {
        Label lastSelected = view.getLabel("Input");
        UiComponent windowMenu = window.getMenuItem("Edit");
        UiComponent subMenu = windowMenu.getMenuItem("Copy");
        subMenu.click();
        assertEquals("Copy", lastSelected.getText());
    }

    @Test
    public void shouldTakeScreenshot() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String scrshotPath = String.format("%sswt-screenshot-%s.png",
                tmpDir, UUID.randomUUID().toString());

        window.takeScreenshot(scrshotPath);

        File screenshotFile = new File(scrshotPath);
        Assert.assertTrue(screenshotFile.exists());
        Path path = screenshotFile.toPath();
        Assert.assertEquals("image/png", Files.probeContentType(path));

        Files.delete(path);
    }

    @Test
    public void getViewComponents() {
        List<Button> buttons = view.getViewComponents("{type = 'org.eclipse.swt.widgets.Button'}", Button.class);
        verifyComponentLabels(buttons, "OK", "Cancel");
    }

    private void verifyComponentLabels(List<? extends UiComponent> components, String... expectedComponentLabels) {
        List<String> expectedComponentLabelsList = Arrays.asList(expectedComponentLabels);
        List<String> actualComponentLabels = Lists.newArrayList();
        for (UiComponent component : components) {
            actualComponentLabels.add(component.getText());
        }
        sort(expectedComponentLabelsList);
        sort(actualComponentLabels);
        assertEquals(expectedComponentLabelsList, actualComponentLabels);
    }

    @Test
    public void getViewComponentsGenericTypes() {
        List<UiComponent> buttons = view.getViewComponents("{type = 'org.eclipse.swt.widgets.Button'}", UiComponent.class);
        verifyComponentLabels(buttons, "OK", "Cancel", "Checkbox 1");
    }

    @Test
    public void getViewComponentsDifferentTypes() {
        List<UiComponent> components = view.getViewComponents("{}", UiComponent.class);
        System.out.println(components.size());
        assertTrue(components.size() > 10);
    }

    @Test
    public void proxyWithVarArg() {
        TextBox text = view.getViewComponent("{type = 'org.eclipse.swt.widgets.Text'}", TextBox.class);
        text.focus();

        String deleteCharacter = "\u007F";
        for (int i = 0; i < 20; i++) {
            text.sendKeys(deleteCharacter);
        }
        text.sendKeys("O", "k", "oK");
        assertEquals("OkoK", text.getText());
    }

    @Test
    public void shouldSearchByWidgetProperty() {
        UiComponent uiComponent =
                view.getViewComponent("{type = 'org.eclipse.swt.widgets.Text', text='Delete me', visible='true'}");
        assertNotNull(uiComponent);
        assertEquals("Delete me", uiComponent.getText());
    }

    @Test(timeout = 5000)
    public void messageDialogDoesNotBlockTest() throws InterruptedException {
        final Button okButton = view.getButton("OK");

        swtNavigator.async(new Callable<Void>() {
            @Override
            public Void call() {
                okButton.click();
                return null;
            }
        });

        Thread.sleep(1000);
        // window.waitUntilComponentIsDisplayed("window:Info", 3000);

        ViewModel messageDialogue = swtNavigator.getWindowByTitle("Info").getGenericView();
        messageDialogue.getButton("OK").click();
    }

    @Test
    public void shouldSelectFileInFileDialog() throws Exception {

        // set up
        MenuItem saveMenuItem = window.getMenuItem("File").getMenuItem("Save");
        saveMenuItem.click();
        File tempFile = File.createTempFile("swt-native-dialog", "");

        // double checking file is empty
        FileReader fileReader = new FileReader(tempFile);
        assertEquals("", IOUtils.toString(fileReader));
        fileReader.close();

        // sending file name key strokes
        String userInput = tempFile.getAbsolutePath() + "\r\n";
        window.sendKeys(userInput);

        // checking that SWT application has successfully selected given file in dialog
        Thread.sleep(100);
        fileReader = new FileReader(tempFile);
        System.out.println(tempFile.getAbsolutePath());
        assertEquals("file selected successfully", IOUtils.toString(fileReader));
        fileReader.close();
    }

}
