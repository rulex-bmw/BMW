package com.rulex.dsm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectService {

    public void sayHello(String name) {

        System.err.println("hello " + name);

    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        //通过反射创建ReflectService对象.

        Object service = Class.forName("com.rulex.dsm.ReflectService").newInstance();

        //获取服务方法——sayHello

        Method method = service.getClass().getMethod("sayHello", String.class);

        //反射调用方法.

        method.invoke(service, "rulex");

    }



}
