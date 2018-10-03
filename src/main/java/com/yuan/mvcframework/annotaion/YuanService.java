package com.yuan.mvcframework.annotaion;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YuanService {
	String value() default "";
}
