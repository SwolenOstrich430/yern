package com.yern.service.secrets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SecretImpl {
    private String name;
    private String value;
    private String version;
    private String externalVersion;
}
