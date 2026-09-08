package com.ecommerce.common.core.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ecommerce.common.core.serializer.SensitiveDataSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveDataSerializer.class)
public @interface SensitiveData {
    SensitiveType type() default SensitiveType.PHONE;

    enum SensitiveType {
        PHONE,
        NAME,
        ADDRESS,
        ID_CARD,
        EMAIL
    }
}
