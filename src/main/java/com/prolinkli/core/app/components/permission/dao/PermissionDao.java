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