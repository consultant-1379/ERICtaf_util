package com.ericsson.cifwk.taf.swt.agent;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class BundleTest extends AbstractPaxTest {

    @Test
    public void testBundleActivated() throws BundleException, InterruptedException {
        assertEquals(Bundle.ACTIVE, getBundle(SWT_AGENT_BUNDLE_ID).getState());
        assertEquals(Bundle.ACTIVE, getBundle(SWT_SAMPLE_APP_BUNDLE_ID).getState());
    }

}
