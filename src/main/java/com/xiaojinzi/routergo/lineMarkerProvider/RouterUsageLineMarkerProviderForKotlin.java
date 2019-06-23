package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
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
import com.xiaojinzi.routergo.util.KtUtil;
import com.xiaojinzi.routergo.util.Util;
import groovy.transform.Undefined;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.jvm.KotlinJavaPsiFacade;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 */
public class RouterUsageLineMarkerProviderForKotlin implements LineMarkerProvider {

    private static Icon routerLink = IconLoader.getIcon("logo.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        // 如果是一个注解
        if (element instanceof KtAnnotationEntry) {
            KtAnnotationEntry annotationEntry = (KtAnnotationEntry) element;
            // 注解的名字
            String annoStr = annotationEntry.getShortName().asString();
            // 如果是 RouterAnno
            if (Constants.RouterAnnoClassShortName.equals(annoStr)) {
                KtAnnotationEntry targetPsiAnnotation = annotationEntry;
                LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                        targetPsiAnnotation,
                        targetPsiAnnotation.getTextRange(),
                        routerLink, null,
                        new RouterUsageNavigation(targetPsiAnnotation), GutterIconRenderer.Alignment.RIGHT
                );
                return markerInfo;
            }
        }
        return null;
    }

}
