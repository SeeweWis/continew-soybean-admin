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

package top.continew.admin.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.model.resp.RouteResp;
import top.continew.admin.auth.model.resp.SocialAuthAuthorizeResp;
import top.continew.admin.auth.model.resp.UserInfoResp;
import top.continew.admin.auth.service.AuthService;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.MenuClientTypeEnum;
import top.continew.admin.system.enums.SocialSourceEnum;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.model.resp.user.UserDetailResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.auth.justauth.AuthRequestFactory;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.log.annotation.Log;
import top.continew.starter.log.enums.Include;
import top.continew.starter.validation.constraints.EnumValue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 认证 API
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Tag(name = "认证 API")
@Log(module = "登录")
@Validated
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private static final String CLIENT_ID_CLAIM = "clientId";

    private final AuthService authService;
    private final UserService userService;
    private final ClientService clientService;
    // 第三方登录为可选能力，未启用时 Starter 不注册 AuthRequestFactory，故惰性获取
    private final ObjectProvider<AuthRequestFactory> authRequestFactoryProvider;

    /**
     * 用户登录
     *
     * @param req      登录信息
     * @param request  请求对象
     * @param response 响应对象
     * @return 登录结果
     */
    @SaIgnore
    @Operation(summary = "登录", description = "用户登录")
    @Log(excludes = {Include.REQUEST_HEADERS, Include.RESPONSE_HEADERS, Include.RESPONSE_BODY})
    @PostMapping("/login")
    public LoginResp login(@RequestBody @Valid LoginReq req, HttpServletRequest request,
        HttpServletResponse response) {
        return authService.login(req, request, response);
    }

    /**
     * 三方账号登录授权
     *
     * @param source 第三方平台来源
     * @return 授权地址响应
     */
    @SaIgnore
    @Operation(summary = "三方账号登录授权", description = "三方账号登录授权")
    @Parameter(name = "source", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @GetMapping("/{source}")
    public SocialAuthAuthorizeResp authorize(@PathVariable @EnumValue(
        value = SocialSourceEnum.class, message = "第三方平台无效") String source) {
        AuthRequestFactory authRequestFactory = authRequestFactoryProvider.getIfAvailable();
        ValidationUtils.throwIf(authRequestFactory == null, "暂未启用第三方登录");
        AuthRequest authRequest = authRequestFactory.getAuthRequest(source);
        return SocialAuthAuthorizeResp.builder()
            .authorizeUrl(authRequest.authorize(AuthStateUtils.createState()))
            .build();
    }

    /**
     * 获取登录用户信息
     *
     * @return 用户信息（含权限列表）
     */
    @Log(ignore = true)
    @Operation(summary = "获取用户信息", description = "获取登录用户信息")
    @GetMapping("/user/info")
    public UserInfoResp getUserInfo() {
        UserContext userContext = UserContextHolder.getContext();
        UserDetailResp userDetailResp = userService.get(userContext.getId());
        UserInfoResp userInfoResp = BeanUtil.copyProperties(userDetailResp, UserInfoResp.class);
        userInfoResp.setPermissions(userContext.getPermissions());
        userInfoResp.setRoles(userContext.getRoleCodes());
        // 两个字段同源推导，避免各自取 now 落在到期点两侧时出现「未过期 + 剩余 0 天」
        Integer pwdExpireDays = userContext.getPwdExpireDays();
        userInfoResp.setPwdExpired(pwdExpireDays != null && pwdExpireDays == 0);
        userInfoResp.setPwdExpireDays(pwdExpireDays);
        userInfoResp.setPwdWarningDays(userContext.getPasswordExpirationWarningDays());
        return userInfoResp;
    }

    /**
     * 获取登录用户路由信息
     *
     * <p>按当前令牌所属客户端的终端类型过滤菜单：桌面端客户端只返回桌面端菜单，
     * 小程序客户端只返回小程序菜单。</p>
     *
     * @return 路由树
     */
    @Log(ignore = true)
    @Operation(summary = "获取路由信息", description = "获取登录用户的路由信息（按客户端终端类型过滤菜单）")
    @GetMapping("/user/route")
    public List<RouteResp> listRoute() {
        UserContext userContext = UserContextHolder.getContext();
        return authService.buildRouteTree(userContext.getId(),
            this.resolveTerminalType(userContext));
    }

    /**
     * 解析当前登录端的终端类型
     *
     * <p>与 {@code SaExtensionInterceptor#resolveClientId} 同理，客户端 ID 必须优先取
     * 令牌声明：用户上下文存在用户级 SaSession，同一账号多端并发登录时会被后一次
     * 登录覆盖，回落读取仅作非 JWT 令牌等场景的兜底。</p>
     *
     * @param userContext 用户上下文
     * @return 终端类型，无法确定时返回 {@code null}（不过滤）
     */
    private MenuClientTypeEnum resolveTerminalType(UserContext userContext) {
        String clientId = null;
        try {
            Object claim = StpUtil.getExtra(StpUtil.getTokenValue(), CLIENT_ID_CLAIM);
            if (claim != null && !claim.toString().isBlank()) {
                clientId = claim.toString();
            }
        } catch (Exception e) {
            // 非 JWT 令牌等场景读不到声明，回落到用户上下文
            log.debug("读取令牌中的客户端声明失败，回落到用户上下文：{}", e.getMessage());
        }
        if (StrUtil.isBlank(clientId) && userContext != null) {
            clientId = userContext.getClientId();
        }
        if (StrUtil.isBlank(clientId)) {
            return null;
        }
        ClientResp client = clientService.getByClientId(clientId);
        if (client == null || StrUtil.isBlank(client.getClientType())) {
            return null;
        }
        return EnumUtil.fromStringQuietly(MenuClientTypeEnum.class, client.getClientType());
    }
}
