package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.xiaojinzi.routergo.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 */
public class InterceptorUsageLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            PsiAnnotation interceptorAnno = psiClass.getAnnotation(Constants.InterceptorAnnoClassName);
            if (interceptorAnno == null) {
                return null;
            }
            PsiElement targetPsiElement = interceptorAnno;
            LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                    targetPsiElement,
                    targetPsiElement.getTextRange(),
                    Constants.INTERCEPTOR_LINK, null,new InterceptorUsageNavigation(interceptorAnno), GutterIconRenderer.Alignment.RIGHT
            );
            return markerInfo;
        }
        return null;
    }

}
