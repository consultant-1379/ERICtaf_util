package com.ericsson.cifwk.taf.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.ba.ObjectTypeFactory;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 * Checks use of logging APIs other than Slf4j
 */
public final class WrongLoggingFramework extends BytecodeScanningDetector {

    private static final String BUG_TYPE = "WRONG_LOG";

    private final BugReporter reporter;

    public WrongLoggingFramework(BugReporter reporter) {
        this.reporter = reporter;
    }

    ObjectType julType = ObjectTypeFactory.getInstance("java.util.logging.Logger");

    @Override
    public void visit(Field field) {
        super.visit(field);
        Type type = field.getType();

        if (!(type instanceof ObjectType)) return;

        ObjectType objectType = (ObjectType) type;

        if (isBannedLoggingFramework(objectType)) {
            BugInstance bug = new BugInstance(this, BUG_TYPE, HIGH_PRIORITY)
                    .addClass(this)
                    .addField(this);
            reporter.reportBug(bug);
        }
    }

    private boolean isBannedLoggingFramework(ObjectType objectType) {
        try {
            return objectType.getClassName().equals("org.apache.log4j.Logger") || objectType.subclassOf(julType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

}
