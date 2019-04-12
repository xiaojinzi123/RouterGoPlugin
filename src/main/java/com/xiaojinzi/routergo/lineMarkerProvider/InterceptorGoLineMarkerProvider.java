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
import com.xiaojinzi.routergo.bean.InterceptorInfo;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 拦截器的到达目标的实现
 */
public class InterceptorGoLineMarkerProvider implements LineMarkerProvider {

    private static PsiMethod routerInterceptorNameMethod = null;
    private static PsiMethod rxRouterInterceptorNameMethod = null;

    private static Icon interceptorLink = IconLoader.getIcon("interceptor_link.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {

        if (routerInterceptorNameMethod == null) {
            routerInterceptorNameMethod = Util.getRouterInterceptorNameMethod(element.getProject());
        }
        if (rxRouterInterceptorNameMethod == null) {
            rxRouterInterceptorNameMethod = Util.getRxRouterInterceptorNameMethod(element.getProject());
        }

        if (element instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) element;
            PsiElement targetPsiElement = psiReferenceExpression.resolve();
            if (targetPsiElement instanceof PsiMethod) {
                PsiMethod targetPsiMethod = (PsiMethod) targetPsiElement;
                if (targetPsiMethod.equals(routerInterceptorNameMethod) ||
                        targetPsiMethod.equals(rxRouterInterceptorNameMethod)) {
                    InterceptorInfo interceptorInfo = getInterceptorInfo(psiReferenceExpression);
                    if (interceptorInfo != null) {
                        PsiElement flagElement = psiReferenceExpression;
                        try {
                            flagElement = psiReferenceExpression.getLastChild();
                        } catch (Exception ignore) {
                            // ignore
                        }
                        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                                flagElement,
                                flagElement.getTextRange(),
                                interceptorLink, null,
                                new NavigationImpl(interceptorInfo), GutterIconRenderer.Alignment.RIGHT
                        );
                        return markerInfo;
                    }
                }
            }
        }

        return null;

    }

    @Nullable
    private InterceptorInfo getInterceptorInfo(@NotNull PsiReferenceExpression psiReferenceExpression) {
        InterceptorInfo info = new InterceptorInfo();
        if (info.interceptorName == null) {
            info.interceptorName = Util.getInterceptorName(psiReferenceExpression);
        }
        if (info.interceptorName == null) {
            return null;
        }
        return info;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

    public static class NavigationImpl implements GutterIconNavigationHandler {

        @NotNull
        private InterceptorInfo info;

        public NavigationImpl(@NotNull InterceptorInfo info) {
            this.info = info;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {

            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
            // 注解类@RouterAnno(.....)
            PsiClass interceptorAnnotation = javaPsiFacade.findClass(Constants.InterceptorAnnoClassName, allScope);
            if (interceptorAnnotation == null) {
                return;
            }
            List<PsiAnnotation> psiAnnotationList = new ArrayList<>();

            Collection<PsiClass> routerInterceptors = AnnotatedElementsSearch
                    .searchPsiClasses(interceptorAnnotation, allScope)
                    .findAll();

            for (PsiClass routerClass : routerInterceptors) {
                // 静态方法上的注解
                PsiAnnotation interceptorClassAnnotation = routerClass.getAnnotation(interceptorAnnotation.getQualifiedName());
                if (interceptorClassAnnotation != null) {
                    psiAnnotationList.add(interceptorClassAnnotation);
                }
            }

            PsiAnnotation targetAnno = null;

            for (int i = psiAnnotationList.size() - 1; i >= 0; i--) {
                PsiAnnotation psiAnnotation = psiAnnotationList.get(i);
                if (isMatchInterceptorName(info, psiAnnotation)) {
                    targetAnno = psiAnnotation;
                    break;
                }
            }

            if (targetAnno != null && targetAnno.canNavigate()) {
                targetAnno.navigate(true);
            }

        }

        private boolean isMatchInterceptorName(@NotNull InterceptorInfo info, @NotNull PsiAnnotation psiAnnotation) {
            List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
            String name = null;
            for (JvmAnnotationAttribute attribute : attributes) {
                if (Constants.InterceptorAnnoValueName.equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                    name = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
                }
            }
            if (name == null) {
                return false;
            }
            return name.equals(info.interceptorName);
        }

    }

}
