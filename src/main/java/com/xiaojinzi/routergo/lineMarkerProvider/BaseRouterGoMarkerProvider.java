package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BaseRouterGoMarkerProvider implements LineMarkerProvider {

    protected static PsiMethod routerRequestHostMethod = null;
    protected static PsiMethod routerHostMethod = null;
    protected static PsiMethod rxRouterHostMethod = null;

    protected static PsiMethod routerRequestHostAndPathMethod = null;
    protected static PsiMethod routerHostAndPathMethod = null;
    protected static PsiMethod rxRouterHostAndPathMethod = null;

    protected static Icon routerLink = IconLoader.getIcon("logo.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {

        if (routerRequestHostMethod == null) {
            routerRequestHostMethod = Util.getRouterRequestHostMethod(element.getProject());
        }
        if (routerHostMethod == null) {
            routerHostMethod = Util.getRouterHostMethod(element.getProject());
        }
        if (rxRouterHostMethod == null) {
            rxRouterHostMethod = Util.getRxRouterHostMethod(element.getProject());
        }

        if (routerRequestHostAndPathMethod == null) {
            routerRequestHostAndPathMethod = Util.getRouterRequestHostAndPathMethod(element.getProject());
        }
        if (routerHostAndPathMethod == null) {
            routerHostAndPathMethod = Util.getRouterHostAndPathMethod(element.getProject());
        }
        if (rxRouterHostAndPathMethod == null) {
            rxRouterHostAndPathMethod = Util.getRxRouterHostAndPathMethod(element.getProject());
        }

        return doGetLineMarkerInfo(element);

    }

    public abstract LineMarkerInfo doGetLineMarkerInfo(@NotNull PsiElement element);

}
