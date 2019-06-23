package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BaseInterceptorGoLineMarkerProvider implements LineMarkerProvider {

    protected static PsiMethod routerInterceptorNameMethod = null;
    protected static PsiMethod rxRouterInterceptorNameMethod = null;

    protected static Icon interceptorLink = IconLoader.getIcon("interceptor_link.png");

    @Nullable
    @Override
    public final LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (routerInterceptorNameMethod == null) {
            routerInterceptorNameMethod = Util.getRouterInterceptorNameMethod(element.getProject());
        }
        if (rxRouterInterceptorNameMethod == null) {
            rxRouterInterceptorNameMethod = Util.getRxRouterInterceptorNameMethod(element.getProject());
        }
        if (isFit(element)) {
            PsiElement targetPsiElement = element;
            LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                    targetPsiElement,
                    targetPsiElement.getTextRange(),
                    interceptorLink, null,
                    new InterceptorNavigation(targetPsiElement), GutterIconRenderer.Alignment.LEFT
            );
            return markerInfo;
        }
        return null;
    }

    /**
     * 满足跳转的这个 element 必须是一个字符串表达式
     *
     * @param element 如果返回 true,必须是一个字符串表达式
     * @return
     */
    public abstract boolean isFit(@NotNull PsiElement element);

}
