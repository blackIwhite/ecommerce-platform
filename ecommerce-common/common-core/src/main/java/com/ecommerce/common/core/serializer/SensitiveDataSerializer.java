package com.ecommerce.common.core.serializer;

import com.ecommerce.common.core.annotation.SensitiveData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

public class SensitiveDataSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private static final ThreadLocal<Boolean> SHOW_SENSITIVE = new ThreadLocal<>();

    private SensitiveData.SensitiveType type;

    public SensitiveDataSerializer() {
    }

    public SensitiveDataSerializer(SensitiveData.SensitiveType type) {
        this.type = type;
    }

    public static void setShowSensitive(boolean show) {
        if (show) {
            SHOW_SENSITIVE.set(true);
        } else {
            SHOW_SENSITIVE.remove();
        }
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if (Boolean.TRUE.equals(SHOW_SENSITIVE.get())) {
            gen.writeString(value);
            return;
        }

        gen.writeString(mask(value, type));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            SensitiveData annotation = property.getAnnotation(SensitiveData.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(SensitiveData.class);
            }
            if (annotation != null) {
                return new SensitiveDataSerializer(annotation.type());
            }
        }
        return this;
    }

    private String mask(String value, SensitiveData.SensitiveType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (type == null) {
            type = SensitiveData.SensitiveType.PHONE;
        }

        return switch (type) {
            case PHONE -> maskPhone(value);
            case NAME -> maskName(value);
            case ADDRESS -> maskAddress(value);
            case ID_CARD -> maskIdCard(value);
            case EMAIL -> maskEmail(value);
        };
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskName(String name) {
        if (name.length() <= 1) return name;
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    private String maskAddress(String address) {
        if (address.length() <= 6) return address;
        return address.substring(0, 6) + "****";
    }

    private String maskIdCard(String idCard) {
        if (idCard.length() < 8) return idCard;
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
