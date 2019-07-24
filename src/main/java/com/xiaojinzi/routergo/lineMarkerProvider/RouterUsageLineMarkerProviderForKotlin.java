package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.xiaojinzi.routergo.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

import java.util.Collection;
import java.util.List;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 *
 * @author xiaojinzi
 */
public class RouterUsageLineMarkerProviderForKotlin implements LineMarkerProvider {

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
                        Constants.ROUTER_LINK, 0, null,
                        new RouterUsageNavigation(targetPsiAnnotation), GutterIconRenderer.Alignment.RIGHT
                );
                /*LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                        targetPsiAnnotation,
                        targetPsiAnnotation.getTextRange(),
                        Constants.ROUTER_LINK, null,
                        new RouterUsageNavigation(targetPsiAnnotation), GutterIconRenderer.Alignment.RIGHT
                );*/
                return markerInfo;
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

}
