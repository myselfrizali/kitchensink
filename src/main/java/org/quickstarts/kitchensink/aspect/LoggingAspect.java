package org.quickstarts.kitchensink.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* org.quickstarts.kitchensink.service.*.*(..))")
    public void serviceMethods() {}

    @Pointcut("execution(* org.quickstarts.kitchensink.controller.*.*(..))")
    public void controllerMethods() {}

    @Before("serviceMethods()")
    public void logBeforeService(JoinPoint joinPoint) {
        log.info("Executing service method: {}", joinPoint.getSignature().getName());
    }

    @After("serviceMethods()")
    public void logAfterService(JoinPoint joinPoint) {
        log.info("Executed service method: {}", joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "exception")
    public void logAfterThrowingService(JoinPoint joinPoint, Throwable exception) {
        log.error("Service method {} threw exception: {}", joinPoint.getSignature().getName(), exception);
    }

    @Before("controllerMethods()")
    public void logBeforeController(JoinPoint joinPoint) {
        log.info("Executing controller method: {}", joinPoint.getSignature().getName());
    }

    @After("controllerMethods()")
    public void logAfterController(JoinPoint joinPoint) {
        log.info("Executed controller method: {}", joinPoint.getSignature().getName());
    }

    // AfterThrowing advice for controller methods
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logAfterThrowingController(JoinPoint joinPoint, Throwable exception) {
        log.error("Controller method {} threw exception: {}", joinPoint.getSignature().getName(), exception);
    }
}
