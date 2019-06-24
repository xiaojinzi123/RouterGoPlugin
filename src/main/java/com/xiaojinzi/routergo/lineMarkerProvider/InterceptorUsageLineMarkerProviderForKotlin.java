package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.xiaojinzi.routergo.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 */
public class InterceptorUsageLineMarkerProviderForKotlin implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof KtAnnotationEntry) {
            KtAnnotationEntry ktAnnotationEntry = (KtAnnotationEntry) element;
            // 注解的名字
            String annoStr = ktAnnotationEntry.getShortName().asString();
            if (Constants.InterceptorAnnoClassShortName.equals(annoStr)) {
                KtAnnotationEntry targetPsiAnnotation = ktAnnotationEntry;
                LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                        targetPsiAnnotation,
                        targetPsiAnnotation.getTextRange(),
                        Constants.INTERCEPTOR_LINK, null,new InterceptorUsageNavigation(targetPsiAnnotation), GutterIconRenderer.Alignment.RIGHT
                );
                return markerInfo;
            }
        }
        return null;
    }

}
