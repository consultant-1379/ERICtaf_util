package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import org.testng.annotations.Guice;

@Guice(moduleFactory = OperatorLookupModuleFactory.class)
public abstract class TafTestBase {
}
