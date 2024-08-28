package com.ericsson.cifwk.taf.logging;

import com.ericsson.cifwk.taf.AllureProvider;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;

public class Attachments {

    private Attachments() {
        // hiding constructor
    }

    public static void addLogAttachment() {
        byte[] attachment = TestStepLogs.removeLog();
        if (attachment.length != 0) {
            Allure allure = AllureProvider.singletone();
            allure.fire(new MakeAttachmentEvent(attachment, "log.txt", "text/plain"));
        }
    }
}
