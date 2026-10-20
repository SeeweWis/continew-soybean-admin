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

package top.continew.admin.config.satoken;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import top.continew.admin.auth.constant.AuthConstants;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.open.util.OpenApiUtils;
import top.continew.starter.auth.refresh.token.api.AccessSessionValidator;
import top.continew.starter.core.util.ServletUtils;
import top.continew.starter.json.jackson.util.JsonUtils;
import top.continew.starter.web.model.R;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sa-Token 扩展拦截器
 *
 * @author Charles7c
 * @since 3.4.0
 */
@Slf4j
public class SaExtensionInterceptor extends SaInterceptor {

    private final AccessSessionValidator accessSessionValidator;

    public SaExtensionInterceptor(SaParamFunction<Object> auth,
        AccessSessionValidator accessSessionValidator) {
        super(auth);
        this.accessSessionValidator = accessSessionValidator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response,
        Object handler) throws Exception {
        boolean flag = super.preHandle(request, response, handler);
        // AK/SK 请求已经由签名认证完成，不属于交互式登录 Session。
        if (!flag || OpenApiUtils.isSignParamExists() || !StpUtil.isLogin()) {
            return flag;
        }
        // Sa-Token 只校验 Access Token 自身有效性；这里补充 Session 状态校验，确保用户、
        // 客户端强制下线后，尚未自然过期的 Access Token 也不能继续访问。
        boolean authRequest = this.isAuthRequest(request);
        if (!authRequest) {
            // 失效原因区分被强退、被顶下线和普通失效，让客户端能给出准确的重新登录提示。
            String invalidReason = accessSessionValidator.getInvalidReason(StpUtil.getTokenValue());
            if (invalidReason != null) {
                // preHandle 返回 false 时 afterCompletion 不再回调（Spring 只对已通过的
                // 拦截器回调），若不在此清除，上一请求写入的 UserContext 线程本地会残留
                // 在 Tomcat 池化线程上，被下一请求复用造成跨用户上下文串用。
                UserContextHolder.clearContext();
                R r = R.fail(String.valueOf(HttpStatus.UNAUTHORIZED.value()), invalidReason);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                ServletUtils.writeJson(response, JsonUtils.toJsonStr(r));
                return false;
            }
        }
        return true;
    }

    private boolean isAuthRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());
        return AuthConstants.LOGIN_URI.equals(path)
            || AuthConstants.REFRESH_URI.equals(path)
            || AuthConstants.LOGOUT_URI.equals(path);
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable Exception e) throws Exception {
        // 清除上下文
        try {
            super.afterCompletion(request, response, handler, e);
        } finally {
            UserContextHolder.clearContext();
        }
    }
}
