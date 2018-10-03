/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: TestController
 * Author:   Administrator
 * Date:     2018/10/3 0003 18:33
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.yuan.demo.controller;

import com.yuan.demo.service.imp.TestServiceImpl;
import com.yuan.mvcframework.annotaion.YuanAutowried;
import com.yuan.mvcframework.annotaion.YuanController;
import com.yuan.mvcframework.annotaion.YuanRequestMapping;
import com.yuan.mvcframework.annotaion.YuanRequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 〈〉
 *
 * @author Administrator
 * @create 2018/10/3 0003
 * @since 1.0.0
 */
@YuanController
@YuanRequestMapping("/user")
public class TestController {

	@YuanAutowried
	private TestServiceImpl testServiceImpl;

	@YuanRequestMapping("/hello")
	public void test(@YuanRequestParam("name") String name, HttpServletResponse response){
		try {
			response.getWriter().write("your name is "+ name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}