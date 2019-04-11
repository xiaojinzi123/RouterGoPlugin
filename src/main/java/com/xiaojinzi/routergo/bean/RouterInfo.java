package com.xiaojinzi.routergo.bean;

import com.intellij.psi.PsiElement;

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

}