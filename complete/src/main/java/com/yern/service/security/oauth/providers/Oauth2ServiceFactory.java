package com.yern.service.security.oauth.providers;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.NoSuchProviderException;
import java.util.Map;

@Component
@Getter
public class Oauth2ServiceFactory {
    @Autowired
    private Map<String, Oauth2Service> services;

    private String serviceTypePrefix;

    public Oauth2ServiceFactory(
        Map<String, Oauth2Service> services,
        @Value("${service.type.oauth}") String serviceTypePrefix
    ) {
        this.services = services;
        this.serviceTypePrefix = serviceTypePrefix;
    }

    public Oauth2Service getService(String serviceType) throws NoSuchProviderException {
        String serviceIdentifier = getProviderIdentifier(serviceType);

        if (!services.containsKey(serviceIdentifier)) {
            throw new NoSuchProviderException(serviceType);
        }

        return services.get(serviceIdentifier);
    }

    private String getProviderIdentifier(String serviceType) {
        return serviceTypePrefix + "_" + serviceType;
    }
}
