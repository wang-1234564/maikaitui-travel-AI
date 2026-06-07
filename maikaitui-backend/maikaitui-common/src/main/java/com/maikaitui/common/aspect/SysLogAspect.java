package com.maikaitui.common.aspect;

import com.maikaitui.common.annotation.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class SysLogAspect {

    @Around("@annotation(sysLog)")
    public Object around(ProceedingJoinPoint point, SysLog sysLog) throws Throwable {
        String methodName = point.getSignature().toShortString();
        Object[] args = point.getArgs();
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        long time = System.currentTimeMillis() - start;
        log.info("SysLog [{}] {} params={} result={} time={}ms",
                sysLog.value(), methodName, Arrays.toString(args), result, time);
        return result;
    }
}
