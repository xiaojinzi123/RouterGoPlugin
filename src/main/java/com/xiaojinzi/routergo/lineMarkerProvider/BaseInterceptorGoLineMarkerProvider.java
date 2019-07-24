package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author xiaojinzi
 */
public abstract class BaseInterceptorGoLineMarkerProvider implements LineMarkerProvider {

    protected PsiMethod routerInterceptorNameMethod = null;
    protected PsiMethod rxRouterInterceptorNameMethod = null;

    @Nullable
    @Override
    public final LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        routerInterceptorNameMethod = Util.getRouterInterceptorNameMethod(element.getProject());
        rxRouterInterceptorNameMethod = Util.getRxRouterInterceptorNameMethod(element.getProject());
        if (isFit(element)) {
            PsiElement targetPsiElement = element;
            LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                    targetPsiElement,
                    targetPsiElement.getTextRange(),
                    Constants.INTERCEPTOR_LINK, 0, null,
                    new InterceptorNavigation(targetPsiElement), GutterIconRenderer.Alignment.LEFT
            );
            /*LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                    targetPsiElement,
                    targetPsiElement.getTextRange(),
                    Constants.INTERCEPTOR_LINK, null,
                    new InterceptorNavigation(targetPsiElement), GutterIconRenderer.Alignment.LEFT
            );*/
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
