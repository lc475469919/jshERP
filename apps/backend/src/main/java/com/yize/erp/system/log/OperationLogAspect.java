package com.yize.erp.system.log;

import com.yize.erp.system.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLogAspect {
    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            operationLogService.record(logOperation.module(), logOperation.operation(), "SUCCESS", "操作成功");
            return result;
        } catch (Throwable throwable) {
            operationLogService.record(logOperation.module(), logOperation.operation(), "FAILED", throwable.getMessage());
            throw throwable;
        }
    }
}
