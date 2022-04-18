package com.promethues;

public class Main {

    @MyAnnotation("我的值")
    public void myMethod(){
    }
    @MyAnnotation(value = "我的值",code = 1)
    public void myMethod1(){
    }
    public static void main(String[] args) {

    }
}
