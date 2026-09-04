package com.ecommerce.common.core.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void success_noArgs_shouldReturnDefaultSuccess() {
        Result<Void> result = Result.success();
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ResultCode.SUCCESS.getMessage(), result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void success_withData_shouldIncludeData() {
        Result<String> result = Result.success("hello");
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals("hello", result.getData());
    }

    @Test
    void success_withMessageAndData_shouldIncludeBoth() {
        Result<Integer> result = Result.success("custom msg", 42);
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals("custom msg", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    void fail_withMessage_shouldUseInternalErrorCode() {
        Result<Void> result = Result.fail("error occurred");
        assertEquals(ResultCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals("error occurred", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void fail_withCodeAndMessage_shouldUseBoth() {
        Result<Void> result = Result.fail(9999, "custom fail");
        assertEquals(9999, result.getCode());
        assertEquals("custom fail", result.getMessage());
    }

    @Test
    void fail_withResultCode_shouldUseCodeAndMessage() {
        Result<Void> result = Result.fail(ResultCode.PARAM_ERROR);
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals(ResultCode.PARAM_ERROR.getMessage(), result.getMessage());
    }
}
