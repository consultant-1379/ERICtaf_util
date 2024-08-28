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

import com.ericsson.cifwk.taf.annotations.Attachment;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import ru.yandex.qatools.allure.config.AllureConfig;

import java.nio.charset.Charset;

import static com.ericsson.cifwk.taf.AllureFacade.getInstance;

@Aspect
public class AttachmentAspects {

    @Pointcut("@annotation(com.ericsson.cifwk.taf.annotations.Attachment)")
    public void withAttachmentAnnotation() {
        //pointcut body, should be empty
    }

    @Pointcut("execution(* *(..))")
    public void anyMethod() {
        //pointcut body, should be empty
    }

    @AfterReturning(pointcut = "anyMethod() && withAttachmentAnnotation()", returning = "result")
    public void attachment(JoinPoint joinPoint, Object result) {
        if(result!=null) {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Attachment attachment = methodSignature.getMethod().getAnnotation(Attachment.class);
            String attachTitle = AllureAspectUtils.renderTemplate(
                    attachment.value(),
                    methodSignature.getName(),
                    joinPoint.getThis(),
                    joinPoint.getArgs()
            );

            Charset charset = AllureConfig.newInstance().getAttachmentsEncoding();
            byte[] bytes = (result instanceof byte[]) ? (byte[]) result : result.toString().getBytes(charset);

            // do not add empty attachments
            if (bytes.length != 0) {
                getInstance().addAttachment(attachment.type(), attachTitle, bytes);
            }
        }
    }

}
