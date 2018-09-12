package pers.robin.summer.web.servlet;

import pers.robin.summer.beans.anotation.Autowired;
import pers.robin.summer.beans.anotation.Service;
import pers.robin.summer.web.anotation.Controller;
import pers.robin.summer.web.anotation.RequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DispatcherServlet extends HttpServlet {

    List<String> classNames = new ArrayList<String>();
    Map<String, Object> beans = new ConcurrentHashMap<String, Object>();
    Map<String, Method> methodMap = new ConcurrentHashMap<String, Method>();

    /**
     * 在tomcat启动过程中初始化bean map
     * @param config
     */
    public void init(ServletConfig config) {
        // 所有待初始化对象class扫描出来
        doScanPackage("pers.robin.summer.demo");

        //1.创建业务bean
        doInstance();

        //2.处理注入
        doAutowired();

        //3.URL映射到方法
        doMapping();
    }

    private void doMapping() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(Controller.class)) {
                RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
                String classPath = mapping.value();

                Method[] methods = clazz.getDeclaredMethods(); //获取类里所有方法
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                        String methodPath = methodMapping.value();
                        String path = classPath + methodPath;
                        methodMap.put(path, method);
                    }
                }
            }
        }
    }

    private void doAutowired() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(Controller.class)) {
                Field[] fields = clazz.getDeclaredFields(); // 拿到类定义的所有属性
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        Autowired auto = field.getAnnotation(Autowired.class);
                        String key = auto.value();
                        Object obj = beans.get(key);
                        // 设置为可访问
                        field.setAccessible(true);
                        try {
                            field.set(instance, obj);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    private void doInstance() {
        // 根据注解创建对象
        for (String className : classNames) {
            String cn = className.replace(".class", "");
            try {
                Class<?> clazz = Class.forName(cn);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // 控制器类
                    Object instance = clazz.newInstance();
                    RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
                    String key = mapping.value();
                    beans.put(key, instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Object instance = clazz.newInstance();
//                    RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
//                    String key = mapping.value();
//                    beans.put(key, instance);
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanPackage(String basePackage) {
        URL url = this.getClass().getResource("/"
                + basePackage.replaceAll("\\.", "/"));
//        System.out.println("basePackage " + url);
        String fileStr = url.getFile();
//        System.out.println("fileStr " + fileStr);
        File file = new File(fileStr);
        String[] filesStr = file.list();
        for (String path : filesStr) {
            File filePath = new File(fileStr + "/" + path);
            if (filePath.isDirectory()) {
                doScanPackage(basePackage + "." + path);
            } else {
                classNames.add(basePackage + "." + filePath.getName());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        String path = uri.replaceAll(context, "");
        Method method = methodMap.get(path);
        Object instance = beans.get("/" + path.split("/")[1]);
        Object[] args = hand(req, resp);
        try {
            method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object[] hand(HttpServletRequest req, HttpServletResponse resp) {
        return null;
    }

}
