package com.ericsson.cifwk.taf.scenario;/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.annotations.TestStep;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class TestStepInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(TestStepInterceptor.class);

    @Pointcut("@annotation(com.ericsson.cifwk.taf.annotations.TestStep)")
    public void withStepAnnotation() {
        //pointcut body, should be empty
    }

    @Pointcut("execution(* *(..))")
    public void anyMethod() {
        //pointcut body, should be empty
    }

    @Around("anyMethod() && withStepAnnotation()")
    public Object interceptTestStep(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        TestStep step = methodSignature.getMethod().getAnnotation(TestStep.class);

        StringBuilder stringBuilder = new StringBuilder("Mocking Test Step ");
        stringBuilder.append(step.id());

        if (joinPoint.getArgs().length>0) {
            stringBuilder.append(" with args: ");

            for (Object arg : joinPoint.getArgs()) {
                stringBuilder.append(arg).append(";");
            }
        }

        logger.info(stringBuilder.toString());
        return null;
    }
}
