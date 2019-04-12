package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.ui.awt.RelativePoint;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.InterceptorInfo;
import com.xiaojinzi.routergo.bean.RouterInfo;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 */
public class RouterUsageLineMarkerProvider implements LineMarkerProvider {

    private static Icon routerLink = IconLoader.getIcon("router_link.png");
    private static Icon interceptorLink = IconLoader.getIcon("interceptor_link.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            PsiAnnotation routerAnno = psiClass.getAnnotation(Constants.RouterAnnoClassName);
            if (routerAnno == null) {
                return null;
            }
            RouterInfo routerInfo = getRouterInfoFromAnno(routerAnno);
            if (routerInfo != null) {
                routerInfo.psiElement = routerAnno;
                LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                        routerInfo.psiElement,
                        routerInfo.psiElement.getTextRange(),
                        routerLink, null,
                        new NavigationImpl(routerInfo), GutterIconRenderer.Alignment.RIGHT
                );
                return markerInfo;
            }
        } else if (element instanceof PsiReferenceExpression &&
                element.getParent() instanceof PsiNameValuePair &&
                element.getParent().getChildren()[0] instanceof PsiIdentifier &&
                "interceptorNames".equals(element.getParent().getChildren()[0].getText())) {
            return getInterceptorLineMarkerInfo(element);
        }else if (element instanceof PsiReferenceExpression &&
                element.getParent() instanceof PsiArrayInitializerMemberValue &&
                element.getParent().getParent() instanceof PsiNameValuePair &&
                element.getParent().getParent().getChildren()[0] instanceof PsiIdentifier &&
                "interceptorNames".equals(element.getParent().getParent().getChildren()[0].getText())) {
            return getInterceptorLineMarkerInfo(element);
        }
        return null;
    }

    @Nullable
    private LineMarkerInfo getInterceptorLineMarkerInfo (@NotNull PsiElement element) {
        InterceptorInfo interceptorInfo = new InterceptorInfo();
        interceptorInfo.interceptorName = Util.getStringValue(element);
        interceptorInfo.psiElement = element;
        if (interceptorInfo.interceptorName == null) {
            return null;
        }
        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                element,
                element.getTextRange(),
                interceptorLink, null,
                new InterceptorGoLineMarkerProvider.NavigationImpl(interceptorInfo), GutterIconRenderer.Alignment.RIGHT
        );
        return markerInfo;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

    @Nullable
    private RouterInfo getRouterInfoFromAnno(@NotNull PsiAnnotation routerAnno) {
        RouterInfo routerInfo = new RouterInfo();
        try {
            JvmAnnotationAttributeValue hostAttributeValue = routerAnno.findAttribute(Constants.RouterAnnoHostName).getAttributeValue();
            if (hostAttributeValue instanceof JvmAnnotationConstantValue) {
                routerInfo.host = (String) ((JvmAnnotationConstantValue) hostAttributeValue).getConstantValue();
            }
        } catch (Exception ignore) {
            // ignore
        }
        try {
            JvmAnnotationAttributeValue pathAttributeValue = routerAnno.findAttribute(Constants.RouterAnnoPathName).getAttributeValue();
            if (pathAttributeValue instanceof JvmAnnotationConstantValue) {
                routerInfo.path = (String) ((JvmAnnotationConstantValue) pathAttributeValue).getConstantValue();
            }
        } catch (Exception ignore) {
            // ignore
        }
        // 可能是默认值
        if (routerInfo.host == null) {
            routerInfo.host = Util.getHostFromRouterAnno(routerAnno);
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
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterRequestBuilderClassName, allScope);

            // 所有的引用都会在这里
            List<PsiReference> referenceList = new ArrayList<>();

            PsiMethod psiMethodRouter = (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostMethodName)[0];
            referenceList.addAll(MethodReferencesSearch.search(psiMethodRouter).findAll());

            PsiMethod psiMethodRxRouter = (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostMethodName)[0];
            referenceList.addAll(MethodReferencesSearch.search(psiMethodRxRouter).findAll());

            List<PsiReferenceExpression> referenceExpressionList = new ArrayList<>();

            // 过滤一下不是 PsiReferenceExpress
            for (PsiReference psiReference : referenceList) {
                if (psiReference instanceof PsiReferenceExpression) {
                    referenceExpressionList.add((PsiReferenceExpression) psiReference);
                }
            }
            referenceList = null;

            Set<RouterInfo> referenceExpressionListResultSet = new HashSet<>();

            for (PsiReferenceExpression psiReferenceExpression : referenceExpressionList) {
                RouterInfo routerInfo = getRouterInfoFromPsiReferenceExpression(psiReferenceExpression);
                if (routerInfo != null) {
                    routerInfo.psiElement = psiReferenceExpression;
                    referenceExpressionListResultSet.add(routerInfo);
                }
            }

            List<RouterInfo> referenceExpressionListResultList = new ArrayList<>(referenceExpressionListResultSet);

            // 过滤 host 和 path 不一样的
            for (int i = referenceExpressionListResultList.size() - 1; i >= 0; i--) {
                RouterInfo routerInfo = referenceExpressionListResultList.get(i);
                if (info.host.equals(routerInfo.host) && info.path.equals(routerInfo.path)) {
                } else {
                    referenceExpressionListResultList.remove(i);
                }
            }

            if (referenceExpressionListResultList.size() == 1) {
                PsiElement targetPsiElement = referenceExpressionListResultList.get(0).psiElement;
                if (targetPsiElement instanceof Navigatable && ((Navigatable) targetPsiElement).canNavigate()) {
                    ((Navigatable) targetPsiElement).navigate(true);
                }
            } else if (referenceExpressionListResultList.size() > 1) {
                List<GotoRelatedItem> gotoRelatedItemList = new ArrayList<>();
                for (RouterInfo routerInfo : referenceExpressionListResultList) {
                    gotoRelatedItemList.add(new GotoRelatedItem(routerInfo.psiElement));
                }
                RelativePoint relativePoint = new RelativePoint(e);
                NavigationUtil.getRelatedItemsPopup(gotoRelatedItemList, "Go To Relative Router")
                        .show(relativePoint);
            } else {
                Messages.showErrorDialog("不好意思,没找到", "来自小金子的警告");
            }

        }

    }

    @Nullable
    private RouterInfo getRouterInfoFromPsiReferenceExpression(@NotNull PsiReferenceExpression psiReferenceExpression) {
        RouterInfo routerInfo = new RouterInfo();
        Util.getHostAndPath(psiReferenceExpression, routerInfo);
        if (routerInfo.host == null || routerInfo.path == null) {
            return null;
        }
        return routerInfo;
    }

}
