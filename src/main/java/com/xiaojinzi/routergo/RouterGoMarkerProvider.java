package com.xiaojinzi.routergo;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.facet.Facet;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiAnnotationPattern;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * com.intellij.psi.search
 */
public class RouterGoMarkerProvider implements LineMarkerProvider {

    // 几种调用的方式

    public static final String CALL_STR1 = "Router\\.with[\\S\\s]*\\.host[\\S\\s]*.path[\\S\\s]*\\.navigate[\\S\\s]*";

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        // AllIcons.FileTypes.JavaClass
        for (PsiElement element : elements) {
            final LineMarkerInfo info = doGetLineMarkerInfo(element);
            if (info != null) {
                result.add(info);
            }
        }
    }
    //
    @Nullable
    private LineMarkerInfo doGetLineMarkerInfo(PsiElement element) {
        if (!(element instanceof PsiMethodCallExpression)) {
            return null;
        }
        final PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;
        // 得到调用的语句
        String callStr = psiMethodCallExpression.getText();
        if (!callStr.matches(CALL_STR1)) {
            return null;
        }
        LineMarkerInfo<PsiMethodCallExpression> markerInfo = new LineMarkerInfo<PsiMethodCallExpression>(
                psiMethodCallExpression,
                psiMethodCallExpression.getTextRange(),
                AllIcons.FileTypes.JavaClass, null, new NavigationImpl(), GutterIconRenderer.Alignment.RIGHT
        );
        return markerInfo;
    }

    private class NavigationImpl implements GutterIconNavigationHandler {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
            // 注解类@RouterAnno(.....)
            PsiClass serviceAnnotation = javaPsiFacade.findClass("com.xiaojinzi.component.anno.RouterAnno", allScope);
            if (serviceAnnotation != null) {
                Collection<PsiClass> routerActivities = AnnotatedElementsSearch
                        .searchPsiClasses(serviceAnnotation, allScope)
                        .findAll();
                for (PsiClass psiClassRouterActivity : routerActivities) {
                    psiClassRouterActivity.navigate(true);
                }
            }
        }
    }

}
