package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;

import java.lang.reflect.Method;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
@API(Internal)
public interface TafAnnotationManagerFactory {

    TafAnnotationManager create(TestMethodExecutionResult testMethodExecutionResult);

    TafAnnotationManager create(Method method, Object[] parameters);

}
