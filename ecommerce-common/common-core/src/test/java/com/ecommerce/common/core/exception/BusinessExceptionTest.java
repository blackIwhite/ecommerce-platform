package com.ecommerce.common.core.exception;

import com.ecommerce.common.core.result.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructorWithMessage_shouldSetMessageAndDefaultCode() {
        BusinessException ex = new BusinessException("something went wrong");
        assertEquals("something went wrong", ex.getMessage());
        assertEquals(ResultCode.FAIL.getCode(), ex.getCode());
    }

    @Test
    void constructorWithCodeAndMessage_shouldSetBoth() {
        BusinessException ex = new BusinessException(1234, "custom error");
        assertEquals(1234, ex.getCode());
        assertEquals("custom error", ex.getMessage());
    }

    @Test
    void constructorWithResultCode_shouldUseCodeAndMessage() {
        BusinessException ex = new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
        assertEquals(ResultCode.INVENTORY_NOT_ENOUGH.getCode(), ex.getCode());
        assertEquals(ResultCode.INVENTORY_NOT_ENOUGH.getMessage(), ex.getMessage());
    }

    @Test
    void constructorWithResultCodeAndDetail_shouldAppendDetail() {
        BusinessException ex = new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH, "SKU: 100");
        assertEquals(ResultCode.INVENTORY_NOT_ENOUGH.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("SKU: 100"));
        assertTrue(ex.getMessage().contains(ResultCode.INVENTORY_NOT_ENOUGH.getMessage()));
    }

    @Test
    void shouldBeRuntimeException() {
        BusinessException ex = new BusinessException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
