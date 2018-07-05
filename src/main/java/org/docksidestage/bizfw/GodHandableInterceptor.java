/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.bizfw;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbflute.hook.AccessContext;
import org.docksidestage.app.application.security.MemberUserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author jflute
 */
public class GodHandableInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(GodHandableInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (logger.isDebugEnabled()) {
            logger.debug("#flow ...Beginning #controller " + buildActionDisp(handlerMethod));
        }

        Object principal =
                SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal() : null;

        HeaderBean headerBean = null;
        if (principal instanceof UserDetails) {
            //String username = ((UserDetails) principal).getUsername();
            headerBean = new HeaderBean((MemberUserDetail) principal);

            try {
                AccessContext context = new AccessContext();
                context.setAccessLocalDateTime(LocalDateTime.now());
                context.setAccessUser(headerBean.memberId);
                context.setAccessProcess(buildActionDisp(handlerMethod));
                AccessContext.setAccessContextOnThread(context);
                //return invocation.proceed();
            } finally {
                //AccessContext.clearAccessContextOnThread();
            }

        } else {
            //String username = principal.toString();
            headerBean = HeaderBean.empty();
        }
        request.getSession().setAttribute("headerBean", headerBean);

        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        super.postHandle(request, response, handler, modelAndView);

        AccessContext.clearAccessContextOnThread();

        if (logger.isDebugEnabled()) {
            logger.debug("modelAndView: {}", modelAndView != null ? modelAndView.toString() : null);
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            logger.debug("#flow ...Calling back #finally for " + buildActionDisp(handlerMethod));
        }
    }

    protected String buildActionDisp(HandlerMethod handlerMethod) {
        final Method method = handlerMethod.getMethod();
        final Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass.getSimpleName() + "." + method.getName() + "()";
    }
}
