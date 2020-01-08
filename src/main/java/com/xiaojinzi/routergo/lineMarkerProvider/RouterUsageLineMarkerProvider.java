package com.xiaojinzi.routergo.lineMarkerProvider;

import com.android.tools.idea.naveditor.property.inspector.SimpleProperty;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.psi.*;
import com.intellij.ui.IconManager;
import com.intellij.util.Function;
import com.xiaojinzi.routergo.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
                    Constants.ROUTER_USAGE_FIND,
                    Constants.TOOLTIP_PROVIDER_FUNCTION_FOR_ROUTER_USAGE,
                    new RouterUsageNavigation(targetPsiAnnotation),
                    GutterIconRenderer.Alignment.RIGHT
            );
            return markerInfo;
        }
        return null;
    }

}
