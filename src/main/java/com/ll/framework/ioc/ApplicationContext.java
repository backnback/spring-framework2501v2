package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.framework.ioc.annotations.Configuration;
import com.ll.framework.ioc.annotations.Repository;
import com.ll.framework.ioc.annotations.Service;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContext {

    private final Map<String, Object> dependencyMap = new HashMap<>();
    private final String packageName;
    private final List<Class<?>> classes = new ArrayList<>();

    public ApplicationContext(String packageName) {
        this.packageName = packageName;
    }

    public void init() {
        try {
            File dir = new File("build/classes/java/test/" + packageName.replace(".", "/"));
            searchClasses(dir, packageName);
            createBeans();

        } catch (Exception e) {
            System.out.println("객체 준비 중 오류 발생!");
            e.printStackTrace();
        }
    }

    public <T> T genBean(String beanName) {
        return (T) dependencyMap.get(beanName);
    }


    private void searchClasses(File dir, String packageName) throws Exception {
        System.out.println(dir.getAbsolutePath());
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        if (!packageName.isEmpty()) {
            packageName += ".";
        }

        for (File file : files) {
            if (file.isDirectory()) {
                searchClasses(file, packageName + file.getName());
            } else {
                String className = packageName + file.getName().replace(".class", "");

                Class<?> clazz = Class.forName(className);
                if (checkAnnotation(clazz)) {
                    classes.add(clazz);
                }
            }
        }
    }


    private boolean checkAnnotation(Class<?> clazz) {
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Component.class)
                || annotation.annotationType().isAnnotationPresent(Service.class)
                || annotation.annotationType().isAnnotationPresent(Repository.class)
                || annotation.annotationType().isAnnotationPresent(Configuration.class)) {
                return true;
            }
        }
        return false;
    }


    private void createBeans() throws Exception {
        for (Class<?> clazz : classes) {
            createObject(clazz);
        }
    }

    private String getBeanName(Class<?> clazz) throws Exception {
        return clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
    }

    private <T> T createObject(Class<?> clazz) throws Exception {
        String className = getBeanName(clazz);
        if (dependencyMap.containsKey(className)) {
            return (T) dependencyMap.get(className);
        }

        Constructor<?> constructor = clazz.getConstructors()[0];

        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = prepareArguments(paramTypes);

        dependencyMap.put(className, constructor.newInstance(args));
        System.out.println("빈 생성 : " + className + " -> " + dependencyMap.get(className));
        return (T) dependencyMap.get(className);
    }


    private Object[] prepareArguments(Class<?>[] paramTypes) throws Exception {
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = createObject(paramTypes[i]);
        }
        return args;
    }

}
