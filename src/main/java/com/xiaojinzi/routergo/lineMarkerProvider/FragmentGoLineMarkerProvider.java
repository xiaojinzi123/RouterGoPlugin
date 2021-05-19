package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
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
 * Fragment 的到达目标的实现
 * 两个条件
 * 1.Router.with() 方法
 * 2.并且 with() 方法中必须是一个字符串的值
 *
 * @author xiaojinzi
 */
public class FragmentGoLineMarkerProvider implements LineMarkerProvider {

    /**
     * 如果是 Router.with("") 方法 的 PsiReferenceExpression 节点
     */
    private boolean isRouterWithFragmentMethod(@Nullable PsiElement psiElement) {
        if (psiElement instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) psiElement;
            PsiElement targetPsiElement = psiReferenceExpression.resolve();
            if (targetPsiElement instanceof PsiMethod) {
                PsiMethod targetPsiMethod = (PsiMethod) targetPsiElement;
                PsiMethod routerWithFragmentMethod = Util.getRouterWithFragmentMethod(psiElement.getProject());
                PsiMethod rxRouterWithFragmentMethod = Util.getRxRouterWithFragmentMethod(psiElement.getProject());
                if (targetPsiMethod.equals(routerWithFragmentMethod) ||
                        targetPsiMethod.equals(rxRouterWithFragmentMethod)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (isRouterWithFragmentMethod(element)) {
            PsiElement targetNavigationPsiElement = element;
            LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<>(
                    targetNavigationPsiElement,
                    targetNavigationPsiElement.getTextRange(),
                    Constants.ROUTER,
                    Constants.TOOLTIP_PROVIDER_FUNCTION_FOR_FRAGMENT,
                    new FragmentNavigation(Util.getStringValue(element.getNextSibling().getChildren()[1])),
                    GutterIconRenderer.Alignment.RIGHT
            );
            return markerInfo;
        }
        return null;
    }

}
