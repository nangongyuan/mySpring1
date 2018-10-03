package com.yuan.mvcframework.annotaion;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YuanRequestMapping {
	String value() default "";
}
