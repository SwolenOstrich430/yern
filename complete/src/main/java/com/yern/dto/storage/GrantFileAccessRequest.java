package com.yern.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GrantFileAccessRequest {
    private Long userId;
    private Long fileId;
    private Long roleId;
}
