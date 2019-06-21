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
 *
 * @TODO:这里定位interceptorNames里面的拦截器名字的时候需要细化
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

        // 如果是 ..interceptorNames("xxx","fff") 中的 参数列表中的元素
        if (element.getParent() != null && isRouterInterceptorNamesMethod(element.getParent().getPrevSibling())) {
            String interceptorName = Util.getStringValue(element);
            if (interceptorName != null) {
                // 拦截器名称的信息
                PsiElement targetPsiElement = element;
                NavigationImpl navigation = new NavigationImpl(targetPsiElement);
                LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                        targetPsiElement,
                        targetPsiElement.getTextRange(),
                        interceptorLink, null,
                        navigation, GutterIconRenderer.Alignment.LEFT
                );
                return markerInfo;
            }
        }

        return null;
    }

    /**
     * @param psiReferenceExpression Router......interceptorNames("xxx","fff") 拿 "xxx","fff"
     * @return
     */
    @Nullable
    private InterceptorInfo getInterceptorInfo(@NotNull PsiReferenceExpression psiReferenceExpression) {
        InterceptorInfo info = new InterceptorInfo();
        if (info.interceptorNames == null) {
            info.interceptorNames = Util.getInterceptorNames(psiReferenceExpression);
        }
        if (info.interceptorNames == null) {
            return null;
        }
        return info;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

        /*result.clear();
        for (PsiElement element : elements) {
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
                            for (int i = interceptorInfo.interceptorNames.size() - 1; i >= 0; i--) {
                                String interceptorName = interceptorInfo.interceptorNames.get(i);
                                InterceptorNavagationInfo interceptorNavagationInfo = new InterceptorNavagationInfo(flagElement, interceptorName);
                                NavigationImpl navigation = new NavigationImpl(interceptorNavagationInfo);
                                LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                                        flagElement,
                                        flagElement.getTextRange(),
                                        interceptorLink, null,
                                        navigation, GutterIconRenderer.Alignment.RIGHT
                                );
                                result.add(markerInfo);
                            }
                        }
                    }
                }
            }
        }*/

    }

    /**
     * 如果是 ...interceptorNames(xxx,fff) 方法
     * @param psiElement
     * @return
     */
    private boolean isRouterInterceptorNamesMethod(@Nullable PsiElement psiElement) {
        if (psiElement instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) psiElement;
            PsiElement targetPsiElement = psiReferenceExpression.resolve();
            if (targetPsiElement instanceof PsiMethod) {
                PsiMethod targetPsiMethod = (PsiMethod) targetPsiElement;
                if (targetPsiMethod.equals(routerInterceptorNameMethod) ||
                        targetPsiMethod.equals(rxRouterInterceptorNameMethod)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class NavigationImpl implements GutterIconNavigationHandler {

        /**
         * 可能是 Router.with(xxx)...interceptorNames(xxx)
         * 也可能是 @RouterAnno(interceptorNames(xxxx))
         */
        @NotNull
        private PsiElement psiElement;

        public NavigationImpl(@NotNull PsiElement psiElement) {
            this.psiElement = psiElement;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {

            String interceptorName = Util.getStringValue(psiElement);

            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
            // 注解类@InterceptorAnno(.....)
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
                if (isMatchInterceptorName(interceptorName, psiAnnotation)) {
                    targetAnno = psiAnnotation;
                    break;
                }
            }

            if (targetAnno != null && targetAnno.canNavigate()) {
                targetAnno.navigate(true);
            }

        }

        /**
         * @param interceptorName
         * @param psiAnnotation   @InterceptorAnno
         * @return
         */
        private boolean isMatchInterceptorName(@NotNull String interceptorName, @NotNull PsiAnnotation psiAnnotation) {
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
            return name.equals(interceptorName);
        }

    }

}
