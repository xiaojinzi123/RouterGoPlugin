package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.ui.awt.RelativePoint;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.RouterAnnoInfo;
import com.xiaojinzi.routergo.bean.RouterInfo;
import com.xiaojinzi.routergo.util.KtUtil;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RouterUsageNavigation implements GutterIconNavigationHandler {

    @NotNull
    private PsiAnnotation targetPsiAnnotation;

    @NotNull
    private KtAnnotationEntry ktAnnotationEntry;

    public RouterUsageNavigation(@NotNull KtAnnotationEntry targetPsiAnnotation) {
        this.ktAnnotationEntry = targetPsiAnnotation;
    }

    public RouterUsageNavigation(@NotNull PsiAnnotation targetPsiAnnotation) {
        this.targetPsiAnnotation = targetPsiAnnotation;
    }

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {

        RouterInfo targetRouterInfo = null;

        if (targetPsiAnnotation != null) {
            targetRouterInfo = Util.getRouterInfoFromAnno(targetPsiAnnotation);
        } else if (ktAnnotationEntry != null) {
            targetRouterInfo = KtUtil.getRouterInfo(ktAnnotationEntry);
        }

        if (targetRouterInfo == null) {
            return;
        }

        GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());

        // 寻找到 RouterRequest.Builder 这个类
        PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterRequestBuilderClassName, allScope);

        // 找到 host 方法和 hostAndPath 方法
        PsiMethod psiHostMethodRouter = (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostMethodName)[0];
        PsiMethod psiHostAndPathMethodRouter = (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostAndPathMethodName)[0];

        // 会找到所有的 RouterRequest.Builder.host方法的引用都会在这里
        List<PsiReference> referenceList = new ArrayList<>();

        // 搜集搜索的结果
        referenceList.addAll(MethodReferencesSearch.search(psiHostMethodRouter).findAll());
        referenceList.addAll(MethodReferencesSearch.search(psiHostAndPathMethodRouter).findAll());

        List<PsiReferenceExpression> referenceExpressionList = new ArrayList<>();
        List<KtSimpleNameReference> ktReferenceExpressionList = new ArrayList<>();

        // 过滤出 Java 的引用和 kotlin 的引用
        for (PsiReference psiReference : referenceList) {
            if (psiReference instanceof PsiReferenceExpression) {
                referenceExpressionList.add((PsiReferenceExpression) psiReference);
            } else if (psiReference instanceof KtSimpleNameReference) {
                ktReferenceExpressionList.add((KtSimpleNameReference) psiReference);
            }
        }

        referenceList = null;

        Set<RouterAnnoInfo> referenceExpressionListResultSet = new HashSet<>();

        for (PsiReferenceExpression psiReferenceExpression : referenceExpressionList) {
            RouterInfo routerInfo = Util.getRouterInfoFromPsiReferenceExpression(psiReferenceExpression);
            RouterAnnoInfo routerAnnoInfo = null;
            if (routerInfo != null) {
                routerAnnoInfo = new RouterAnnoInfo(routerInfo);
            }
            if (routerInfo != null) {
                routerAnnoInfo.psiElement = psiReferenceExpression;
                referenceExpressionListResultSet.add(routerAnnoInfo);
            }
        }

        for (KtSimpleNameReference ktSimpleNameReference : ktReferenceExpressionList) {
            RouterInfo routerInfo = KtUtil.getRouterInfoFromKtNameReferenceExpression(ktSimpleNameReference.getExpression());
            RouterAnnoInfo routerAnnoInfo = null;
            if (routerInfo != null) {
                routerAnnoInfo = new RouterAnnoInfo(routerInfo);
            }
            if (routerInfo != null) {
                routerAnnoInfo.psiElement = ktSimpleNameReference.getElement();
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
