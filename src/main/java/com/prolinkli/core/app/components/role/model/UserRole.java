package com.prolinkli.core.app.components.role.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
    private Long assignedBy;
    private LocalDateTime assignedAt;
    private String roleName; // For convenience when joining with roles table
    
    public UserRole() {
        // Default constructor for serialization/deserialization
    }
    
    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
} 