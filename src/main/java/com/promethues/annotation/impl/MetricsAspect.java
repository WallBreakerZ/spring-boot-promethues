package com.promethues.annotation.impl;

import com.promethues.annotation.TimeConsuming;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 监控 metrics 管理
 * 统一处理Prometheus中的监控指标
 *
 * @author ZYH on 2022/4/21
 */
@Aspect
@Component
public class MetricsAspect {

    @Autowired
    CollectorRegistry collectorRegistry;
    Summary.Timer startTimer;
    static Summary METHOD_EXECUTION_TIME_SUMMARY;
    private static final String[] method_execution_time_tags = new String[]{"method"};

    @PostConstruct
    public void init() {
        METHOD_EXECUTION_TIME_SUMMARY = Summary.build()
                .name("method_execution_time")
                .labelNames(method_execution_time_tags)
                .help("方法的执行耗时")
                .quantile(0.5, 0.05)
                .quantile(0.75, 0.02)
                // 0.95 quantile with 0.005 allowed error
                .quantile(0.95, 0.01)
                .quantile(0.99, 0.01)
                .register(collectorRegistry);
    }

    @Pointcut("@annotation(com.promethues.annotation.TimeConsuming)")
    public void timeConsumingPointcut() {
    }

//    @Before("timeConsumingPointcut() && @annotation(timeConsuming)")
//    public void before(TimeConsuming timeConsuming) {
//        startTimer = METHOD_EXECUTION_TIME_SUMMARY.labels(timeConsuming.value()).startTimer();
//    }
//
//    @After("timeConsumingPointcut() && @annotation(timeConsuming)")
//    public void after(TimeConsuming timeConsuming) {
//        startTimer.observeDuration();
//    }

    @Around("timeConsumingPointcut() && @annotation(timeConsuming)")
    public Object around(ProceedingJoinPoint joinPoint, TimeConsuming timeConsuming) {
        Object result = null;
        Summary.Timer startTimer = METHOD_EXECUTION_TIME_SUMMARY.labels(timeConsuming.value()).startTimer();
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            startTimer.observeDuration();
        }
        return result;
    }


}
