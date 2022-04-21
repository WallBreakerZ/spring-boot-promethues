package com.promethues.rest;

import com.promethues.annotation.TimeConsuming;
import com.promethues.metrics.JobMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CounterController {
    @Autowired
    private JobMetrics jobMetrics;

    @RequestMapping(value = "/counter1", method = RequestMethod.GET)
    public void counter1() {
        jobMetrics.job2Counter.increment();
    }

    @RequestMapping(value = "/counter2", method = RequestMethod.GET)
    public void counter2() {
        jobMetrics.job2Counter.increment();
    }

    @RequestMapping(value = "/gauge", method = RequestMethod.GET)
    public void gauge(@RequestParam(value = "x") String x) {
        System.out.println("gauge controller x" + x);
        jobMetrics.map.put("x", Double.valueOf(x));
    }

    @RequestMapping(value = "/cost1", method = RequestMethod.GET)
    @TimeConsuming("cost1")
    public int cost() {
        try {
            test();
            method(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @RequestMapping(value = "/cost2", method = RequestMethod.GET)
//    @TimeConsuming("cost2")
    public int cost2() {
        try {
            test();
            method(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void method(int i) throws InterruptedException {
        double l = Math.random() * i * 0.01;
        System.out.println("睡" + l * 10 + "秒");
        Thread.sleep((long) (l * 1000));
    }

    @TimeConsuming("test")
    public void test() throws InterruptedException {
        Thread.sleep(10);
    }
}

