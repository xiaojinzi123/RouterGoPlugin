package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.xiaojinzi.routergo.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 *
 * @author xiaojinzi
 */
public class RouterUsageLineMarkerProvider implements LineMarkerProvider {

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
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

}
