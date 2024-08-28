package com.ericsson.cifwk.taf.swtsample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

public class SwtWindow {

    protected Shell shell;
    private Table table;
    private Text text;
    private Display display;
    private volatile boolean initialized;
    private Label label;
    private Button checkBox;

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
        final SwtWindow window = new SwtWindow();
        try {
            window.open();
            Thread.sleep(200000);
            window.close();
        } finally {
            try {
                window.close();
            } catch (Exception e) {
                System.err.println("Can't close SwtWindow :" + e);
            }
        }
    }

    /**
     * Open the window.
     */
    public void open() throws InterruptedException {
        new Thread() {
            @Override
            public void run() {
                openInternal();
            }
        }.start();

        while (!initialized) {
            // do not return while initializing
        }
    }

    private void openInternal() {
        display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        initialized = true;
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public void close() {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                shell.close();
                shell.dispose();
                display.close();
                display.dispose();
            }
        });

    }

    public static void setId(Widget widget, String id) {
        final String WIDGET_ID_KEY = "org.eclipse.swtbot.widget.key";
        widget.setData(WIDGET_ID_KEY, id);
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {
        shell = new Shell();
        shell.setSize(450, 400);
        shell.setText("SWT Application");

        SelectionListener menuListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String value = "Default";
                Object source = event.getSource();
                if (source instanceof MenuItem) {
                    MenuItem menu = (MenuItem) source;
                    value = menu.getText();
                }
                label.setText(value);
            }
        };

        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);

        MenuItem mntmFile_1 = new MenuItem(menu, SWT.CASCADE);
        mntmFile_1.setText("File");
        mntmFile_1.addSelectionListener(menuListener);

        Menu menu_1 = new Menu(mntmFile_1);
        mntmFile_1.setMenu(menu_1);

        MenuItem mntmOpen = new MenuItem(menu_1, SWT.NONE);
        mntmOpen.setText("&Open\tCTRL+O");
        mntmOpen.setAccelerator(SWT.CTRL + 'O');
        mntmOpen.addSelectionListener(menuListener);

        MenuItem saveMenu = new MenuItem(menu_1, SWT.NONE);
        saveMenu.setText("&Save\tCTRL+S");
        saveMenu.setAccelerator(SWT.CTRL + 'S');
        saveMenu.addSelectionListener(menuListener);
        saveMenu.addSelectionListener(new SaveFileNativeDialogue(shell));

        MenuItem mntmClose = new MenuItem(menu_1, SWT.NONE);
        mntmClose.setText("Close");
        mntmClose.addSelectionListener(menuListener);

        MenuItem mntmEdit = new MenuItem(menu, SWT.CASCADE);
        mntmEdit.setText("Edit");
        mntmEdit.addSelectionListener(menuListener);

        Menu menu_2 = new Menu(mntmEdit);
        mntmEdit.setMenu(menu_2);

        MenuItem mntmCopy = new MenuItem(menu_2, SWT.NONE);
        mntmCopy.setText("Copy");
        mntmCopy.addSelectionListener(menuListener);

        MenuItem mntmPaste = new MenuItem(menu_2, SWT.NONE);
        mntmPaste.setText("Paste");
        mntmPaste.addSelectionListener(menuListener);

        MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
        mntmHelp.setText("Help");
        mntmHelp.addSelectionListener(menuListener);

        Menu menu_3 = new Menu(mntmHelp);
        mntmHelp.setMenu(menu_3);

        MenuItem mntmAbout = new MenuItem(menu_3, SWT.NONE);
        mntmAbout.setText("About");
        mntmAbout.addSelectionListener(menuListener);

        Tree tree = new Tree(shell, SWT.BORDER);
        tree.setBounds(10, 10, 167, 222);

        table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setBounds(183, 10, 241, 104);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tblclmnProperty = new TableColumn(table, SWT.NONE);
        tblclmnProperty.setWidth(127);
        tblclmnProperty.setText("Property");

        TableColumn tblclmnValue = new TableColumn(table, SWT.NONE);
        tblclmnValue.setWidth(108);
        tblclmnValue.setText("Value");

        final TableColumn[] columns = table.getColumns();
        for (int i = 0; i < 4; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            for (int j = 0; j < columns.length; j++) {
                item.setText(j, "Item " + i + ":" + j);
            }
        }

        Combo combo = new Combo(shell, SWT.NONE);
        combo.setBounds(244, 120, 91, 23);

        label = new Label(shell, SWT.NONE);
        label.setBounds(183, 155, 55, 15);
        label.setText("Input");

        text = new Text(shell, SWT.BORDER);
        text.setBounds(244, 149, 91, 21);
        setId(text, "sample.text.input");
        text.setData("customId", "firstName");
        text.setData("BigChunkOfData");
        text.setText("Delete me");
        text.setToolTipText("Please delete me");

        CLabel cLabel = new CLabel(shell, SWT.NONE);
        cLabel.setBounds(183, 180, 55, 15);
        cLabel.setText("cLabel");

        Menu contextMenu = new Menu(label);
        label.setMenu(contextMenu);

        final MenuItem viewMenuItem = new MenuItem(contextMenu, SWT.NONE);
        viewMenuItem.setText("View");

        MenuItem mntmNewSubmenu = new MenuItem(contextMenu, SWT.CASCADE);
        mntmNewSubmenu.setText("Edit...");

        Menu contextMenu_1 = new Menu(mntmNewSubmenu);
        mntmNewSubmenu.setMenu(contextMenu_1);

        final MenuItem cutMenuItem = new MenuItem(contextMenu_1, SWT.NONE);
        cutMenuItem.setText("Cut");

        final MenuItem copyMenuItem = new MenuItem(contextMenu_1, SWT.NONE);
        copyMenuItem.setText("Copy");

        final MenuItem pasteMenuItem = new MenuItem(contextMenu_1, SWT.NONE);
        pasteMenuItem.setText("Paste");

        cutMenuItem.addSelectionListener(menuListener);
        copyMenuItem.addSelectionListener(menuListener);
        pasteMenuItem.addSelectionListener(menuListener);
        viewMenuItem.addSelectionListener(menuListener);

        Label lblSelection = new Label(shell, SWT.NONE);
        lblSelection.setBounds(183, 128, 55, 15);
        lblSelection.setText("Selection");

        checkBox = new Button(shell, SWT.CHECK);
        checkBox.setText("Checkbox 1");
        checkBox.setBounds(183, 207, 75, 25);

        Button btnOk = new Button(shell, SWT.NONE);
        btnOk.setBounds(208, 235, 75, 25);
        btnOk.setText("OK");
        btnOk.addSelectionListener(new BlockingDialogueOpener());

        Button btnCancel = new Button(shell, SWT.NONE);
        btnCancel.setBounds(314, 235, 75, 25);
        btnCancel.setText("Cancel");

    }

    public Shell getShell() {
        return shell;
    }

    private static class BlockingDialogueOpener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent arg0) {
            final Display display = Display.getDefault();

            final Shell dialogShell = new Shell(display, SWT.APPLICATION_MODAL);
            dialogShell.setSize(450, 200);
            dialogShell.setText("Info");
            Button btnOk = new Button(dialogShell, SWT.NONE);
            btnOk.setBounds(35, 35, 75, 25);
            btnOk.setText("OK");

            final AtomicBoolean gialogueCompleted = new AtomicBoolean(false);
            btnOk.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent selectionEvent) {
                    gialogueCompleted.set(true);
                    dialogShell.close();
                    dialogShell.dispose();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                    // empty
                }
            });

            dialogShell.open();

            while (!gialogueCompleted.get() && !dialogShell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent arg0) {
            // empty
        }

    }

    private static class SaveFileNativeDialogue implements SelectionListener {

        private Shell shell;

        public SaveFileNativeDialogue(Shell shell) {
            this.shell = shell;
        }

        @Override
        public void widgetSelected(SelectionEvent event) {
            FileDialog fd = new FileDialog(shell, SWT.SAVE);
            fd.setText("Native Dialog: Save");
            String selected = fd.open();
            File file = new File(selected);
            try {
                file.createNewFile();
                writeToFile(file);
            } catch (IOException ignored) {
                // do not handle
            }
        }

        private void writeToFile(File file) throws IOException {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write("file selected successfully");
                fileWriter.flush();
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent event) {
        }

    }

}
