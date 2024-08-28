package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.core.UiComponentSize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtControl extends SwtUiComponent {

    private AbstractSWTBotControl<?> target;

    public SwtControl(AbstractSWTBotControl<?> target) {
        super(target);
        this.target = target;
    }

    @Override
    public void click() {
        try {
            // trying to call click() on target object
            UIThreadRunnable.syncExec(target.widget.getDisplay(), new ReflectionInvoker(target, "click"));
        } catch (Exception e) {
            // from abstract SWT bot
            try {
                target.setFocus();
            } catch (Exception ignored){
                // OK
            }
            Rectangle location = absoluteLocation();
            click(location.x + location.width / 2, location.y + location.height / 2);
        }
    }

    @Override
    public void mouseOver(int... coordinates) {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                Rectangle location = absoluteLocation();
                moveMouse(location.x + location.width / 2, location.y + location.height / 2);
            }
        });
    }

    @Override
    public void mouseDown(int... coordinates) {
        final int x = coordinates[0];
        final int y = coordinates[1];
        final int button = coordinates[2];
        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                Event event = createMouseEvent(x, y, button, 0, 0);
                event.type = SWT.MouseDown;
                target.display.post(event);
            }
        });
    }

    @Override
    public void mouseUp(int... coordinates) {
        final int x = coordinates[0];
        final int y = coordinates[1];
        final int button = coordinates[2];
        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                Event event = createMouseEvent(x, y, button, 0, 0);
                event.type = SWT.MouseUp;
                target.display.post(event);
            }
        });
    }

    @Override
    public UiComponentSize getSize() {
        return UIThreadRunnable.syncExec(target.display, new Result<UiComponentSize>() {
            @Override
            public UiComponentSize run() {
                Rectangle bounds = target.widget.getBounds();
                return new UiComponentSize(bounds.width, bounds.height);
            }
        });
    }

    protected Rectangle absoluteLocation() {
        return UIThreadRunnable.syncExec(new Result<Rectangle>() {
            @Override
            public Rectangle run() {
                return target.display.map(target.widget.getParent(), null, target.widget.getBounds());
            }
        });
    }

    protected void moveMouse(final int x, final int y) {
        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                Event event = createMouseEvent(x, y, 0, 0, 0);
                event.type = SWT.MouseMove;
                target.display.post(event);
            }
        });
    }

    protected Event createMouseEvent(int x, int y, int button, int stateMask, int count) {
        Event event = new Event();
        event.time = (int) System.currentTimeMillis();
        event.widget = target.widget;
        event.display = target.display;
        event.x = x;
        event.y = y;
        event.button = button;
        event.stateMask = stateMask;
        event.count = count;
        return event;
    }

    protected void click(final int x, final int y) {
        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                moveMouse(x, y);
                mouseDown(x, y, 1);
                mouseUp(x, y, 1);
            }
        });
        SWTUtils.sleep(500);
    }

}
