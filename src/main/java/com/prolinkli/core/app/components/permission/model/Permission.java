package com.prolinkli.core.app.components.permission.model;

import lombok.Data;

@Data
public class Permission {
    private String permissionCode;
    private String permissionName;
    private String permissionLevel;
    private String permissionScope;
    private String description;
    private String shortDescription;
    
    public Permission() {
        // Default constructor for serialization/deserialization
    }
    
    public Permission(String permissionCode, String permissionName, String permissionLevel, String permissionScope) {
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.permissionLevel = permissionLevel;
        this.permissionScope = permissionScope;
    }
} 