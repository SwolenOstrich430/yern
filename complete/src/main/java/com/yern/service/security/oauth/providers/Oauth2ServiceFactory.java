package com.yern.service.security.oauth.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.NoSuchProviderException;
import java.util.Map;

@Component
public class Oauth2ServiceFactory {
    @Autowired
    private Map<String, Oauth2Service> services; // Injects all Oauth2Service beans by name

    @Value("${service.type.oauth}")
    private String serviceTypePrefix;

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
