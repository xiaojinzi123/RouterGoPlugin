package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.ui.awt.RelativePoint;
import com.xiaojinzi.routergo.bean.InterceptorAnnoInfo;
import com.xiaojinzi.routergo.bean.InterceptorInfo;
import com.xiaojinzi.routergo.util.KtUtil;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拦截器的使用查询,有以下几个方面
 * 1. Router 代码使用方面的
 * 1.1 分为 Java 使用和 Kotlin 的使用
 * 2. RouterAnno 注解使用方面的
 * 2.1 分为 Java 的注解和 Kotlin 的注解
 */
public class InterceptorUsageNavigation implements GutterIconNavigationHandler {

    @NotNull
    private PsiAnnotation interceptorAnno;

    /**
     * For Kotlin
     */
    @NotNull
    private KtAnnotationEntry targetPsiAnnotation;

    public InterceptorUsageNavigation(@NotNull PsiAnnotation interceptorAnno) {
        this.interceptorAnno = interceptorAnno;
    }

    public InterceptorUsageNavigation(@NotNull KtAnnotationEntry targetPsiAnnotation) {
        this.targetPsiAnnotation = targetPsiAnnotation;
    }

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {

        // 拿到拦截器注解的信息
        InterceptorAnnoInfo tempInterceptorAnnoInfo = null;
        if (interceptorAnno != null) {
            tempInterceptorAnnoInfo = Util.getInterceptorInfoFromInterceptorAnno(interceptorAnno);
        }
        if (targetPsiAnnotation != null) {
            tempInterceptorAnnoInfo = KtUtil.getInterceptorInfoFromInterceptorAnno(targetPsiAnnotation);
        }
        if (tempInterceptorAnnoInfo == null) {
            return;
        }

        final InterceptorAnnoInfo interceptorAnnoInfo = tempInterceptorAnnoInfo;

        // 所有调用 Router...interceptorNames 方法的所有引用
        List<PsiReference> interceptorMethodReferences = Util.getAllInterceptorMethodReferences(elt.getProject());
        // 包括 java 和 kotlin 使用 RouterAnno
        List<PsiAnnotation> psiAnnotationList = Util.getAllRouterAnno(elt.getProject());

        List<InterceptorInfo> interceptorInfoList = new ArrayList<>();

        for (PsiReference reference : interceptorMethodReferences) {
            InterceptorInfo interceptorInfo = new InterceptorInfo();
            interceptorInfo.psiElement = reference.getElement();
            if (reference instanceof PsiReferenceExpression) {
                interceptorInfo.interceptorNames = Util.getInterceptorNames((PsiReferenceExpression) reference);
            } else if (reference instanceof KtSimpleNameReference) {
                interceptorInfo.interceptorNames = KtUtil.getInterceptorNames((KtSimpleNameReference)reference);
            }
            if (interceptorInfo.interceptorNames != null) {
                // interceptorInfoList.add(interceptorInfo);
            }
        }

        // 获取每一个 RouterAnno 的拦截器名称集合
        for (PsiAnnotation psiAnnotation : psiAnnotationList) {
            InterceptorInfo interceptorInfo = new InterceptorInfo();
            interceptorInfo.psiElement = psiAnnotation;
            interceptorInfo.interceptorNames = Util.getInterceptorNamesFromRouterAnno(psiAnnotation);
            interceptorInfoList.add(interceptorInfo);
        }

        // 先过滤一波
        interceptorInfoList = interceptorInfoList.stream()
                .filter(item -> item.interceptorNames != null && !item.interceptorNames.isEmpty())
                .collect(Collectors.toList());

        outter:
        for (int i = interceptorInfoList.size() - 1; i >= 0; i--) {
            InterceptorInfo interceptorInfo = interceptorInfoList.get(i);
            // 如果名称对上,就返回 true
            inner:
            for (String interceptorName : interceptorInfo.interceptorNames) {
                if (interceptorAnnoInfo.name.equals(interceptorName)) {
                    continue outter;
                }
            }
            interceptorInfoList.remove(i);
        }

        if (interceptorInfoList.size() == 1) {
            PsiElement targetPsiElement = interceptorInfoList.get(0).psiElement;
            if (targetPsiElement instanceof Navigatable && ((Navigatable) targetPsiElement).canNavigate()) {
                ((Navigatable) targetPsiElement).navigate(true);
            }
        } else if (interceptorInfoList.size() > 1) {
            interceptorInfoList =
                    new SortHelper<>(
                            interceptorInfoList,
                            item -> item.psiElement
                    ).getSortedList();
            List<GotoRelatedItem> gotoRelatedItemList = new ArrayList<>();
            for (InterceptorInfo interceptorInfo : interceptorInfoList) {
                gotoRelatedItemList.add(new GotoRelatedItem(interceptorInfo.psiElement));
            }
            RelativePoint relativePoint = new RelativePoint(e);
            NavigationUtil.getRelatedItemsPopup(gotoRelatedItemList, "Go To Relative Interceptor")
                    .show(relativePoint);
        } else {
            Messages.showErrorDialog("不好意思,没找到", "来自小金子的警告");
        }

    }

}
