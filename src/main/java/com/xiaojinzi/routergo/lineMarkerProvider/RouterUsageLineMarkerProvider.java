package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.xiaojinzi.routergo.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 */
public class RouterUsageLineMarkerProvider implements LineMarkerProvider {

    private static Icon routerLink = IconLoader.getIcon("logo.png");
    private static Icon interceptorLink = IconLoader.getIcon("interceptor_link.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiClass || element instanceof PsiMethod) {
            PsiModifierListOwner psiModifierListOwner = (PsiModifierListOwner) element;
            PsiAnnotation routerAnno = psiModifierListOwner.getAnnotation(Constants.RouterAnnoClassName);
            if (routerAnno == null) {
                return null;
            }
            PsiAnnotation targetPsiAnnotation = routerAnno;
            LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                    targetPsiAnnotation,
                    targetPsiAnnotation.getTextRange(),
                    routerLink, null,
                    new RouterUsageNavigation(targetPsiAnnotation), GutterIconRenderer.Alignment.RIGHT
            );
            return markerInfo;
        }
        return null;
    }

    @Nullable
    private LineMarkerInfo getInterceptorLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                element,
                element.getTextRange(),
                interceptorLink, null,
                new InterceptorNavigation(element), GutterIconRenderer.Alignment.RIGHT
        );
        return markerInfo;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

}
