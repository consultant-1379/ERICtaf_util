/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.aspects;

import static com.ericsson.cifwk.taf.aspects.AllureAspectUtils.renderTemplate;

import com.ericsson.cifwk.taf.AllureProvider;
import com.ericsson.cifwk.taf.ParametersManager;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.logging.Attachments;
import com.ericsson.cifwk.taf.logging.TestStepLogs;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.events.StepFailureEvent;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;

@SuppressWarnings("unused")
@Aspect
public class TestStepAspects {

    private static final Logger LOG = LoggerFactory.getLogger(TestStepAspects.class);

    @Pointcut("@annotation(com.ericsson.cifwk.taf.annotations.TestStep)")
    public void withStepAnnotation() {
        //pointcut body, should be empty
    }

    @Pointcut("execution(* *(..))")
    public void anyMethod() {
        //pointcut body, should be empty
    }

    @Before("anyMethod() && withStepAnnotation()")
    public void stepStart(JoinPoint joinPoint) {
        String stepTitle = getTestStepTitle(joinPoint);

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        StepStartedEvent startedEvent = new StepStartedEvent(methodSignature.getName());

        if (!stepTitle.isEmpty()) {
            startedEvent.setTitle(stepTitle);
        }

        AllureProvider.singletone().fire(startedEvent);
        TestStepLogs.addLog();
    }

    @AfterThrowing(pointcut = "anyMethod() && withStepAnnotation()", throwing = "e")
    public void stepFailed(JoinPoint joinPoint, Throwable e) {
        finishTestStep(e);

        AllureProvider.singletone().fire(new StepFailureEvent().withThrowable(e));
        AllureProvider.singletone().fire(new StepFinishedEvent());
    }

    @AfterReturning(pointcut = "anyMethod() && withStepAnnotation()", returning = "result")
    public void stepStop(JoinPoint joinPoint, Object result) {
        finishTestStep();

        AllureProvider.singletone().fire(new StepFinishedEvent());
    }

    private void finishTestStep(Throwable e) {
        finishTestStep();
    }

    private void finishTestStep() {
        Attachments.addLogAttachment();
    }

    private String getTestStepTitle(JoinPoint joinPoint) {

        // getting according annotation
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        TestStep step = methodSignature.getMethod().getAnnotation(TestStep.class);
        if (step == null) {
            return "";
        }

        // processing template
        String methodName = methodSignature.getName();
        String stepId = step.id();
        String titleTemplate = step.description();
        if (!methodName.equals(stepId)) {
            titleTemplate = stepId + ": " + titleTemplate;
        }
        Object[] parameters = joinPoint.getArgs();
        String title = renderTemplate(titleTemplate, methodName, joinPoint.getThis(), parameters);

        return ParametersManager.getParametrizedName(title, parameters);
    }
}
