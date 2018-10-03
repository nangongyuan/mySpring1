/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: YuanDispatcherServlet
 * Author:   Administrator
 * Date:     2018/10/3 0003 18:19
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.yuan.mvcframework.servlet;

import com.yuan.mvcframework.annotaion.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

/**
 * 〈〉
 *
 * @author Administrator
 * @create 2018/10/3 0003
 * @since 1.0.0
 */
@YuanController
public class YuanDispatcherServlet extends HttpServlet {

	private Properties properties = new Properties();

	private List<String> classNames = new ArrayList<String>();

	private Map<String, Object> ioc = new HashMap<>();

	private Map<String, Handler> handlerMapping  = new HashMap<>();
	/** 
	* @Description: 初始化
	* @Param:  
	* @return:  
	* @Author: yuan
	* @Date: 2018/10/3 0003 
	*/ 
	@Override
	public void init(ServletConfig config) throws ServletException {
		//1.加载配置文件
		doLoadConfig(config.getInitParameter("contextConfigLocation"));

		//2.扫描所有相关的类
		doScanner((String) properties.get("scanPackage"));

		//3.将所有相关Class的实例初始化,并且将其保存到map中
		doInstance();

		//4.自动化的依赖注入
		doAutowired();

		//5.初始化HandlerMapping
		initHanderMapping();

		System.out.println("yuan mvc is init");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req,resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doDispatch(req,resp);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void doLoadConfig(String location){
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(location);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (inputStream!=null){
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void doScanner(String packageName){
		URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.","/"));
		File classesDir = new File(url.getFile());
		for (File item : classesDir.listFiles()){
			if (item.isDirectory()){
				doScanner(packageName+"."+item.getName());
			}else{
				classNames.add(packageName + "."+item.getName().replace(".class",""));
			}
		}
	}

	private void doInstance(){
		if (classNames.isEmpty()){
			return;
		}
		for (String item : classNames){
			try {
				Class<?> c = Class.forName(item);

				//判断该类是否需要spring初始化
				if (c.isAnnotationPresent(YuanController.class)){
					// beanName beanId

					String beanName = lowerFirst(c.getSimpleName());
					ioc.put(beanName,c.newInstance());
				}else if (c.isAnnotationPresent(YuanService.class)){
					//1.默认采用类名的首字母
					//2.如果自己定义了名字的话，优先使用自己定义的名字
					//3.根据类型匹配，利用接口作为key
					YuanService service = c.getAnnotation(YuanService.class);
					//获取注解中的beanName
					String beanName = service.value();
					if ("".equals(beanName)){
						beanName = lowerFirst(c.getSimpleName());
					}
					Object instance = c.newInstance();
					ioc.put(beanName,instance);
					//将它实现的接口以接口名放入ioc 这样依赖接口也可以获取实现类
					Class<?>[] interfaces = c.getInterfaces();
					for (Class<?> i : interfaces){
						ioc.put(i.getName(),instance);
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}

	private void doAutowired(){
		if (ioc.isEmpty()){
			return;
		}
		for (Map.Entry<String, Object> entry: ioc.entrySet()){
			//在spring里面没有隐私  获取所有属性变量
			Field[] fields = entry.getValue().getClass().getDeclaredFields();

			for (Field field : fields){
				//有autowried注释
				if (field.isAnnotationPresent(YuanAutowried.class)){
					YuanAutowried autowried = field.getAnnotation(YuanAutowried.class);
					String beanName = autowried.value().trim();
					if ("".equals(beanName)){
						beanName = field.getType().getName();
					}
					field.setAccessible(true);
					try {
						field.set(entry.getValue(),ioc.get(beanName));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void initHanderMapping() {
		if (ioc.isEmpty()){
			return;
		}
		for (Map.Entry<String, Object> entry : ioc.entrySet()){
			Class<?> clazz = entry.getValue().getClass();
			if (clazz.isAnnotationPresent(YuanController.class)){
				String url = "";
				if (clazz.isAnnotationPresent(YuanRequestMapping.class)){
					YuanRequestMapping requestMapping = clazz.getAnnotation(YuanRequestMapping.class);
					url = requestMapping.value();
				}
				Method[] methods = clazz.getMethods();
				for (Method method : methods){
					if (method.isAnnotationPresent(YuanRequestMapping.class)){
						YuanRequestMapping requestMapping = method.getAnnotation(YuanRequestMapping.class);
						String murl = url + requestMapping.value();

						Map<String,Object> paramMap = new LinkedHashMap<>();
						Parameter[] parameters = method.getParameters();
						for (Parameter parameter : parameters){
							String paramName = parameter.getName();
							if (parameter.isAnnotationPresent(YuanRequestParam.class)){
								YuanRequestParam requestParam = parameter.getAnnotation(YuanRequestParam.class);
								if (!requestParam.value().equals("")){
									paramName = requestParam.value();
								}
							}
							paramMap.put(paramName,parameter.getType());
						}
						handlerMapping.put(murl,new Handler(entry.getValue(),method,paramMap));


					}
				}
			}
		}
	}


	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath,"").replaceAll("/+","/");
		//如果没有该Mapping
		if (!handlerMapping.containsKey(url)){
			resp.getWriter().write("404 Not Found!!!");
		}

		Handler handler = handlerMapping.get(url);

		Object[] objects = new Object[handler.getParam().size()];
		int i=0;
		for (Map.Entry<String,Object> entry: handler.getParam().entrySet()){
			Object obj = req.getParameter(entry.getKey());
			if (obj==null){
				if (entry.getValue().equals(HttpServletRequest.class)){
					obj = req;
				}else if (entry.getValue().equals(HttpServletResponse.class)) {
					obj = resp;
				}
			}
			objects[i++]=obj;
		}
		handler.getMethod().invoke(handler.getController(),objects);
	}

	private String lowerFirst(String str){
		char[] chars = str.toCharArray();
		chars[0]+=32;
		return String.valueOf(chars);
	}

	private class Handler{
		private Object controller;
		private Method method;
		private Map<String,Object> param;

		public Handler(Object controller, Method method, Map<String, Object> param) {
			this.controller = controller;
			this.method = method;
			this.param = param;
		}

		public Object getController() {
			return controller;
		}

		public void setController(Object controller) {
			this.controller = controller;
		}

		public Method getMethod() {
			return method;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

		public Map<String, Object> getParam() {
			return param;
		}

		public void setParam(Map<String, Object> param) {
			this.param = param;
		}
	}
}