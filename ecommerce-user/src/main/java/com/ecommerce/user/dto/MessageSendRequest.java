package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Internal request for sending a message to a user (used by other services via Feign).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequest implements Serializable {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Message type is required")
    private Integer type;

    @NotBlank(message = "Message title is required")
    @Size(max = 200)
    private String title;

    private String content;

    private Long referenceId;
}
