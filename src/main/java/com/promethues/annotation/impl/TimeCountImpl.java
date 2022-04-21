package com.promethues.annotation.impl;

import io.prometheus.client.Summary;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 耗时统计的注解的实现类
 *
 * @author ZYH on 2022/4/21
 */
@Aspect
@Service
public class TimeCountImpl {

    static final Summary METHOD_EXECUTION_TIME  = Summary.build()
            .name("method-execution-time")
            .help("方法的执行耗时")
            // 0.5 quantile (median) with 0.01 allowed error
            .quantile(0.5, 0.01)
            // 0.95 quantile with 0.005 allowed error
            .quantile(0.95, 0.005)
            .register();

    @Pointcut("@annotation(com.promethues.annotation.TimeCount)")
    public void timeCountPointcut() {
    }

    @Around("timeCountPointcut()")
    public void around(ProceedingJoinPoint joinPoint){
        System.out.println("开始统计方法耗时！");
        Summary.Timer startTimer = METHOD_EXECUTION_TIME.startTimer();
        try {
            joinPoint.proceed();
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            System.out.println("统计结束！");
            startTimer.observeDuration();
        }
    }
}
