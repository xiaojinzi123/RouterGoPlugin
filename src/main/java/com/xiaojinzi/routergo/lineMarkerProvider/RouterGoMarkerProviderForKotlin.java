package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.util.KtUtil;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;

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
            PsiMethod targetPsiMethod = KtUtil.getTargetRefrenceMethod(ktSimpleNameExpression);
            if (targetPsiMethod != null) {
                boolean isHostMethod = Util.isHostMethod(targetPsiMethod.getProject(), targetPsiMethod);
                boolean isHostAndPathMethod = Util.isHostAndPathMethod(targetPsiMethod.getProject(), targetPsiMethod);
                if (isHostMethod || isHostAndPathMethod) {
                    if (KtUtil.getRouterInfoFromKtNameReferenceExpression(ktSimpleNameExpression) != null) {
                        PsiElement targetPsiElement = ktSimpleNameExpression;
                        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                                targetPsiElement,
                                targetPsiElement.getTextRange(),
                                Constants.ROUTER,
                                Constants.TOOLTIP_PROVIDER_FUNCTION_FOR_ROUTER,
                                new RouterNavigation(ktSimpleNameExpression),
                                GutterIconRenderer.Alignment.RIGHT
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
