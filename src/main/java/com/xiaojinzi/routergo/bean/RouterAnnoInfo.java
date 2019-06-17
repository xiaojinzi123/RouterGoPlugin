package com.xiaojinzi.routergo.bean;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class RouterAnnoInfo extends RouterInfo {

    /**
     * 这个路由关联的元素,
     * 一般是一个 PsiMethodCallExpression 或者 PsiReferenceExpression
     */
    public PsiElement psiElement;

    public RouterAnnoInfo(RouterInfo routerInfo) {
        host = routerInfo.host;
        path = routerInfo.path;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RouterAnnoInfo)) {
            return false;
        }
        RouterAnnoInfo target = (RouterAnnoInfo) obj;
        if (
                ((host == null && target.host == null) || host.equals(target.host)) &&
                ((path == null && target.path == null) || path.equals(target.path)) &&
                ((psiElement == null && target.psiElement == null) || psiElement.equals(target.psiElement))
        ) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return
                (psiElement == null?0:psiElement.hashCode()) +
                (host == null?0:host.hashCode()) +
                (path == null?0:path.hashCode());
    }

}