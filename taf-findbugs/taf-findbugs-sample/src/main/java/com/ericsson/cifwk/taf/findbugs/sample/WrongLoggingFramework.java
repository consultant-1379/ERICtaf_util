package com.ericsson.cifwk.taf.findbugs.sample;

import java.util.logging.Logger;

public class WrongLoggingFramework {

    Logger julLogger = Logger.getLogger("sample");

    org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger("sample");

    org.slf4j.Logger slf4jLogger;

}
