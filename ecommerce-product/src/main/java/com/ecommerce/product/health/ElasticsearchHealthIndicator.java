package com.ecommerce.product.health;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public Health health() {
        try {
            HealthResponse health = elasticsearchClient.cluster().health();
            String status = health.status().jsonValue();
            if ("red".equals(status)) {
                return Health.down()
                        .withDetail("elasticsearch", "cluster status is red")
                        .build();
            }
            return Health.up()
                    .withDetail("elasticsearch", "available")
                    .withDetail("status", status)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("elasticsearch", "unavailable")
                    .withException(e)
                    .build();
        }
    }
}
