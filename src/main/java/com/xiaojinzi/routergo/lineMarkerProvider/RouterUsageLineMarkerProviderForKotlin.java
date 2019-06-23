package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.xiaojinzi.routergo.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.*;

import javax.swing.*;

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
