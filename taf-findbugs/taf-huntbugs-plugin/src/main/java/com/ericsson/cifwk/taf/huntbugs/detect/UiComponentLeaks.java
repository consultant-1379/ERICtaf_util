package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.IGenericInstance;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.warning.Role.MemberRole;

import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isPublic;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isUiComponent;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isViewModel;
import static one.util.huntbugs.util.Types.isCollection;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 26.08.2016
 */
@WarningDefinition(category = "TAF UI", name = "UiComponentLeak", maxScore = 50)
public class UiComponentLeaks {

    private static final MemberRole CALLED_METHOD = MemberRole.forName("CALLED_METHOD");

    @MethodVisitor
    public void visit(MethodContext mc, MethodDefinition md, TypeDefinition td) {

        // we are interested in View Models only
        if (!isViewModel(td)) {
            return;
        }

        // we are interested only in public methods
        if (!isPublic(md)) {
            return;
        }

        // WARNING: UI component is method return type
        TypeReference returnType = md.getReturnType();
        if (isUiComponent(returnType)) {
            mc.report("UiComponentLeak", 5);
        }

        // WARNING: collection of UI components is method return type
        if (isCollection(returnType) && returnType instanceof IGenericInstance) {
            IGenericInstance genericReturnType = (IGenericInstance) returnType;
            TypeReference collectionParameter = genericReturnType.getTypeArguments().get(0);
            if (isUiComponent(collectionParameter)) {
                mc.report("UiComponentLeak", 5, CALLED_METHOD.create(md));
            }
        }
    }

}
