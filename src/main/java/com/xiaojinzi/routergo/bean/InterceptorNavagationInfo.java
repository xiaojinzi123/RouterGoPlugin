package com.xiaojinzi.routergo.bean;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InterceptorNavagationInfo {

    /**
     * 这个路由关联的元素,
     * 一般是一个 PsiMethodCallExpression 或者 PsiReferenceExpression
     */
    public PsiElement psiElement;

    /**
     * 拦截器的名称
     */
    public String interceptorName;

    public InterceptorNavagationInfo() {
    }

    public InterceptorNavagationInfo(PsiElement psiElement, String interceptorName) {
        this.psiElement = psiElement;
        this.interceptorName = interceptorName;
    }

}
