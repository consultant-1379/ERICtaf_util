package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.decompiler.ast.Block;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isTestStepMethod;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 27.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "TestStepSize", maxScore = 50)
public class TestStepSize {

    private static final int MIN_LINES = 2;

    private static final int MAX_LINES = 20;

    @AstVisitor(nodes = AstNodes.ROOT)
    public void visitMethod(Block block, MethodDefinition md, MethodContext mc) {

        // interested in test step methods only
        if(!isTestStepMethod(md)) {
            return;
        }

        // checking method size
        int methodCodeLinesSize = block.getChildren().size();
        if (methodCodeLinesSize < MIN_LINES || methodCodeLinesSize > MAX_LINES) {
            mc.report("TestStepSize", 20);
        }
    }

}
