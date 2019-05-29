package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.RouterInfo;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * com.intellij.psi.search
 */
public class RouterGoMarkerProvider implements LineMarkerProvider {

    private static PsiMethod routerRequestHostMethod = null;
    private static PsiMethod routerHostMethod = null;
    private static PsiMethod rxRouterHostMethod = null;
    private static Icon routerLink = IconLoader.getIcon("logo.png");


    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {

        if (routerRequestHostMethod == null) {
            routerRequestHostMethod = Util.getRouterRequestHostMethod(element.getProject());
        }
        if (routerHostMethod == null) {
            routerHostMethod = Util.getRouterHostMethod(element.getProject());
        }
        if (rxRouterHostMethod == null) {
            rxRouterHostMethod = Util.getRxRouterHostMethod(element.getProject());
        }

        if (element instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) element;
            PsiElement targetPsiElement = psiReferenceExpression.resolve();
            if (targetPsiElement instanceof PsiMethod) {
                PsiMethod targetPsiMethod = (PsiMethod) targetPsiElement;
                if (targetPsiMethod.equals(routerRequestHostMethod) ||
                        targetPsiMethod.equals(routerHostMethod) ||
                        targetPsiMethod.equals(rxRouterHostMethod)) {

                    final RouterInfo info = getRouterInfo(psiReferenceExpression);
                    if (info != null) {
                        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                                psiReferenceExpression,
                                psiReferenceExpression.getTextRange(),
                                routerLink,null,
                                new NavigationImpl(info), GutterIconRenderer.Alignment.RIGHT
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

    /**
     * 获取路由的信息
     *
     * @return
     */
    private RouterInfo getRouterInfo(@NotNull PsiReferenceExpression psiReferenceExpression) {
        final RouterInfo info = new RouterInfo();
        Util.getHostAndPath(psiReferenceExpression, info);
        if (info.host == null || info.path == null) {
            return null;
        }
        return info;
    }

    private class NavigationImpl implements GutterIconNavigationHandler {

        @NotNull
        private RouterInfo info;

        public NavigationImpl(@NotNull RouterInfo info) {
            this.info = info;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            //Messages.showMessageDialog("host = " + info.host + "\npath = " + info.path , "tip", null);
            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
            // 注解类@RouterAnno(.....)
            PsiClass serviceAnnotation = javaPsiFacade.findClass(Constants.RouterAnnoClassName, allScope);
            if (serviceAnnotation == null) {
                return;
            }
            List<PsiAnnotation> psiAnnotationList = new ArrayList<>();

            Collection<PsiClass> routerActivities = AnnotatedElementsSearch
                    .searchPsiClasses(serviceAnnotation, allScope)
                    .findAll();
            Collection<PsiMethod> routerStaticMethods = AnnotatedElementsSearch
                    .searchPsiMethods(serviceAnnotation, allScope)
                    .findAll();
                /*for (PsiClass psiClassRouterActivity : routerActivities) {
                    psiClassRouterActivity.navigate(true);
                }*/
            for (PsiClass routerClass : routerActivities) {
                // 静态方法上的注解
                PsiAnnotation routerClassAnnotation = routerClass.getAnnotation(serviceAnnotation.getQualifiedName());
                if (routerClassAnnotation != null) {
                    psiAnnotationList.add(routerClassAnnotation);
                }
            }
            for (PsiMethod routerStaticMethod : routerStaticMethods) {
                // 静态方法上的注解
                PsiAnnotation routerStaticMethodAnnotation = routerStaticMethod.getAnnotation(serviceAnnotation.getQualifiedName());
                if (routerStaticMethodAnnotation != null) {
                    psiAnnotationList.add(routerStaticMethodAnnotation);
                }
            }

            PsiAnnotation targetAnno = null;

            for (int i = psiAnnotationList.size() - 1; i >= 0; i--) {
                PsiAnnotation psiAnnotation = psiAnnotationList.get(i);
                if (isMatchHostAndPath(info, psiAnnotation)) {
                    targetAnno = psiAnnotation;
                    break;
                }
            }

            if (targetAnno != null && targetAnno.canNavigate()) {
                targetAnno.navigate(true);
            }

        }
    }

    private boolean isMatchHostAndPath(@NotNull RouterInfo routerInfo, @NotNull PsiAnnotation psiAnnotation) {
        List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
        String host = null, path = null;
        for (JvmAnnotationAttribute attribute : attributes) {
            if (Constants.RouterAnnoHostName.equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                host = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
            } else if (Constants.RouterAnnoPathName.equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                path = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
            }
        }
        // 可能是默认值
        if (host == null) {
            host = Util.getHostFromRouterAnno(psiAnnotation);
        }
        if (host == null || path == null) {
            return false;
        }

        return host.equals(routerInfo.host) && path.equals(routerInfo.path);
    }


}
