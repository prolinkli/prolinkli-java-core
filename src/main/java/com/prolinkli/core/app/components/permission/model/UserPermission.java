package com.prolinkli.core.app.components.permission.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserPermissions {
    private String userPermissionId;
    private Long userId;
    private String permissionLk;
    private String permissionTargetLk;
    private String permissionLevelLk;
    private Integer levelValue; // For bitwise operations
    private LocalDateTime grantedAt;
    private Long grantedBy;
} 