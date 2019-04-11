package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.RouterInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 */
public class RouterUsageLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            PsiAnnotation routerAnno = psiClass.getAnnotation(Constants.RouterAnnoClassName);
            if (routerAnno != null) {
                RouterInfo routerInfo = getRouterInfoFromAnno(routerAnno);
                if (routerInfo != null) {
                    routerInfo.psiElement = routerAnno;
                    LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                            routerInfo.psiElement,
                            routerInfo.psiElement.getTextRange(),
                            AllIcons.FileTypes.JavaClass, null,
                            new NavigationImpl(routerInfo), GutterIconRenderer.Alignment.RIGHT
                    );
                    return markerInfo;
                }
            }
        }
        return null;
    }

    @Nullable
    private RouterInfo getRouterInfoFromAnno(@NotNull PsiAnnotation routerAnno) {
        RouterInfo routerInfo = new RouterInfo();
        JvmAnnotationAttributeValue hostAttributeValue = routerAnno.findAttribute(Constants.RouterAnnoHostName).getAttributeValue();
        JvmAnnotationAttributeValue pathAttributeValue = routerAnno.findAttribute(Constants.RouterAnnoPathName).getAttributeValue();
        if (hostAttributeValue instanceof JvmAnnotationConstantValue) {
            routerInfo.host = (String) ((JvmAnnotationConstantValue) hostAttributeValue).getConstantValue();
        }
        if (pathAttributeValue instanceof JvmAnnotationConstantValue) {
            routerInfo.path = (String) ((JvmAnnotationConstantValue) pathAttributeValue).getConstantValue();
        }
        if (routerInfo.host == null || routerInfo.path == null) {
            return null;
        }
        return routerInfo;
    }

    private class NavigationImpl implements GutterIconNavigationHandler {

        @NotNull
        private RouterInfo info;

        public NavigationImpl(@NotNull RouterInfo info) {
            this.info = info;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
            // 注解类@RouterAnno(.....)
            PsiClass routerBuilderClass = javaPsiFacade.findClass(Constants.RouterBuilderClassName, allScope);
            PsiClass rxRouterBuilderClass = javaPsiFacade.findClass(Constants.RxRouterBuilderClassName, allScope);
            /*PsiMethod psiMethod = (PsiMethod) routerClass.findMethodsByName("with")[0];
PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression)((List) MethodReferencesSearch.search(psiMethod).findAll()).get(1);
psiReferenceExpression.getParent().getParent().getParent().getParent().getParent().getParent().getParent();*/
        }

    }

}
