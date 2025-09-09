package com.yern.service.security.oauth.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = Oauth2ServiceFactory.class)
public class Oauth2ServiceFactoryTests {
    @MockitoBean
    private GoogleOauth2Service googleOauth2Service;

    @Value("${service.type.oauth}")
    private String serviceTypePrefix;

    private Oauth2ServiceFactory oauth2ServiceFactory;

    @BeforeEach
    public void setup() {
        Map<String, Oauth2Service> services = new HashMap<>();
        services.put(serviceTypePrefix + "_google", googleOauth2Service);
        oauth2ServiceFactory = new Oauth2ServiceFactory(services, serviceTypePrefix);
    }

    @Test
    public void getService_throwsNoSuchProviderException_givenInvalidProvider() {
        assertThrows(NoSuchProviderException.class, () -> {
            oauth2ServiceFactory.getService("invalid");
        });
    }

    @Test
    public void getService_returnsAnInstanceOfAssociatedProvider_givenValidProvider() throws NoSuchProviderException {
        Oauth2Service factory = oauth2ServiceFactory.getService("google");
        assertInstanceOf(GoogleOauth2Service.class, factory);
    }
}
