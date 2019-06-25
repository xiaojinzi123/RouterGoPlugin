package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.RouterInfo;
import com.xiaojinzi.routergo.util.KtUtil;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RouterNavigation implements GutterIconNavigationHandler {

    @NotNull
    private PsiReferenceExpression psiReferenceExpression;
    @NotNull
    private KtSimpleNameExpression ktSimpleNameExpression;

    public RouterNavigation(@NotNull PsiReferenceExpression psiReferenceExpression) {
        this.psiReferenceExpression = psiReferenceExpression;
    }

    public RouterNavigation(@NotNull KtSimpleNameExpression ktSimpleNameExpression) {
        this.ktSimpleNameExpression = ktSimpleNameExpression;
    }

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {

        RouterInfo info = null;
        if (psiReferenceExpression != null) {
            info = Util.getRouterInfoFromPsiReferenceExpression(psiReferenceExpression);
        } else if (ktSimpleNameExpression != null) {
            info = KtUtil.getRouterInfoFromKtNameReferenceExpression(ktSimpleNameExpression);
        }

        if (info == null) {
            Messages.showErrorDialog("没有找到 host 和 path 等信息,请检查您的代码", "来自小金子的警告");
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
            routerInfoTarget.host = Util.getHostValueFromModule(psiAnnotation);
        }
        routerInfoTarget.setHostAndPath(hostAndPath);

        if (routerInfoTarget.host == null || routerInfoTarget.path == null) {
            return false;
        }
        return routerInfoTarget.equals(routerInfo);
    }

}
