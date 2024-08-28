package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.operators.ConfigurationAnnoatationScanner;
import com.google.inject.Module;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

import java.util.List;
import java.util.Set;

/**
 * This class is a way to discover all annotated operators in classpath and register them to TestNG Guice module.
 * @see Operator
 */
public class OperatorLookupModuleFactory implements IModuleFactory {

    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {
        return assembleGuiceModule();
    }

    public Module assembleGuiceModule() {
        ConfigurationAnnoatationScanner scanner = new ConfigurationAnnoatationScanner();
        final Set<Class> classes = scanner.findClassesByAnnotation();

        return new TafGuiceModule(classes);
    }

}
