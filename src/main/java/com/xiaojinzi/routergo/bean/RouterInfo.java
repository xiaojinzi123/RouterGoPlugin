package com.xiaojinzi.routergo.bean;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class RouterInfo {

    /**
     * 这个路由关联的元素,
     * 一般是一个 PsiMethodCallExpression 或者 PsiReferenceExpression
     */
    public PsiElement psiElement;

    /**
     * 路由的 host
     */
    public String host;

    /**
     * 路由的 path
     */
    public String path;

    public void setHostAndPath(@Nullable String hostAndPath) {
        if (hostAndPath != null) {
            int index = hostAndPath.indexOf('/');
            if (index > 0 && index < hostAndPath.length() - 1) {
                host = hostAndPath.substring(0, index);
                path = hostAndPath.substring(index + 1);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RouterInfo)) {
            return false;
        }
        RouterInfo target = (RouterInfo) obj;
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