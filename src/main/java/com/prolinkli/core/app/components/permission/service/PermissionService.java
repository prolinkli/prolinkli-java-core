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
        this.levelValueCache = loadLevelValues();
    }

    public boolean hasPermission(User userToCheck, User userContext, 
                               String permissionLk, String permissionLevelLk, 
                               String permissionTargetLk) {

        if (userToCheck == null || permissionLk == null) {
            return false;
        }

        List<UserPermissionDb> userPermissions = permissionDao.getUserPermissions(
            userToCheck.getId(), permissionLk);

        if (userPermissions.isEmpty()) {
            return false;
        }

        for (UserPermissionDb permission : userPermissions) {
            if (matchesTargetRequirement(permission, permissionTargetLk, userToCheck, userContext) &&
                matchesLevelRequirement(permission, permissionLevelLk)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasPermission(User userToCheck, String permissionLk) {
        return hasPermission(userToCheck, null, permissionLk, null, null);
    }

    public boolean hasPermission(User userToCheck, String permissionLk, String permissionLevelLk) {
        return hasPermission(userToCheck, null, permissionLk, permissionLevelLk, null);
    }

    public boolean hasPermission(User userToCheck, String permissionLk, 
                               String permissionLevelLk, String permissionTargetLk) {
        return hasPermission(userToCheck, userToCheck, permissionLk, permissionLevelLk, permissionTargetLk);
    }

    public UserPermission grantPermission(Long userId, String permissionLk, 
                                        String permissionTargetLk, String permissionLevelLk, 
                                        Long grantedBy) {

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

    public List<UserPermission> getUserPermissions(Long userId) {
        List<UserPermissionDb> dbPermissions = permissionDao.getUserPermissions(userId);
        return dbPermissions.stream()
            .map(this::convertToBusinessModel)
            .collect(Collectors.toList());
    }

    private boolean matchesTargetRequirement(UserPermissionDb permission, String requiredTarget, 
                                           User userToCheck, User userContext) {
        if (requiredTarget == null && permission.getPermissionTargetLk() == null) {
            return true;
        }

        if (requiredTarget == null || permission.getPermissionTargetLk() == null) {
            return false;
        }

        String userTarget = permission.getPermissionTargetLk();

        switch (userTarget) {
            case Constants.Permissions.PermissionTargetLk.ALL:
                return true;
            case Constants.Permissions.PermissionTargetLk.SELF:
                return userContext != null && userToCheck.getId().equals(userContext.getId());
            default:
                return userTarget.equals(requiredTarget);
        }
    }

    private boolean matchesLevelRequirement(UserPermissionDb permission, String requiredLevel) {
        if (requiredLevel == null && permission.getPermissionLevelLk() == null) {
            return true;
        }

        if (requiredLevel == null || permission.getPermissionLevelLk() == null) {
            return false;
        }

        Integer userLevelValue = levelValueCache.get(permission.getPermissionLevelLk());
        Integer requiredLevelValue = levelValueCache.get(requiredLevel);

        if (userLevelValue == null || requiredLevelValue == null) {
            return false;
        }

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