package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRouterGoMarkerProvider implements LineMarkerProvider {

    protected PsiMethod routerRequestHostMethod = null;
    protected PsiMethod routerHostMethod = null;
    protected PsiMethod rxRouterHostMethod = null;

    protected PsiMethod routerRequestHostAndPathMethod = null;
    protected PsiMethod routerHostAndPathMethod = null;
    protected PsiMethod rxRouterHostAndPathMethod = null;

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        routerRequestHostMethod = Util.getRouterRequestHostMethod(element.getProject());
        routerHostMethod = Util.getRouterHostMethod(element.getProject());
        rxRouterHostMethod = Util.getRxRouterHostMethod(element.getProject());

        routerRequestHostAndPathMethod = Util.getRouterRequestHostAndPathMethod(element.getProject());
        routerHostAndPathMethod = Util.getRouterHostAndPathMethod(element.getProject());
        rxRouterHostAndPathMethod = Util.getRxRouterHostAndPathMethod(element.getProject());

        return doGetLineMarkerInfo(element);

    }

    public abstract LineMarkerInfo doGetLineMarkerInfo(@NotNull PsiElement element);

}
