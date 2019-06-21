package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.ui.awt.RelativePoint;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.RouterAnnoInfo;
import com.xiaojinzi.routergo.bean.RouterInfo;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.*;

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
        } else if ((element instanceof PsiReferenceExpression || element instanceof PsiLiteralExpression) &&
                element.getParent() instanceof PsiNameValuePair &&
                element.getParent().getChildren()[0] instanceof PsiIdentifier &&
                "interceptorNames".equals(element.getParent().getChildren()[0].getText())) {
            // 拦截器
            return getInterceptorLineMarkerInfo(element);
        } else if ((element instanceof PsiReferenceExpression || element instanceof PsiLiteralExpression) &&
                element.getParent() instanceof PsiArrayInitializerMemberValue &&
                element.getParent().getParent() instanceof PsiNameValuePair &&
                element.getParent().getParent().getChildren()[0] instanceof PsiIdentifier &&
                "interceptorNames".equals(element.getParent().getParent().getChildren()[0].getText())) {
            // 拦截器
            return getInterceptorLineMarkerInfo(element);
        }
        return null;
    }

    @Nullable
    private LineMarkerInfo getInterceptorLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                element,
                element.getTextRange(),
                interceptorLink, null,
                new InterceptorGoLineMarkerProvider.NavigationImpl(element), GutterIconRenderer.Alignment.RIGHT
        );
        return markerInfo;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

}
