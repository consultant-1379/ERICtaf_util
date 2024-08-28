package com.ericsson.cifwk.taf;

import ru.yandex.qatools.allure.Allure;

/**
 *
 */
public class AllureProvider {

    static Allure instance;

    public static Allure singletone() {
        if (instance == null) {
            return Allure.LIFECYCLE;
        }
        return instance;
    }

}
