package com.ecommerce.common.core.constant;

/**
 * Common application constants.
 */
public final class CommonConstants {

    private CommonConstants() {
    }

    // ---- Authentication ----

    /** JWT token prefix in Authorization header. */
    public static final String JWT_PREFIX = "Bearer ";

    /** HTTP header name for authorization. */
    public static final String AUTH_HEADER = "Authorization";

    /** HTTP header name for internal service calls. */
    public static final String INNER_HEADER = "X-Inner-Request";

    // ---- User context ----

    /** Request attribute / header key for current user id. */
    public static final String USER_ID_HEADER = "X-User-Id";

    /** Request attribute / header key for current username. */
    public static final String USERNAME_HEADER = "X-Username";

    /** Request attribute / header key for current user phone. */
    public static final String PHONE_HEADER = "X-User-Phone";

    // ---- Common values ----

    /** Logical delete: not deleted. */
    public static final int NOT_DELETED = 0;

    /** Logical delete: deleted. */
    public static final int DELETED = 1;

    /** Enabled status. */
    public static final int STATUS_ENABLED = 1;

    /** Disabled status. */
    public static final int STATUS_DISABLED = 0;
}
