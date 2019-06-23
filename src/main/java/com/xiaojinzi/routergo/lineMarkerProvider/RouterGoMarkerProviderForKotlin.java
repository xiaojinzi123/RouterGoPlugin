package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.RouterInfo;
import com.xiaojinzi.routergo.util.KtUtil;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * com.intellij.psi.search
 */
public class RouterGoMarkerProviderForKotlin extends BaseRouterGoMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo doGetLineMarkerInfo(@NotNull PsiElement element) {

        if (element instanceof KtSimpleNameExpression) {
            KtSimpleNameExpression ktSimpleNameExpression = (KtSimpleNameExpression) element;
            PsiMethod targetPsiMethod = KtUtil.getTargetRefrenceMethod(element);
            if (targetPsiMethod != null) {
                boolean isHostMethod = targetPsiMethod.equals(routerRequestHostMethod) ||
                        targetPsiMethod.equals(routerHostMethod) ||
                        targetPsiMethod.equals(rxRouterHostMethod);
                boolean isHostAndPathMethod = targetPsiMethod.equals(routerRequestHostAndPathMethod) ||
                        targetPsiMethod.equals(routerHostAndPathMethod) ||
                        targetPsiMethod.equals(rxRouterHostAndPathMethod);
                if (isHostMethod || isHostAndPathMethod) {
                    if (KtUtil.getRouterInfoFromKtNameReferenceExpression(ktSimpleNameExpression) != null) {
                        PsiElement targetPsiElement = ktSimpleNameExpression;
                        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                                targetPsiElement,
                                targetPsiElement.getTextRange(),
                                routerLink, null, new RouterNavigation(ktSimpleNameExpression), GutterIconRenderer.Alignment.RIGHT
                        );
                        return markerInfo;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

}