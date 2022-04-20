package com.test;

public class UseThreadLocal {

    public static ThreadLocal<Object> threadLocal = new ThreadLocal<Object>();

    /**
     * 运行三个线程
     */
    public void StartThreadArray(){
        threadLocal.set(new Object());
        Thread[] runs = new Thread[3];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = new Thread(new TestThreadLocal(i));
        }
        for (int i = 0; i < runs.length; i++) {
            runs[i].start();
        }
    }

    public static class TestThreadLocal implements Runnable{
        int id;
        public TestThreadLocal(int id){
            this.id = id;
        }
        @Override
        public void run() {
//            System.out.println(Thread.currentThread().getName()+":start");
            Object str = threadLocal.get();
//            threadLocal.set(str+id);
//            System.out.println(Thread.currentThread().getName()+" "+str.getBytes());
//            System.out.println(Thread.currentThread().getName()+" "+str.hashCode());
            System.out.println(Thread.currentThread().getName()+" "+threadLocal.get());
        }
    }

    public static void main(String[] args) {
        UseThreadLocal test = new UseThreadLocal();
        test.StartThreadArray();
        System.out.println(Thread.currentThread().getName()+" "+threadLocal.get());
    }

}