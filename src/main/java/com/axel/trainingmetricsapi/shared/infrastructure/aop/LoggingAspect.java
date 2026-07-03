package com.axel.trainingmetricsapi.shared.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.axel.trainingmetricsapi.*.application.*UseCaseImpl.execute(..))")
    public Object logUseCaseExecution(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = proceedingJoinPoint.getArgs();

        String className = signature.getDeclaringType().getSimpleName();
        String readableName = className.replace("UseCaseImpl", "")
            .replaceAll("([A-Z])", " $1").trim();

        StringBuilder argsLog = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) argsLog.append(" ");
            String paramName = parameters[i].getName();
            if (parameters[i].isAnnotationPresent(SensitiveArg.class)) {
                argsLog.append(paramName).append("=[REDACTED]");
            } else {
                argsLog.append(paramName).append("=").append(args[i]);
            }
        }

        log.info("Executing {} {}", readableName, argsLog);

        try {
            Object result = proceedingJoinPoint.proceed();
            if (className.contains("Create")) {
                log.info("{} created successfully", readableName);
            } else if (className.contains("Delete")) {
                log.info("{} deleted successfully", readableName);
            } else if (className.contains("Update")) {
                log.info("{} updated successfully", readableName);
            }
            return result;
        } catch (Throwable t) {
            if (t.getClass().getPackageName().contains("domain.exception")) {
                log.warn("Business error in {}: {}", readableName, t.getMessage()); // clear readable message
            } else {
                log.error("Unexpected error in {}", readableName, t); // unexpected error : complete stack trace
            }
            throw t;
        }
    }

}
