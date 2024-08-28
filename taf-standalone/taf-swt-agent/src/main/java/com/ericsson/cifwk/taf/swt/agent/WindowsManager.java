package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Throwables;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WindowsManager {

    public static List<String> listWindows() {
        try {
            List<String> windows = new ArrayList<String>();
            List<Display> displays;
            displays = getDisplays();
            for (Display display : displays) {
                try {
                    windows.addAll(listWindows(display));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return windows;
        } catch (NoSuchFieldException e) {
            throw Throwables.propagate(e);
        } catch (SecurityException e) {
            throw Throwables.propagate(e);
        } catch (IllegalArgumentException e) {
            throw Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    public static SWTBot findWindow(String windowTitle) {
        try {
            List<Display> displays = getDisplays();
            for (Display display : displays) {
                try {
                    Shell shell = findWindow(windowTitle, display);
                    if (shell != null) {
                        return new SWTBot(shell);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        } catch (NoSuchFieldException e) {
            throw Throwables.propagate(e);
        } catch (SecurityException e) {
            throw Throwables.propagate(e);
        } catch (IllegalArgumentException e) {
            throw Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    private static Shell findWindow(final String windowTitle, final Display display) throws InterruptedException {
        final AtomicReference<Shell> shellHolder = new AtomicReference<Shell>(null);
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                Shell[] shells = display.getShells();
                for (Shell shell : shells) {
                    if (shell.getText().equals(windowTitle)) {
                        shellHolder.set(shell);
                    }
                }
            }
        });
        return shellHolder.get();
    }

    private static List<String> listWindows(final Display display) throws InterruptedException {
        final List<String> windows = new CopyOnWriteArrayList<String>();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                Shell[] shells = display.getShells();
                for (Shell shell : shells) {
                    windows.add(shell.getText());
                }
            }
        });
        return windows;
    }

    private static List<Display> getDisplays() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        List<Display> displays = new ArrayList<Display>();
        for (Display aDisplay : getDisplaysField()) {
            if (aDisplay != null) {
                displays.add(aDisplay);
            }
        }
        return displays;
    }

    private static Display[] getDisplaysField() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field displaysField = Display.class.getDeclaredField("Displays");
        displaysField.setAccessible(true);
        return (Display[]) displaysField.get(Display.class);
    }

}
