package com.prolinkli.core.app.components.user.model;

import java.util.List;

import com.prolinkli.core.app.components.permission.model.UserPermission;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWithPermissions extends User {

    private List<UserPermission> permissions;

    public UserWithPermissions() {
        super();
    }

    public UserWithPermissions(User user) {
        super(user.getId(), user.getUsername());
    }

    /**
     * Quick check if user has any version of a permission.
     */
    public boolean hasPermission(String permissionLk) {
        return permissions != null && permissions.stream()
            .anyMatch(p -> permissionLk.equals(p.getPermissionLk()));
    }

    /**
     * Check if user has permission with specific level.
     */
    public boolean hasPermissionLevel(String permissionLk, int requiredLevel) {
        return permissions != null && permissions.stream()
            .anyMatch(p -> permissionLk.equals(p.getPermissionLk()) &&
                          p.getLevelValue() != null &&
                          (p.getLevelValue() & requiredLevel) == requiredLevel);
    }
} 