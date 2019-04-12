package com.xiaojinzi.routergo.bean;

import com.intellij.psi.PsiElement;

public class InterceptorInfo {

    /**
     * 这个路由关联的元素,
     * 一般是一个 PsiMethodCallExpression 或者 PsiReferenceExpression
     */
    public PsiElement psiElement;

    /**
     * 拦截器的名称
     */
    public String interceptorName;

}
