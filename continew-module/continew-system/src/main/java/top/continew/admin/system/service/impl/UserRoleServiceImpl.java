/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.system.service.impl;

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.UserRoleDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.query.RoleUserQuery;
import top.continew.admin.system.model.resp.role.RoleUserResp;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 用户和角色业务实现
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper baseMapper;
    @Lazy
    @Resource
    private RoleService roleService;
    @Lazy
    @Resource
    private UserService userService;

    @Override
    @AutoOperate(type = RoleUserResp.class, on = "list")
    public PageResp<RoleUserResp> pageUser(RoleUserQuery query, PageQuery pageQuery) {
        String description = query.getDescription();
        QueryWrapper<UserRoleDO> queryWrapper =
            new QueryWrapper<UserRoleDO>().eq("t1.role_id", query.getRoleId())
                .and(StrUtil.isNotBlank(description), q -> q.like("t2.username", description)
                    .or()
                    .like("t2.nickname", description)
                    .or()
                    .like("t2.description", description));
        QueryWrapperHelper.sort(queryWrapper, pageQuery.getSort());
        IPage<RoleUserResp> page =
            baseMapper.selectUserPage(new Page<>(pageQuery.getPage(), pageQuery
                .getSize()), queryWrapper);
        return PageResp.build(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(List<Long> roleIds, Long userId) {
        List<Long> oldRoleIds = this.listRoleIdByUserId(userId);
        boolean isRoleChanged = CollUtil
            .isNotEmpty(CollUtil.disjunction(roleIds, oldRoleIds));
        this.checkSystemUserChange(isRoleChanged ? Set.of(userId) : Set.of());
        CheckUtils.throwIfEmpty(roleIds, "所属角色不能为空");
        CheckUtils.throwIf(roleIds.stream().anyMatch(Objects::isNull), "角色不存在或无权限");
        Set<Long> uniqueRoleIds = CollUtil.newHashSet(roleIds);
        CheckUtils.throwIf(uniqueRoleIds.size() != roleIds.size(), "角色 ID 重复");
        List<RoleDO> roles = roleService.listByIds(roleIds);
        CheckUtils.throwIf(roles.size() != uniqueRoleIds.size(), "角色不存在或无权限");
        Set<String> roleCodes = CollUtils.mapToSet(roles, RoleDO::getCode);
        CheckUtils.throwIf(roleCodes.contains(RoleCodeEnum.SUPER_ADMIN.getCode()),
            "不允许分配超级管理员角色");
        if (!isRoleChanged) {
            return false;
        }
        baseMapper.lambdaUpdate().eq(UserRoleDO::getUserId, userId).remove();
        List<UserRoleDO> userRoles =
            CollUtils.mapToList(roleIds, roleId -> new UserRoleDO(userId, roleId));
        return baseMapper.insertBatch(userRoles);
    }

    @Override
    public boolean assignRoleToUsers(Long roleId, List<Long> userIds) {
        this.checkSystemUserChange(new HashSet<>(userIds));
        List<UserRoleDO> userRoles =
            CollUtils.mapToList(userIds, userId -> new UserRoleDO(userId, roleId));
        return baseMapper.insertBatch(userRoles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        List<UserRoleDO> userRoles = baseMapper.selectByIds(ids);
        Set<Long> userIds = CollUtils.mapToSet(userRoles, UserRoleDO::getUserId);
        this.checkSystemUserChange(userIds);
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        this.checkSystemUserChange(new HashSet<>(userIds));
        baseMapper.lambdaUpdate().in(UserRoleDO::getUserId, userIds).remove();
    }

    @Override
    public void saveBatch(List<UserRoleDO> userRoles) {
        Set<Long> userIds = CollUtils.mapToSet(userRoles, UserRoleDO::getUserId);
        this.checkSystemUserChange(userIds);
        baseMapper.insert(userRoles);
    }

    @Override
    public List<Long> listRoleIdByUserId(Long userId) {
        return baseMapper.lambdaQuery()
            .select(UserRoleDO::getRoleId)
            .eq(UserRoleDO::getUserId, userId)
            .list()
            .stream()
            .map(UserRoleDO::getRoleId)
            .toList();
    }

    @Override
    public List<Long> listUserIdByRoleId(Long roleId) {
        return baseMapper.lambdaQuery()
            .select(UserRoleDO::getUserId)
            .eq(UserRoleDO::getRoleId, roleId)
            .list()
            .stream()
            .map(UserRoleDO::getUserId)
            .toList();
    }

    @Override
    public boolean isRoleIdExists(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        return baseMapper.lambdaQuery().in(UserRoleDO::getRoleId, roleIds).exists();
    }

    private void checkSystemUserChange(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        Optional<UserDO> systemUser = userService.listByIds(userIds).stream()
            .filter(user -> Boolean.TRUE.equals(user.getIsSystem()))
            .findFirst();
        CheckUtils.throwIf(systemUser::isPresent, "[{}] 是系统内置用户，不允许变更角色",
            systemUser.orElseGet(UserDO::new).getNickname());
    }
}
