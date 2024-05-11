package com.hjh.bibackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)//表示该注解在运行时仍然可用
public @interface RoleCheck {

    /**
     * 有任何一个角色
     * */
    String[] anyRole() default "";

    /**
     * 必须有某个角色才能通过
     * */
    String mustRole() default "";
}
