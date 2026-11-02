package com.logitrack.eureka;

import com.netflix.discovery.EurekaClientConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurekaRegistryTest {
    @Autowired TestRestTemplate http;
    @Autowired EurekaClientConfig config;

    @Test void exposesDashboardAndHealthWithoutSelfRegistration() {
        assertThat(http.getForEntity("/", String.class).getStatusCode().value()).isEqualTo(200);
        assertThat(http.getForEntity("/actuator/health", String.class).getBody()).contains("UP");
        assertThat(config.shouldRegisterWithEureka()).isFalse();
        assertThat(config.shouldFetchRegistry()).isFalse();
    }

    @Test void registersRenewsAndRemovesAnInstanceThroughTheRealApi() {
        var instance = Map.of("instance", Map.of(
                "instanceId", "courier-fixture", "hostName", "courier-service",
                "app", "COURIER-SERVICE", "ipAddr", "127.0.0.1", "status", "UP",
                "port", Map.of("$", 8082, "@enabled", "true"),
                "dataCenterInfo", Map.of("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo", "name", "MyOwn")));
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assertThat(http.postForEntity("/eureka/apps/COURIER-SERVICE", new HttpEntity<>(instance, headers), String.class)
                .getStatusCode().value()).isEqualTo(204);
        try {
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            var found = http.exchange("/eureka/apps/COURIER-SERVICE/courier-fixture", HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);
            assertThat(found.getStatusCode().value()).isEqualTo(200);
            assertThat(found.getBody()).contains("courier-fixture", "COURIER-SERVICE");
            assertThat(http.exchange("/eureka/apps/COURIER-SERVICE/courier-fixture", HttpMethod.PUT,
                    HttpEntity.EMPTY, String.class).getStatusCode().value()).isEqualTo(200);
        } finally {
            http.delete("/eureka/apps/COURIER-SERVICE/courier-fixture");
        }
        assertThat(http.getForEntity("/eureka/apps/COURIER-SERVICE/courier-fixture", String.class)
                .getStatusCode().value()).isEqualTo(404);
    }
}
