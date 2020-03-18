package com.lagou.edu.utils;

import com.alibaba.druid.util.StringUtils;
import com.lagou.edu.annotation.Autowired;
import com.lagou.edu.annotation.Component;
import com.lagou.edu.annotation.Repository;
import com.lagou.edu.annotation.Service;
import org.dom4j.Element;
import org.reflections.Reflections;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationScannerUtils {

    private static Map<String, Object> map = new HashMap<>();  // 存储对象

    static {
        Reflections ref = new Reflections("com.lagou.edu");

        //任务一：读取解析注解，通过反射技术实例化对象并且存储待用（map集合）
        try {
            //将注解为Component的类读取并存储到map中
            for (Class<?> cl : ref.getTypesAnnotatedWith(Component.class)) {
                Component component = cl.getAnnotation(Component.class);

                Object o = cl.newInstance();

                //注解有value值，id为value值
                if (!StringUtils.isEmpty(component.value())) {
                    map.put(component.value(), o);
                } else {
                    //注解没有value值，id为类名的首字母小写
                    String startWithLowerCaseName = getStartWithLowerCaseName(cl);
                    map.put(startWithLowerCaseName, o);
                }
                //判断类是否实现接口，如果实现接口，则可以按照接口的全限定类名向IoC容器中再放一遍对象，便于@Autowired注入
                Class<?>[] interfaces = cl.getInterfaces();
                if (interfaces.length > 0) {
                    map.put(interfaces[0].getName(), o);
                }
            }

            //将注解为Service的类读取并存储到map中
            for (Class<?> cl : ref.getTypesAnnotatedWith(Service.class)) {
                Service service = cl.getAnnotation(Service.class);
                Object o = cl.newInstance();

                if (!StringUtils.isEmpty(service.value())) {
                    map.put(service.value(), o);
                } else {
                    String startWithLowerCaseName = getStartWithLowerCaseName(cl);
                    map.put(startWithLowerCaseName, o);
                }
                Class<?>[] interfaces = cl.getInterfaces();
                if (interfaces.length > 0) {
                    map.put(interfaces[0].getName(), o);
                }
            }

            //将注解为Repository的类读取并存储到map中
            for (Class<?> cl : ref.getTypesAnnotatedWith(Repository.class)) {
                Repository repository = cl.getAnnotation(Repository.class);
                Object o = cl.newInstance();

                if (!StringUtils.isEmpty(repository.value())) {
                    map.put(repository.value(), o);
                } else {
                    String startWithLowerCaseName = getStartWithLowerCaseName(cl);
                    map.put(startWithLowerCaseName, o);
                }
                Class<?>[] interfaces = cl.getInterfaces();
                if (interfaces.length > 0) {
                    map.put(interfaces[0].getName(), o);
                }
            }

            //根据@Autowired维护依赖关系
            map.forEach((parentId, parentObject) -> findAndProcessAutowiredFields(parentId, parentObject));


        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void findAndProcessAutowiredFields(String parentId, Object parentObject) {
        for (Field f : parentObject.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Autowired.class)) {
                Method[] methods = parentObject.getClass().getMethods();
                for (Method method : methods) {
                    if (method.getName().equalsIgnoreCase("set" + f.getType().getSimpleName())) {
                        try {
                            Object params = map.get(f.getName()) != null ? map.get(f.getName()) : map.get(f.getType().getName());
                            method.invoke(parentObject, params);
                            // 把处理之后的parentObject重新放到map中
                            map.put(parentId, parentObject);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    private static String getStartWithLowerCaseName(Class<?> cl) {
        char c[] = cl.getSimpleName().toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static Object getBean(String id) {
        return map.get(id);
    }

}