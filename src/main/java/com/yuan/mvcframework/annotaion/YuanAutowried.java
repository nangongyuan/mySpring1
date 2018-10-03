/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: YuanAutowried
 * Author:   Administrator
 * Date:     2018/10/3 0003 18:31
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.yuan.mvcframework.annotaion;

import java.lang.annotation.*;

/**
 * 〈〉
 *
 * @author Administrator
 * @create 2018/10/3 0003
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YuanAutowried {
	String value() default "";
}