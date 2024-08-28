package com.ericsson.cifwk.taf.ddc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

    public static final String MESSAGES_PROPERTIES = Messages.class.getPackage().getName() + ".messages";
    public static final ResourceBundle MB = ResourceBundle.getBundle(MESSAGES_PROPERTIES, Locale.getDefault(), Thread.currentThread().getContextClassLoader());

    public static String format(String msg, Object... attr) {
        try {
            msg = MB.getString(msg);
        } catch (MissingResourceException ignore) {
        }
        return MessageFormat.format(msg, attr);
    }

}

