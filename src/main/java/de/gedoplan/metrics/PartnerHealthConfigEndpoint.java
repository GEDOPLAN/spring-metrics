package de.gedoplan.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "partnerHealthConfig")
public class PartnerHealthConfigEndpoint {

    @Autowired
    private DummyJsonHealthIndicator dummyJsonHealthIndicator;

    @ReadOperation
    public Map<String, String> getPartnerUrlToCheck(){
        return Map.of(
                "dummyJsonHealthIndicator",dummyJsonHealthIndicator.getPartnerUrl()
        );
    }

    @WriteOperation
    public void setPartnerUrlToCheck(@Selector String partnerId,  String partnerUrl){
        switch (partnerId){
            case "dummyJsonHealthIndicator": dummyJsonHealthIndicator.setPartnerUrl(partnerUrl); break;
        }
    }
}
