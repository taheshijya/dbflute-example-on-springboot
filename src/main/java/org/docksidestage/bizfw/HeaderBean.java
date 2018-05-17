/*
 * Copyright 2015-2018 the original author or authors.
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

import org.docksidestage.app.application.security.MemberUserDetail;

/**
 * @author jflute
 */
public class HeaderBean { // #change_it #delete_ifapi

    private static final HeaderBean EMPTY_INSTANCE = new HeaderBean();

    public final String memberId;
    public final String memberName;
    public final boolean isLogin;

    private HeaderBean() {
        this.memberId = null;
        this.memberName = null;
        this.isLogin = false;
    }

    public HeaderBean(MemberUserDetail userBean) {
        this.memberId = userBean.getMember().getMemberAccount();
        this.memberName = userBean.getMember().getMemberName();
        this.isLogin = true;
    }

    public static HeaderBean empty() {
        return EMPTY_INSTANCE;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{").append(memberId);
        sb.append(",").append(memberName);
        if (isLogin) {
            sb.append(", login");
        }
        sb.append("}");
        return sb.toString();
    }
}
