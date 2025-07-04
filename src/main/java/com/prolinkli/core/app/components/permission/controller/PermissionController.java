package com.prolinkli.core.app.components.permission.controller;

import java.util.List;

import com.prolinkli.core.app.components.permission.model.UserPermission;
import com.prolinkli.core.app.components.permission.service.PermissionService;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.framework.auth.model.CurrentUser;
import com.prolinkli.framework.exception.response.model.ResponseObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public List<UserPermission> getUserPermissions(@PathVariable Long userId) {
        return permissionService.getUserPermissions(userId);
    }

    @PostMapping("/grant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseObject<UserPermission> grantPermission(
            @RequestParam Long userId,
            @RequestParam String permissionLk,
            @RequestParam(required = false) String permissionTargetLk,
            @RequestParam(required = false) String permissionLevelLk,
            @CurrentUser User currentUser) {

        UserPermission granted = permissionService.grantPermission(
            userId, permissionLk, permissionTargetLk, permissionLevelLk, currentUser.getId());

        return ResponseObject.success(granted);
    }

    @GetMapping("/check")
    public ResponseObject<Boolean> checkPermission(
            @RequestParam Long userId,
            @RequestParam String permissionLk,
            @RequestParam(required = false) String permissionLevelLk,
            @RequestParam(required = false) String permissionTargetLk,
            @CurrentUser User currentUser) {

        User userToCheck = new User();
        userToCheck.setId(userId);

        boolean hasPermission = permissionService.hasPermission(
            userToCheck, currentUser, permissionLk, permissionLevelLk, permissionTargetLk);

        return ResponseObject.success(hasPermission);
    }
} 