package de.gedoplan.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class DummyJsonHealthIndicator implements HealthIndicator {

    @Autowired
    RestTemplate restTemplate;

    private String partnerUrl = "https://dummyjson.com/users";

    @Override
    public Health health() {
        try {
            restTemplate.optionsForAllow(URI.create(partnerUrl));
            return Health.up().build();
        }catch (Exception e) {
            return Health.down(e).withDetail("reason", "dummyjson.com not reachable").build();
        }
    }

    public String getPartnerUrl() {
        return partnerUrl;
    }

    public void setPartnerUrl(String partnerUrl) {
        this.partnerUrl = partnerUrl;
    }
}
