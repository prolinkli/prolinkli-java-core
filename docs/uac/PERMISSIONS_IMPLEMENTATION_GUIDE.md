# Permissions System Implementation Guide

This guide shows how to implement a comprehensive permissions system for your Spring Boot application using your existing MyBatis, DAO, and service patterns.

## Table of Contents

1. [Database Schema](#database-schema)
2. [MyBatis Configuration](#mybatis-configuration)
3. [Constants Definition](#constants-definition)
4. [Models and Mappers](#models-and-mappers)
5. [DAO Layer](#dao-layer)
6. [Service Layer](#service-layer)
7. [User Model Integration](#user-model-integration)
8. [Permission Checking](#permission-checking)
9. [Usage Examples](#usage-examples)

## Database Schema

### Step 1: Create Liquibase Migration

Create `src/main/resources/liquibase/changelogs/20250107.01-AddPermissionsSystem.sql`:

```sql
-- liquibase formatted sql
-- changeset permissions:CreatePermissionsSystem splitStatements:false

-- Permissions lookup table
select create_table(
  table_name => 'permissions_lk',
  columns => 'permission_lk VARCHAR(50) NOT NULL,
              permission_name VARCHAR(100) NOT NULL,
              has_targets_flg BOOLEAN NOT NULL DEFAULT FALSE,
              has_levels_flg BOOLEAN NOT NULL DEFAULT FALSE,
              description VARCHAR(255),',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_lk",
    "comment": "Lookup table for permission types",
    "if_not_exists": true,
    "add_timestamps": true
  }'
);

-- Permission levels lookup table
select create_table(
  table_name => 'permissions_levels_lk',
  columns => 'permission_level_lk VARCHAR(15) NOT NULL,
              level_value INTEGER NOT NULL,
              level_name VARCHAR(50) NOT NULL,',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_level_lk",
    "comment": "Lookup table for permission levels with bitwise values",
    "if_not_exists": true,
    "add_timestamps": true,
    "unique_constraints": ["level_value"]
  }'
);

-- Permission targets lookup table
select create_table(
  table_name => 'permissions_targets_lk',
  columns => 'permission_target_lk VARCHAR(50) NOT NULL,
              target_name VARCHAR(100) NOT NULL,',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_target_lk",
    "comment": "Lookup table for permission targets",
    "if_not_exists": true,
    "add_timestamps": true
  }'
);

-- Possible targets for each permission
select create_table(
  table_name => 'permissions_possible_targets_lk',
  columns => 'permission_lk VARCHAR(50) NOT NULL,
              permission_target_lk VARCHAR(50) NOT NULL,',
  foreign_keys => '[
    {
      "column": "permission_lk",
      "references": "permissions_lk(permission_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_target_lk",
      "references": "permissions_targets_lk(permission_target_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_lk, permission_target_lk",
    "comment": "Maps which targets are valid for each permission",
    "if_not_exists": true,
    "add_timestamps": false
  }'
);

-- Possible levels for each permission
select create_table(
  table_name => 'permissions_possible_levels_lk',
  columns => 'permission_lk VARCHAR(50) NOT NULL,
              permission_level_lk VARCHAR(15) NOT NULL,',
  foreign_keys => '[
    {
      "column": "permission_lk",
      "references": "permissions_lk(permission_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_level_lk",
      "references": "permissions_levels_lk(permission_level_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_lk, permission_level_lk",
    "comment": "Maps which levels are valid for each permission",
    "if_not_exists": true,
    "add_timestamps": false
  }'
);

-- User permissions table
select create_table(
  table_name => 'user_permissions',
  columns => 'user_permission_id VARCHAR(36) NOT NULL DEFAULT gen_random_uuid(),
              user_id BIGINT NOT NULL,
              permission_lk VARCHAR(50) NOT NULL,
              permission_target_lk VARCHAR(50) NULL,
              permission_level_lk VARCHAR(15) NULL,
              granted_at TIMESTAMPTZ DEFAULT NOW(),
              granted_by BIGINT,',
  foreign_keys => '[
    {
      "column": "user_id",
      "references": "users(id)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_lk",
      "references": "permissions_lk(permission_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_target_lk",
      "references": "permissions_targets_lk(permission_target_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_level_lk",
      "references": "permissions_levels_lk(permission_level_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "granted_by",
      "references": "users(id)",
      "if_not_exists": true,
      "on_delete": "SET NULL"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "user_permission_id",
    "comment": "User-specific permission assignments",
    "if_not_exists": true,
    "add_timestamps": true,
    "unique_constraints": ["user_id, permission_lk, permission_target_lk, permission_level_lk"]
  }'
);
```

### Step 2: Seed Initial Data

Create `src/main/resources/liquibase/changelogs/20250107.02-SeedPermissionsData.sql`:

```sql
-- liquibase formatted sql
-- changeset permissions:SeedPermissionsData

-- Insert permission levels with bitwise values
INSERT INTO permissions_levels_lk (permission_level_lk, level_value, level_name) VALUES
('NONE', 0, 'No Access'),
('READ', 1, 'Read Only'),
('EDIT', 3, 'Edit (includes Read)'),
('CREATE', 7, 'Create (includes Read, Edit)'),
('DELETE', 15, 'Delete (includes Read, Edit, Create)');

-- Insert permission targets
INSERT INTO permissions_targets_lk (permission_target_lk, target_name) VALUES
('ALL', 'All Resources'),
('SELF', 'Own Resources Only'),
('PROFESSIONALS', 'Professional Users'),
('CONSUMERS', 'Consumer Users'),
('TEAM', 'Team Members'),
('ORGANIZATION', 'Organization Members');

-- Insert sample permissions
INSERT INTO permissions_lk (permission_lk, permission_name, has_targets_flg, has_levels_flg, description) VALUES
('QUOTE', 'Quote Management', true, true, 'Manage quotes and pricing'),
('USER', 'User Management', true, true, 'Manage user accounts'),
('REPORT', 'Report Access', true, true, 'Access and generate reports'),
('ADMIN', 'Admin Panel', false, false, 'Administrative access'),
('PROFILE', 'Profile Management', false, true, 'Manage user profiles');

-- Map valid targets for each permission
INSERT INTO permissions_possible_targets_lk (permission_lk, permission_target_lk) VALUES
('QUOTE', 'ALL'),
('QUOTE', 'SELF'),
('QUOTE', 'PROFESSIONALS'),
('USER', 'ALL'),
('USER', 'TEAM'),
('USER', 'ORGANIZATION'),
('REPORT', 'ALL'),
('REPORT', 'TEAM'),
('PROFILE', 'SELF');

-- Map valid levels for each permission
INSERT INTO permissions_possible_levels_lk (permission_lk, permission_level_lk) VALUES
('QUOTE', 'READ'),
('QUOTE', 'EDIT'),
('QUOTE', 'CREATE'),
('QUOTE', 'DELETE'),
('USER', 'READ'),
('USER', 'EDIT'),
('USER', 'CREATE'),
('USER', 'DELETE'),
('REPORT', 'READ'),
('REPORT', 'CREATE'),
('PROFILE', 'READ'),
('PROFILE', 'EDIT');
```

## MyBatis Configuration

### Step 3: Update MyBatis Generator Config

Add these tables to your `src/main/resources/mybatis-generator-config.xml`:

```xml
<!-- Add these table configurations -->
<table tableName="permissions_lk"
       schema="public"
       domainObjectName="PermissionLkDb">
  <columnOverride column="created_at" isGeneratedAlways="true" />
  <columnOverride column="updated_at" isGeneratedAlways="true" />
</table>

<table tableName="permissions_levels_lk"
       schema="public"
       domainObjectName="PermissionLevelLkDb">
  <columnOverride column="created_at" isGeneratedAlways="true" />
  <columnOverride column="updated_at" isGeneratedAlways="true" />
</table>

<table tableName="permissions_targets_lk"
       schema="public"
       domainObjectName="PermissionTargetLkDb">
  <columnOverride column="created_at" isGeneratedAlways="true" />
  <columnOverride column="updated_at" isGeneratedAlways="true" />
</table>

<table tableName="permissions_possible_targets_lk"
       schema="public"
       domainObjectName="PermissionPossibleTargetLkDb" />

<table tableName="permissions_possible_levels_lk"
       schema="public"
       domainObjectName="PermissionPossibleLevelLkDb" />

<table tableName="user_permissions"
       schema="public"
       domainObjectName="UserPermissionDb">
  <columnOverride column="created_at" isGeneratedAlways="true" />
  <columnOverride column="updated_at" isGeneratedAlways="true" />
  <columnOverride column="granted_at" isGeneratedAlways="true" />
</table>
```

## Constants Definition

### Step 4: Add Permission Constants

Update `src/main/java/com/prolinkli/core/app/Constants.java`:

```java
/**
 * Permission-related constants for the permissions system.
 * 
 * @documentation-pr-rule.mdc
 * 
 * These constants define the permission types, levels, and targets used throughout
 * the application for access control and authorization checking.
 */
public static final class Permissions {

  /**
   * Permission type constants matching the permissions_lk table.
   */
  public static final class PermissionLk {
    public static final String QUOTE = "QUOTE";
    public static final String USER = "USER";
    public static final String REPORT = "REPORT";
    public static final String ADMIN = "ADMIN";
    public static final String PROFILE = "PROFILE";
  }

  /**
   * Permission level constants with bitwise values for hierarchical checking.
   * 
   * @documentation-pr-rule.mdc
   * 
   * Bitwise values allow for hierarchical permission checking:
   * - NONE (0): No access
   * - READ (1): Read-only access
   * - EDIT (3): Edit access (includes READ)
   * - CREATE (7): Create access (includes READ, EDIT)
   * - DELETE (15): Delete access (includes READ, EDIT, CREATE)
   * 
   * Use bitwise AND (&) operations to check if a user has a specific level:
   * if ((userLevel & requiredLevel) == requiredLevel) { /* has permission */ }
   */
  public static final class PermissionLevelLk {
    public static final Integer NONE = 0b0000;
    public static final Integer READ = 0b0001;
    public static final Integer EDIT = 0b0011;
    public static final Integer CREATE = 0b0111;
    public static final Integer DELETE = 0b1111;
    

  }

  /**
   * Permission target constants for scoped access control.
   */
  public static final class PermissionTargetLk {
    public static final String ALL = "ALL";
    public static final String SELF = "SELF";
    public static final String PROFESSIONALS = "PROFESSIONALS";
    public static final String CONSUMERS = "CONSUMERS";
    public static final String TEAM = "TEAM";
    public static final String ORGANIZATION = "ORGANIZATION";
  }
}
```

## Models and Mappers

### Step 5: Generate MyBatis Models

Run your MyBatis generator to create the database models:

```bash
./run-mybatis.sh
```

This will generate:
- `PermissionLkDb.java`
- `PermissionLevelLkDb.java`
- `PermissionTargetLkDb.java`
- `UserPermissionDb.java`
- And their corresponding mappers and XML files

### Step 6: Create Business Models

Create `src/main/java/com/prolinkli/core/app/components/permission/model/Permission.java`:

```java
package com.prolinkli.core.app.components.permission.model;

import lombok.Data;

@Data
public class Permission {
    private String permissionLk;
    private String permissionName;
    private String description;
    private boolean hasTargetsFlg;
    private boolean hasLevelsFlg;
}
```

Create `src/main/java/com/prolinkli/core/app/components/permission/model/UserPermission.java`:

```java
package com.prolinkli.core.app.components.permission.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserPermission {
    private String userPermissionId;
    private Long userId;
    private String permissionLk;
    private String permissionTargetLk;
    private String permissionLevelLk;
    private Integer levelValue; // For bitwise operations
    private LocalDateTime grantedAt;
    private Long grantedBy;
}
```

## DAO Layer

### Step 7: Create Permission DAOs

Create `src/main/java/com/prolinkli/core/app/components/permission/dao/PermissionDao.java`:

```java
package com.prolinkli.core.app.components.permission.dao;

import java.util.List;

import com.prolinkli.core.app.db.model.generated.UserPermissionDb;
import com.prolinkli.core.app.db.model.generated.UserPermissionDbExample;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionDao {

    private final Dao<UserPermissionDb, String> userPermissionDao;

    @Autowired
    public PermissionDao(DaoFactory daoFactory) {
        this.userPermissionDao = daoFactory.getDao(UserPermissionDb.class, String.class);
    }

    public List<UserPermissionDb> getUserPermissions(Long userId) {
        UserPermissionDbExample example = new UserPermissionDbExample();
        example.createCriteria().andUserIdEqualTo(userId);
        return userPermissionDao.select(example);
    }

    public List<UserPermissionDb> getUserPermissions(Long userId, String permissionLk) {
        UserPermissionDbExample example = new UserPermissionDbExample();
        example.createCriteria()
            .andUserIdEqualTo(userId)
            .andPermissionLkEqualTo(permissionLk);
        return userPermissionDao.select(example);
    }

    public UserPermissionDb getUserPermission(Long userId, String permissionLk, 
                                            String targetLk, String levelLk) {
        UserPermissionDbExample example = new UserPermissionDbExample();
        var criteria = example.createCriteria()
            .andUserIdEqualTo(userId)
            .andPermissionLkEqualTo(permissionLk);
        
        if (targetLk != null) {
            criteria.andPermissionTargetLkEqualTo(targetLk);
        } else {
            criteria.andPermissionTargetLkIsNull();
        }
        
        if (levelLk != null) {
            criteria.andPermissionLevelLkEqualTo(levelLk);
        } else {
            criteria.andPermissionLevelLkIsNull();
        }
        
        return userPermissionDao.select(example).stream().findFirst().orElse(null);
    }

    public int insertUserPermission(UserPermissionDb userPermission) {
        return userPermissionDao.insert(userPermission);
    }

    public int deleteUserPermission(String userPermissionId) {
        return userPermissionDao.delete(userPermissionId);
    }
}
```

## Service Layer

### Step 8: Create Permission Service

Create `src/main/java/com/prolinkli/core/app/components/permission/service/PermissionService.java`:

```java
package com.prolinkli.core.app.components.permission.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.prolinkli.core.app.Constants;
import com.prolinkli.core.app.components.permission.dao.PermissionDao;
import com.prolinkli.core.app.components.permission.model.UserPermission;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.db.model.generated.PermissionLevelLkDb;
import com.prolinkli.core.app.db.model.generated.PermissionLevelLkDbExample;
import com.prolinkli.core.app.db.model.generated.UserPermissionDb;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final PermissionDao permissionDao;
    private final Dao<PermissionLevelLkDb, String> levelDao;
    private final Map<String, Integer> levelValueCache;

    @Autowired
    public PermissionService(PermissionDao permissionDao, DaoFactory daoFactory) {
        this.permissionDao = permissionDao;
        this.levelDao = daoFactory.getDao(PermissionLevelLkDb.class, String.class);
        
        // Cache level values for performance
        this.levelValueCache = loadLevelValues();
    }

    /**
     * Check if a user has a specific permission with target and level.
     * This is the main permission checking method.
     */
    public boolean hasPermission(User userToCheck, User userContext, 
                               String permissionLk, String permissionLevelLk, 
                               String permissionTargetLk) {
        
        if (userToCheck == null || permissionLk == null) {
            return false;
        }

        // Get user's permissions for this permission type
        List<UserPermissionDb> userPermissions = permissionDao.getUserPermissions(
            userToCheck.getId(), permissionLk);

        if (userPermissions.isEmpty()) {
            return false;
        }


        // use this instead
        permission.stream().forEach(obj -> {

        });

        // Check each permission against the requirements
        for (UserPermissionDb permission : userPermissions) {
            if (matchesTargetRequirement(permission, permissionTargetLk, userToCheck, userContext) &&
                matchesLevelRequirement(permission, permissionLevelLk)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Overload for permissions without levels (boolean permissions).
     */
    public boolean hasPermission(User userToCheck, String permissionLk) {
        return hasPermission(userToCheck, null, permissionLk, null, null);
    }

    /**
     * Overload for permissions with only levels (no targets).
     */
    public boolean hasPermission(User userToCheck, String permissionLk, String permissionLevelLk) {
        return hasPermission(userToCheck, null, permissionLk, permissionLevelLk, null);
    }

    /**
     * Overload for self-checking permissions.
     */
    public boolean hasPermission(User userToCheck, String permissionLk, 
                               String permissionLevelLk, String permissionTargetLk) {
        return hasPermission(userToCheck, userToCheck, permissionLk, permissionLevelLk, permissionTargetLk);
    }

    /**
     * Grant a permission to a user.
     */
    public UserPermission grantPermission(Long userId, String permissionLk, 
                                        String permissionTargetLk, String permissionLevelLk, 
                                        Long grantedBy) {
        
        // Check if permission already exists
        UserPermissionDb existing = permissionDao.getUserPermission(
            userId, permissionLk, permissionTargetLk, permissionLevelLk);
        
        if (existing != null) {
            throw new IllegalStateException("Permission already granted");
        }

        UserPermissionDb newPermission = new UserPermissionDb();
        newPermission.setUserId(userId);
        newPermission.setPermissionLk(permissionLk);
        newPermission.setPermissionTargetLk(permissionTargetLk);
        newPermission.setPermissionLevelLk(permissionLevelLk);
        newPermission.setGrantedBy(grantedBy);

        permissionDao.insertUserPermission(newPermission);

        return convertToBusinessModel(newPermission);
    }

    /**
     * Get all permissions for a user.
     */
    public List<UserPermission> getUserPermissions(Long userId) {
        List<UserPermissionDb> dbPermissions = permissionDao.getUserPermissions(userId);
        return dbPermissions.stream()
            .map(this::convertToBusinessModel)
            .collect(Collectors.toList());
    }

    private boolean matchesTargetRequirement(UserPermissionDb permission, String requiredTarget, 
                                           User userToCheck, User userContext) {
        if (requiredTarget == null && permission.getPermissionTargetLk() == null) {
            return true; // No target requirement
        }
        
        if (requiredTarget == null || permission.getPermissionTargetLk() == null) {
            return false; // Mismatch in target requirement
        }

        String userTarget = permission.getPermissionTargetLk();
        
        // Handle special target logic
        switch (userTarget) {
            case Constants.Permissions.PermissionTargetLk.ALL:
                return true; // ALL target matches any requirement
            case Constants.Permissions.PermissionTargetLk.SELF:
                return userContext != null && userToCheck.getId().equals(userContext.getId());
            default:
                return userTarget.equals(requiredTarget);
        }
    }

    private boolean matchesLevelRequirement(UserPermissionDb permission, String requiredLevel) {
        if (requiredLevel == null && permission.getPermissionLevelLk() == null) {
            return true; // No level requirement
        }
        
        if (requiredLevel == null || permission.getPermissionLevelLk() == null) {
            return false; // Mismatch in level requirement
        }

        Integer userLevelValue = levelValueCache.get(permission.getPermissionLevelLk());
        Integer requiredLevelValue = levelValueCache.get(requiredLevel);
        
        if (userLevelValue == null || requiredLevelValue == null) {
            return false;
        }

        // Bitwise check: user must have at least the required level
        return (userLevelValue & requiredLevelValue) == requiredLevelValue;
    }

    private Map<String, Integer> loadLevelValues() {
        PermissionLevelLkDbExample example = new PermissionLevelLkDbExample();
        List<PermissionLevelLkDb> levels = levelDao.select(example);
        
        return levels.stream()
            .collect(Collectors.toMap(
                PermissionLevelLkDb::getPermissionLevelLk,
                PermissionLevelLkDb::getLevelValue
            ));
    }

    private UserPermission convertToBusinessModel(UserPermissionDb db) {
        UserPermission model = new UserPermission();
        model.setUserPermissionId(db.getUserPermissionId());
        model.setUserId(db.getUserId());
        model.setPermissionLk(db.getPermissionLk());
        model.setPermissionTargetLk(db.getPermissionTargetLk());
        model.setPermissionLevelLk(db.getPermissionLevelLk());
        
        if (db.getPermissionLevelLk() != null) {
            model.setLevelValue(levelValueCache.get(db.getPermissionLevelLk()));
        }
        
        if (db.getGrantedAt() != null) {
            model.setGrantedAt(db.getGrantedAt().toLocalDateTime());
        }
        model.setGrantedBy(db.getGrantedBy());
        
        return model;
    }
}
```

## User Model Integration

### Step 9: Enhanced User Model

Create `src/main/java/com/prolinkli/core/app/components/user/model/UserWithPermissions.java`:

```java
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
```

### Step 10: Update User Service

Add to `src/main/java/com/prolinkli/core/app/components/user/service/UserGetService.java`:

```java
@Autowired
private PermissionService permissionService;

public UserWithPermissions getUserWithPermissions(Long userId) {
    User user = getUserById(userId);
    if (user == null) {
        return null;
    }
    
    UserWithPermissions userWithPermissions = new UserWithPermissions(user);
    userWithPermissions.setPermissions(permissionService.getUserPermissions(userId));
    
    return userWithPermissions;
}
```

## Permission Checking

### Step 11: Create Permission Controller

Create `src/main/java/com/prolinkli/core/app/components/permission/controller/PermissionController.java`:

```java
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
        
        // Create user object for checking
        User userToCheck = new User();
        userToCheck.setId(userId);
        
        boolean hasPermission = permissionService.hasPermission(
            userToCheck, currentUser, permissionLk, permissionLevelLk, permissionTargetLk);
        
        return ResponseObject.success(hasPermission);
    }
}
```

## Usage Examples

### Step 12: Implementation Examples

Here are practical examples of how to use the permission system:

#### Basic Permission Checking

```java
@Service
public class QuoteService {
    
    @Autowired
    private PermissionService permissionService;
    
    public void updateQuote(Long quoteId, User currentUser, QuoteUpdateRequest request) {
        // Check if user can edit quotes for professionals
        if (!permissionService.hasPermission(
                currentUser, 
                Constants.Permissions.PermissionLk.QUOTE,
                Constants.Permissions.PermissionLevelLk.EDIT,
                Constants.Permissions.PermissionTargetLk.PROFESSIONALS)) {
            throw new AccessDeniedException("Insufficient permissions to edit professional quotes");
        }
        
        // Proceed with quote update
        doUpdateQuote(quoteId, request);
    }
}
```

#### Controller-Level Permission Checking

```java
@RestController
@RequestMapping("/api/quotes")
public class QuoteController {
    
    @Autowired
    private PermissionService permissionService;
    
    @PostMapping
    public ResponseObject<Quote> createQuote(@RequestBody QuoteRequest request, 
                                           @CurrentUser User currentUser) {
        
        // Check create permission
        if (!permissionService.hasPermission(
                currentUser,
                Constants.Permissions.PermissionLk.QUOTE,
                Constants.Permissions.PermissionLevelLk.CREATE)) {
            throw new AccessDeniedException("Cannot create quotes");
        }
        
        return ResponseObject.success(quoteService.createQuote(request, currentUser));
    }
}
```

#### Aspect-Based Permission Checking

Create `src/main/java/com/prolinkli/framework/auth/aspect/RequirePermission.java`:

```java
package com.prolinkli.framework.auth.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String permission();
    String level() default "";
    String target() default "";
}
```

And the aspect:

```java
@Aspect
@Component
public class PermissionAspect {
    
    @Autowired
    private PermissionService permissionService;
    
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // Extract current user from security context
        User currentUser = getCurrentUser();
        
        if (!permissionService.hasPermission(
                currentUser,
                requirePermission.permission(),
                requirePermission.level().isEmpty() ? null : requirePermission.level(),
                requirePermission.target().isEmpty() ? null : requirePermission.target())) {
            throw new AccessDeniedException("Insufficient permissions");
        }
    }
}
```

Usage with annotation:

```java
@RequirePermission(
    permission = Constants.Permissions.PermissionLk.QUOTE,
    level = Constants.Permissions.PermissionLevelLk.DELETE,
    target = Constants.Permissions.PermissionTargetLk.ALL
)
public void deleteQuote(Long quoteId) {
    // Method implementation
}
```

### Step 13: Seeding Test Permissions

Create `src/main/resources/liquibase/local-only/20250107.03-SeedTestPermissions.sql`:

```sql
-- liquibase formatted sql
-- changeset permissions:SeedTestPermissions

-- Grant admin user some permissions for testing
INSERT INTO user_permissions (user_id, permission_lk, permission_target_lk, permission_level_lk, granted_by)
VALUES 
-- Admin can do everything with quotes
((SELECT id FROM users WHERE username = 'admin'), 'QUOTE', 'ALL', 'DELETE', 
 (SELECT id FROM users WHERE username = 'admin')),

-- Admin can manage all users
((SELECT id FROM users WHERE username = 'admin'), 'USER', 'ALL', 'DELETE', 
 (SELECT id FROM users WHERE username = 'admin')),

-- Admin has admin access
((SELECT id FROM users WHERE username = 'admin'), 'ADMIN', NULL, NULL, 
 (SELECT id FROM users WHERE username = 'admin'));
```

## Testing

### Step 14: Unit Tests

Create `src/test/java/com/prolinkli/core/app/components/permission/service/PermissionServiceTest.java`:

```java
@SpringBootTest
class PermissionServiceTest {
    
    @Autowired
    private PermissionService permissionService;
    
    @Test
    void testBasicPermissionCheck() {
        User user = new User(100000L, "testuser");
        
        // Grant permission
        permissionService.grantPermission(
            user.getId(),
            Constants.Permissions.PermissionLk.QUOTE,
            Constants.Permissions.PermissionTargetLk.SELF,
            Constants.Permissions.PermissionLevelLk.READ,
            user.getId()
        );
        
        // Test permission check
        assertTrue(permissionService.hasPermission(
            user,
            Constants.Permissions.PermissionLk.QUOTE,
            Constants.Permissions.PermissionLevelLk.READ,
            Constants.Permissions.PermissionTargetLk.SELF
        ));
    }
    
    @Test
    void testBitwisePermissionCheck() {
        User user = new User(100001L, "testuser2");
        
        // Grant EDIT permission (which includes READ)
        permissionService.grantPermission(
            user.getId(),
            Constants.Permissions.PermissionLk.QUOTE,
            null,
            Constants.Permissions.PermissionLevelLk.EDIT,
            user.getId()
        );
        
        // Should have READ permission due to bitwise hierarchy
        assertTrue(permissionService.hasPermission(
            user,
            Constants.Permissions.PermissionLk.QUOTE,
            Constants.Permissions.PermissionLevelLk.READ
        ));
    }
}
```

This comprehensive implementation provides:

1. **Flexible Permission System**: Supports permissions with/without levels and targets
2. **Bitwise Level Checking**: Hierarchical permission levels using bitwise operations
3. **Target-Based Access**: Different access scopes (ALL, SELF, TEAM, etc.)
4. **Integration with Existing Patterns**: Uses your DAO, Service, and Constants patterns
5. **Performance Optimized**: Caches level values and uses efficient database queries
6. **Extensible**: Easy to add new permissions, levels, or targets
7. **Testing Support**: Includes examples for unit testing

The system allows you to call:
```java
permissionService.hasPermission(userToCheck, userContext, 
    Constants.Permissions.PermissionLk.QUOTE, 
    Constants.Permissions.PermissionLevelLk.EDIT, 
    Constants.Permissions.PermissionTargetLk.PROFESSIONALS);
```

Exactly as requested in your specification! 