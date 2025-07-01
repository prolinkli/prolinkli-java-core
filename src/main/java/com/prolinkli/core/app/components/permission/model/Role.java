package com.prolinkli.core.app.components.permission.model;

import lombok.Data;
import java.util.List;

@Data
public class Role {
    private Long id;
    private String roleName;
    private String roleDescription;
    private Boolean disabledFlg;
    private Boolean isSystemRole;
    private List<String> permissions; // List of permission codes for convenience
    
    public Role() {
        // Default constructor for serialization/deserialization
    }
    
    public Role(Long id, String roleName, String roleDescription) {
        this.id = id;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.disabledFlg = false;
        this.isSystemRole = false;
    }
} 