package com.damburisoft.android.yamalocationsrv.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Key {
    /**
     * Override the data key name of the field or {@code "##default"} to use the Java field's name.
     */
    String value() default "##default";
}
