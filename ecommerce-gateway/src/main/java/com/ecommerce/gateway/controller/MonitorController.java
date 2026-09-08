package com.ecommerce.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final DiscoveryClient discoveryClient;
    private final WebClient webClient = WebClient.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(256 * 1024))
            .build();

    private static final Map<String, String> SERVICE_DISPLAY_NAMES = Map.of(
            "ecommerce-auth", "认证服务",
            "ecommerce-product", "商品服务",
            "ecommerce-user", "用户服务",
            "ecommerce-order", "订单服务",
            "ecommerce-inventory", "库存服务",
            "ecommerce-marketing", "营销服务",
            "ecommerce-aftersales", "售后服务",
            "ecommerce-gateway", "API网关"
    );

    private static final List<String> MONITORED_SERVICES = List.of(
            "ecommerce-gateway", "ecommerce-auth", "ecommerce-product",
            "ecommerce-user", "ecommerce-order", "ecommerce-inventory",
            "ecommerce-marketing", "ecommerce-aftersales"
    );

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        return Flux.fromIterable(MONITORED_SERVICES)
                .flatMap(this::checkService)
                .collectList()
                .map(data -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("code", 200);
                    result.put("message", "success");
                    result.put("data", data);
                    return result;
                });
    }

    private Mono<Map<String, Object>> checkService(String serviceName) {
        if ("ecommerce-gateway".equals(serviceName)) {
            return webClient.get()
                    .uri("http://localhost:8080/actuator/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .map(body -> buildResult(serviceName, body))
                    .onErrorResume(e -> Mono.just(buildResult(serviceName, "DOWN", null, null)));
        }

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            return Mono.just(buildResult(serviceName, "UNKNOWN", null, null));
        }

        ServiceInstance instance = instances.get(0);
        String url = String.format("http://%s:%d/actuator/health", instance.getHost(), instance.getPort());

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .map(body -> buildResult(serviceName, body))
                .onErrorResume(e -> Mono.just(buildResult(serviceName, "DOWN",
                        instance.getHost(), instance.getPort())));
    }

    private Map<String, Object> buildResult(String serviceName, Map<?, ?> healthBody) {
        String status = healthBody != null ? String.valueOf(healthBody.get("status")) : "UNKNOWN";
        String host = null;
        Integer port = null;

        if (!"ecommerce-gateway".equals(serviceName)) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (!instances.isEmpty()) {
                ServiceInstance inst = instances.get(0);
                host = inst.getHost();
                port = inst.getPort();
            }
        } else {
            host = "localhost";
            port = 8080;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", serviceName);
        result.put("displayName", SERVICE_DISPLAY_NAMES.getOrDefault(serviceName, serviceName));
        result.put("status", status);
        result.put("host", host);
        result.put("port", port);
        return result;
    }

    private Map<String, Object> buildResult(String serviceName, String status, String host, Integer port) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", serviceName);
        result.put("displayName", SERVICE_DISPLAY_NAMES.getOrDefault(serviceName, serviceName));
        result.put("status", status);
        result.put("host", host);
        result.put("port", port);
        return result;
    }
}
