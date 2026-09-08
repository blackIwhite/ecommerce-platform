package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsTraceDTO implements Serializable {

    private String trackingNo;
    private String logisticsCompany;
    private List<TraceNode> traces;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraceNode implements Serializable {
        private String time;
        private String description;
    }
}
