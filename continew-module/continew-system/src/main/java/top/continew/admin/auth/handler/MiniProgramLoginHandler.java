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

package top.continew.admin.auth.handler;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.auth.AbstractLoginHandler;
import top.continew.admin.auth.client.MiniProgramApiClient;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.MiniProgramLoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.system.enums.OptionCategoryEnum;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.starter.core.util.validation.ValidationUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 微信小程序手机号授权登录处理器
 *
 * @author seewe-yolo
 * @since 4.2.0
 */
@Component
@RequiredArgsConstructor
public class MiniProgramLoginHandler extends AbstractLoginHandler<MiniProgramLoginReq> {

    private static final String APP_ID_OPTION = "MINI_PROGRAM_APP_ID";
    private static final String APP_SECRET_OPTION = "MINI_PROGRAM_APP_SECRET";
    private static final String DEFAULT_DEPT_ID_OPTION = "MINI_PROGRAM_DEFAULT_DEPT_ID";

    private final MiniProgramApiClient miniProgramApiClient;
    private final DeptService deptService;
    private final UserRoleService userRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp login(MiniProgramLoginReq req, ClientResp client, HttpServletRequest request,
        HttpServletResponse response) {
        Map<String, String> loginOptions = optionService.getByCategory(OptionCategoryEnum.LOGIN);
        String appId = loginOptions.get(APP_ID_OPTION);
        String appSecret = loginOptions.get(APP_SECRET_OPTION);
        ValidationUtils.throwIf(StrUtil.hasBlank(appId, appSecret), "微信小程序登录尚未配置");
        String phone = miniProgramApiClient.getPhoneNumber(req.getCode(), appId, appSecret);
        UserDO user = userService.getByPhone(phone);
        if (user == null) {
            user = this.register(phone, loginOptions.get(DEFAULT_DEPT_ID_OPTION));
        }
        return super.authenticate(user, client, request, response);
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.MINI_PROGRAM;
    }

    private UserDO register(String phone, String defaultDeptIdText) {
        ValidationUtils.throwIfBlank(defaultDeptIdText, "未配置小程序默认归属部门");
        ValidationUtils.throwIf(!NumberUtil.isLong(defaultDeptIdText), "小程序默认归属部门配置无效");
        Long defaultDeptId = NumberUtil.parseLong(defaultDeptIdText);
        DeptDO defaultDept = deptService.getById(defaultDeptId);
        ValidationUtils.throwIf(defaultDept == null
            || defaultDept.getStatus() != DisEnableStatusEnum.ENABLE, "未查询到启用的小程序默认归属部门");
        Long roleId = roleService.getIdByCode(RoleCodeEnum.MINI_PROGRAM_USER.getCode());
        ValidationUtils.throwIfNull(roleId, "未查询到小程序用户角色");

        ValidationUtils.throwIf(StrUtil.isBlank(phone) || phone.length() < 4,
            "微信小程序未返回有效手机号");
        String phoneSuffix = phone.substring(phone.length() - 4);
        UserDO user = new UserDO();
        user.setUsername(this.generateUsername(phoneSuffix));
        user.setNickname("微信用户_" + phoneSuffix);
        user.setPhone(phone);
        user.setDeptId(defaultDept.getId());
        user.setStatus(DisEnableStatusEnum.ENABLE);
        userService.save(user);
        userRoleService.assignRolesToUser(List.of(roleId), user.getId());
        return user;
    }

    private String generateUsername(String phoneSuffix) {
        // ponytail: Limit username probes to 100; raise it if a phone suffix namespace gets crowded.
        for (int attempt = 0; attempt < 100; attempt++) {
            String username = "wx_" + RandomUtil.randomString(RandomUtil.BASE_CHAR, 2)
                .toLowerCase(Locale.ROOT) + phoneSuffix;
            if (userService.getByUsername(username) == null) {
                return username;
            }
        }
        throw new IllegalStateException("小程序用户账号生成失败，请稍后重试");
    }
}
