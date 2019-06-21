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
import com.xiaojinzi.routergo.bean.InterceptorAnnoInfo;
import com.xiaojinzi.routergo.bean.InterceptorInfo;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * 使用 RouterAnno 的地方的显示一个图标,可以展示所有用到这个界面 url 的选项
 */
public class InterceptorUsageLineMarkerProvider implements LineMarkerProvider {

    private static Icon interceptorLink = IconLoader.getIcon("interceptor_link.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            PsiAnnotation interceptorAnno = psiClass.getAnnotation(Constants.InterceptorAnnoClassName);
            if (interceptorAnno == null) {
                return null;
            }
            PsiElement targetPsiElement = interceptorAnno;
            LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                    targetPsiElement,
                    targetPsiElement.getTextRange(),
                    interceptorLink, null,new InterceptorUsageNavigation(interceptorAnno), GutterIconRenderer.Alignment.RIGHT
            );
            return markerInfo;
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    }

    /*private class NavigationImpl implements GutterIconNavigationHandler {

        @NotNull
        private PsiAnnotation interceptorAnno;

        public NavigationImpl(@NotNull PsiAnnotation interceptorAnno) {
            this.interceptorAnno = interceptorAnno;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {

            // 拿到拦截器注解的信息
            InterceptorAnnoInfo interceptorAnnoInfo = getInterceptorInfoFromAnno(interceptorAnno);
            if (interceptorAnnoInfo == null) {
                return;
            }

            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());

            PsiClass routerAnnoBuilderClass = javaPsiFacade.findClass(Constants.RouterAnnoClassName, allScope);
            PsiClass routerBuilderClass = javaPsiFacade.findClass(Constants.RouterBuilderClassName, allScope);
            // 这个可能为空
            PsiClass rxRouterBuilderClass = javaPsiFacade.findClass(Constants.RxRouterBuilderClassName, allScope);

            // 所有方法的引用都会在这里
            List<PsiReference> referenceMethodList = new ArrayList<>();
            // 所有使用 RouterAnno 注解中的 interceptorNames 属性方法的
            List<PsiReference> referenceAnnoMethodList = new ArrayList<>();

            // 搜索 使用中的 interceptorNames 的方法
            PsiMethod psiMethodRouter = (PsiMethod) routerBuilderClass.findMethodsByName(Constants.RouterInterceptorNameMethodName)[0];
            referenceMethodList.addAll(MethodReferencesSearch.search(psiMethodRouter).findAll());

            // 如果用户依赖了 rx 版本的库,那么才去寻找对应的方法
            if (rxRouterBuilderClass != null) {
                PsiMethod psiMethodRxRouter = (PsiMethod) rxRouterBuilderClass.findMethodsByName(Constants.RouterInterceptorNameMethodName)[0];
                referenceMethodList.addAll(MethodReferencesSearch.search(psiMethodRxRouter).findAll());
            }

            // 寻找注解 RouterAnno 的
            PsiAnnotationMethod psiAnnotationMethod = (PsiAnnotationMethod) routerAnnoBuilderClass.findMethodsByName(Constants.RouterAnnoInterceptorName)[0];
            referenceAnnoMethodList.addAll(MethodReferencesSearch.search(psiAnnotationMethod).findAll());

            // 过滤后的结果
            List<PsiReferenceExpression> referenceExpressionList = new ArrayList<>();
            List<PsiNameValuePair> nameValuePairList = new ArrayList<>();
            List<KtAnnotationEntry> ktAnnotationEntryList = new ArrayList<>();

            // 过滤一下不是 PsiReferenceExpress
            for (PsiReference psiReference : referenceMethodList) {
                if (psiReference instanceof PsiReferenceExpression) {
                    referenceExpressionList.add((PsiReferenceExpression) psiReference);
                }
            }
            // 过滤一下不是注解中使用到的
            for (PsiReference psiReference : referenceAnnoMethodList) {
                if (psiReference.getElement() != null) {
                    if (psiReference.getElement().getParent() instanceof PsiNameValuePair) {
                        nameValuePairList.add((PsiNameValuePair) psiReference.getElement().getParent());
                    } else if (psiReference.getElement().getParent().getParent().getParent().getParent() instanceof KtAnnotationEntry) {
                        ktAnnotationEntryList.add((KtAnnotationEntry) psiReference.getElement().getParent().getParent().getParent().getParent());
                    }
                }
            }

            referenceMethodList = null;
            referenceAnnoMethodList = null;

            // 生成最终的引用列表
            Set<InterceptorInfo> referenceExpressionListResultSet = new HashSet<>();

            for (PsiReferenceExpression psiReferenceExpression : referenceExpressionList) {
                InterceptorInfo interceptorInfo = getInterceptorInfoFromPsiReferenceExpression(psiReferenceExpression);
                if (interceptorInfo != null) {
                    interceptorInfo.psiElement = psiReferenceExpression;
                    referenceExpressionListResultSet.add(interceptorInfo);
                }
            }

            for (PsiNameValuePair psiNameValuePair : nameValuePairList) {
                InterceptorInfo interceptorInfo = getInterceptorInfoFromPsiNameValuePair(psiNameValuePair);
                if (interceptorInfo != null) {
                    interceptorInfo.psiElement = psiNameValuePair;
                    referenceExpressionListResultSet.add(interceptorInfo);
                }
            }

            // 最终和传进来的 InterceptorInfo 名称匹配到
            List<InterceptorInfo> referenceExpressionListResultList = new ArrayList<>(referenceExpressionListResultSet);

            for (int i = referenceExpressionListResultList.size() - 1; i >= 0; i--) {
                InterceptorInfo interceptorInfo = referenceExpressionListResultList.get(i);
                boolean isExistOne = false;
                for (String interceptorName : interceptorInfo.interceptorNames) {
                    if (interceptorAnnoInfo.name.equals(interceptorName)) {
                        isExistOne = true;
                        break;
                    }
                }
                if (!isExistOne) {
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
                for (InterceptorInfo interceptorInfo : referenceExpressionListResultList) {
                    gotoRelatedItemList.add(new GotoRelatedItem(interceptorInfo.psiElement));
                }
                RelativePoint relativePoint = new RelativePoint(e);
                NavigationUtil.getRelatedItemsPopup(gotoRelatedItemList, "Go To Relative Interceptor")
                        .show(relativePoint);
            } else {
                Messages.showErrorDialog("不好意思,没找到", "来自小金子的警告");
            }

        }

    }*/

    @Nullable
    private InterceptorInfo getInterceptorInfoFromPsiReferenceExpression(@NotNull PsiReferenceExpression psiReferenceExpression) {
        InterceptorInfo interceptorInfo = new InterceptorInfo();
        interceptorInfo.interceptorNames = Util.getInterceptorNames(psiReferenceExpression);
        if (interceptorInfo.interceptorNames == null) {
            return null;
        }
        return interceptorInfo;
    }

    @Nullable
    private InterceptorInfo getInterceptorInfoFromPsiNameValuePair(@NotNull PsiNameValuePair psiNameValuePair) {
        InterceptorInfo interceptorInfo = new InterceptorInfo();
        // 一个 RouterAnno 注解中的 interceptorNames 中的拦截器的名称列表
        List<String> interceptorNames = new ArrayList<String>();
        PsiElement lastChild = psiNameValuePair.getLastChild();
        if (lastChild instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue initializerMemberValue = (PsiArrayInitializerMemberValue) lastChild;
            for (PsiAnnotationMemberValue initializer : initializerMemberValue.getInitializers()) {
                String name = Util.getStringValue(initializer);
                if (name != null) {
                    interceptorNames.add(name);
                }
            }
        } else if (lastChild != null) {
            String name = Util.getStringValue(lastChild);
            if (name != null) {
                interceptorNames.add(name);
            }
        }
        if (interceptorNames.size() > 0) {
            interceptorInfo.interceptorNames = interceptorNames;
        }
        if (interceptorInfo.interceptorNames == null) {
            return null;
        }
        return interceptorInfo;
    }

}
