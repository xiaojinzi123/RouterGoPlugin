package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * com.intellij.psi.search
 *
 * @author xiaojinzi
 */
public class RouterGoMarkerProvider extends BaseRouterGoMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo doGetLineMarkerInfo(@NotNull PsiElement element) {

        if (element instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) element;
            PsiElement targetPsiElement = psiReferenceExpression.resolve();
            if (targetPsiElement instanceof PsiMethod) {
                PsiMethod targetPsiMethod = (PsiMethod) targetPsiElement;
                boolean isHostMethod = targetPsiMethod.equals(routerRequestHostMethod) ||
                        targetPsiMethod.equals(routerHostMethod) ||
                        targetPsiMethod.equals(rxRouterHostMethod);
                boolean isHostAndPathMethod = targetPsiMethod.equals(routerRequestHostAndPathMethod) ||
                        targetPsiMethod.equals(routerHostAndPathMethod) ||
                        targetPsiMethod.equals(rxRouterHostAndPathMethod);
                // 如果是 host 方法或者是 hostAndPath 方法
                if (isHostMethod || isHostAndPathMethod) {
                    // 如果是一个有 host 和 path 方法 或者 hostAndPath 方法使用的
                    if (Util.isRouteAble(psiReferenceExpression)) {
                        PsiElement targetNavigationPsiElement = psiReferenceExpression;
                        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                                targetNavigationPsiElement,
                                targetNavigationPsiElement.getTextRange(),
                                Constants.ROUTER,
                                Constants.TOOLTIP_PROVIDER_FUNCTION_FOR_ROUTER,
                                new RouterNavigation(psiReferenceExpression), GutterIconRenderer.Alignment.RIGHT
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
