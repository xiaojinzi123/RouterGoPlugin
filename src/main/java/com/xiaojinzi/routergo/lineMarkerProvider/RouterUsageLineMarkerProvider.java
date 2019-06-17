package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
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
import com.xiaojinzi.routergo.bean.RouterAnnoInfo;
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

    private static Icon routerLink = IconLoader.getIcon("logo.png");
    private static Icon interceptorLink = IconLoader.getIcon("interceptor_link.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiClass || element instanceof PsiMethod) {
            PsiModifierListOwner psiModifierListOwner = (PsiModifierListOwner) element;
            PsiAnnotation routerAnno = psiModifierListOwner.getAnnotation(Constants.RouterAnnoClassName);
            if (routerAnno == null) {
                return null;
            }
            RouterInfo routerInfo = getRouterInfoFromAnno(routerAnno);
            if (routerInfo != null) {
                PsiAnnotation targetPsiAnnotation = routerAnno;
                LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                        targetPsiAnnotation,
                        targetPsiAnnotation.getTextRange(),
                        routerLink, null,
                        new NavigationImpl(targetPsiAnnotation), GutterIconRenderer.Alignment.RIGHT
                );
                return markerInfo;
            }
        } else if ((element instanceof PsiReferenceExpression || element instanceof PsiLiteralExpression) &&
                element.getParent() instanceof PsiNameValuePair &&
                element.getParent().getChildren()[0] instanceof PsiIdentifier &&
                "interceptorNames".equals(element.getParent().getChildren()[0].getText())) {
            // 拦截器
            return getInterceptorLineMarkerInfo(element);
        }else if ((element instanceof PsiReferenceExpression || element instanceof PsiLiteralExpression) &&
                element.getParent() instanceof PsiArrayInitializerMemberValue &&
                element.getParent().getParent() instanceof PsiNameValuePair &&
                element.getParent().getParent().getChildren()[0] instanceof PsiIdentifier &&
                "interceptorNames".equals(element.getParent().getParent().getChildren()[0].getText())) {
            // 拦截器
            return getInterceptorLineMarkerInfo(element);
        }
        return null;
    }

    @Nullable
    private LineMarkerInfo getInterceptorLineMarkerInfo (@NotNull PsiElement element) {
        LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                element,
                element.getTextRange(),
                interceptorLink, null,
                new InterceptorGoLineMarkerProvider.NavigationImpl(element), GutterIconRenderer.Alignment.RIGHT
        );
        return markerInfo;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

    @Nullable
    private RouterInfo getRouterInfoFromAnno(@NotNull PsiAnnotation routerAnno) {
        RouterInfo routerInfo = new RouterInfo();
        String hostAndPath = null;
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
        try {
            JvmAnnotationAttributeValue pathAttributeValue = routerAnno.findAttribute(Constants.RouterAnnoHostAndPathName).getAttributeValue();
            if (pathAttributeValue instanceof JvmAnnotationConstantValue) {
                hostAndPath = (String) ((JvmAnnotationConstantValue) pathAttributeValue).getConstantValue();
            }
        } catch (Exception ignore) {
            // ignore
        }
        // 可能是默认值
        if (routerInfo.host == null) {
            routerInfo.host = Util.getHostFromRouterAnno(routerAnno);
        }
        routerInfo.setHostAndPath(hostAndPath);
        if (routerInfo.host == null || routerInfo.path == null) {
            return null;
        }
        return routerInfo;
    }

    private class NavigationImpl implements GutterIconNavigationHandler {

        @NotNull
        private PsiAnnotation targetPsiAnnotation;

        public NavigationImpl(@NotNull PsiAnnotation targetPsiAnnotation) {
            this.targetPsiAnnotation = targetPsiAnnotation;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {

            RouterInfo targetRouterInfo = getRouterInfoFromAnno(targetPsiAnnotation);
            if (targetRouterInfo == null) {
                return;
            }

            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());

            // 寻找到 RouterRequest.Builder 这个类
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterRequestBuilderClassName, allScope);

            // 会找到所有的 RouterRequest.Builder.host方法的引用都会在这里
            List<PsiReference> referenceList = new ArrayList<>();

            PsiMethod psiHostMethodRouter = (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostMethodName)[0];
            referenceList.addAll(MethodReferencesSearch.search(psiHostMethodRouter).findAll());

            PsiMethod psiHostAndPathMethodRouter = (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostAndPathMethodName)[0];
            referenceList.addAll(MethodReferencesSearch.search(psiHostAndPathMethodRouter).findAll());

            List<PsiReferenceExpression> referenceExpressionList = new ArrayList<>();
            // 过滤一下不是 PsiReferenceExpress
            for (PsiReference psiReference : referenceList) {
                if (psiReference instanceof PsiReferenceExpression) {
                    referenceExpressionList.add((PsiReferenceExpression) psiReference);
                }
            }
            referenceList = null;
            Set<RouterAnnoInfo> referenceExpressionListResultSet = new HashSet<>();
            for (PsiReferenceExpression psiReferenceExpression : referenceExpressionList) {
                RouterInfo routerInfo = getRouterAnnoInfoFromPsiReferenceExpression(psiReferenceExpression);
                RouterAnnoInfo routerAnnoInfo = null;
                if (routerInfo != null) {
                    routerAnnoInfo = new RouterAnnoInfo(routerInfo);
                }
                if (routerInfo != null) {
                    routerAnnoInfo.psiElement = psiReferenceExpression;
                    referenceExpressionListResultSet.add(routerAnnoInfo);
                }
            }

            List<RouterAnnoInfo> referenceExpressionListResultList = new ArrayList<>(referenceExpressionListResultSet);

            // 过滤 host 和 path 不一样的
            for (int i = referenceExpressionListResultList.size() - 1; i >= 0; i--) {
                RouterInfo routerInfo = referenceExpressionListResultList.get(i);
                if (targetRouterInfo.host.equals(routerInfo.host) && targetRouterInfo.path.equals(routerInfo.path)) {
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
                for (RouterAnnoInfo routerAnnoInfo : referenceExpressionListResultList) {
                    gotoRelatedItemList.add(new GotoRelatedItem(routerAnnoInfo.psiElement));
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
    private RouterInfo getRouterAnnoInfoFromPsiReferenceExpression(@NotNull PsiReferenceExpression psiReferenceExpression) {
        RouterInfo routerInfo = Util.getHostAndPath(psiReferenceExpression);
        return routerInfo;
    }

}
