package com.example.account.service;

import java.lang.annotation.*;

@Target(ElementType.METHOD) //anotation 붙일 수 있는 target
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AccountLock {
}
