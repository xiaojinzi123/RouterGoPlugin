package com.xiaojinzi.routergo.lineMarkerProvider;

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

    private static PsiMethod routerRequestHostAndPathMethod = null;
    private static PsiMethod routerHostAndPathMethod = null;
    private static PsiMethod rxRouterHostAndPathMethod = null;

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

        if (routerRequestHostAndPathMethod == null) {
            routerRequestHostAndPathMethod = Util.getRouterRequestHostAndPathMethod(element.getProject());
        }
        if (routerHostAndPathMethod == null) {
            routerHostAndPathMethod = Util.getRouterHostAndPathMethod(element.getProject());
        }
        if (rxRouterHostAndPathMethod == null) {
            rxRouterHostAndPathMethod = Util.getRxRouterHostAndPathMethod(element.getProject());
        }

        if (element instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) element;
            PsiElement targetPsiElement = psiReferenceExpression.resolve();
            if (targetPsiElement instanceof PsiMethod) {
                PsiMethod targetPsiMethod = (PsiMethod) targetPsiElement;
                boolean isHostMethod = targetPsiMethod.equals(routerRequestHostMethod) ||
                        targetPsiMethod.equals(routerHostMethod) ||
                        targetPsiMethod.equals(rxRouterHostMethod);
                boolean isHostAndPathMethod = targetPsiMethod.equals(routerRequestHostAndPathMethod) ||
                        targetPsiMethod.equals(routerHostAndPathMethod) ||
                        targetPsiMethod.equals(rxRouterHostAndPathMethod);
                // 如果是 host 方法或者是 hostAndPath 方法
                if (isHostMethod || isHostAndPathMethod) {
                    // 如果是一个有 host 和 path 方法 或者 hostAndPath 方法使用的
                    if (Util.isRouteAble(psiReferenceExpression)) {
                        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                                psiReferenceExpression,
                                psiReferenceExpression.getTextRange(),
                                routerLink, null,
                                new NavigationImpl(psiReferenceExpression), GutterIconRenderer.Alignment.RIGHT
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
    @Nullable
    private RouterInfo getRouterInfo(@NotNull PsiReferenceExpression psiReferenceExpression) {
        final RouterInfo info = Util.getHostAndPath(psiReferenceExpression);
        return info;
    }

    private class NavigationImpl implements GutterIconNavigationHandler {

        @NotNull
        private PsiReferenceExpression psiReferenceExpression;

        public NavigationImpl(@NotNull PsiReferenceExpression psiReferenceExpression) {
            this.psiReferenceExpression = psiReferenceExpression;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {

            final RouterInfo info = getRouterInfo(psiReferenceExpression);
            if (info == null) {
                return;
            }

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
                // Activity上的注解
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

    /**
     * 是否匹配 host 和 path
     *
     * @param routerInfo
     * @param psiAnnotation
     * @return
     */
    private boolean isMatchHostAndPath(@NotNull RouterInfo routerInfo, @NotNull PsiAnnotation psiAnnotation) {
        List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
        RouterInfo routerInfoTarget = new RouterInfo();
        String hostAndPath = null;
        for (JvmAnnotationAttribute attribute : attributes) {
            if (Constants.RouterAnnoHostName.equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                routerInfoTarget.host = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
            } else if (Constants.RouterAnnoPathName.equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                routerInfoTarget.path = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
            } else if (Constants.RouterAnnoHostAndPathName.equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                hostAndPath = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
            }
        }
        // 可能是默认值
        if (routerInfoTarget.host == null) {
            routerInfoTarget.host = Util.getHostFromRouterAnno(psiAnnotation);
        }
        routerInfoTarget.setHostAndPath(hostAndPath);

        if (routerInfoTarget.host == null || routerInfoTarget.path == null) {
            return false;
        }
        return routerInfoTarget.equals(routerInfo);
    }


}
