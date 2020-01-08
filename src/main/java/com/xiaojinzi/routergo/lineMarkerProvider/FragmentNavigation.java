package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
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

public class FragmentNavigation implements GutterIconNavigationHandler {

    @NotNull
    private final String mFragmentFalg;

    public FragmentNavigation(@NotNull String fragmentFalg) {
        this.mFragmentFalg = fragmentFalg;
    }

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {

        if (mFragmentFalg == null || "".equals(mFragmentFalg)) {
            return;
        }

        GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
        // 注解类@RouterAnno(.....)
        PsiClass fragmentAnnotation = javaPsiFacade.findClass(Constants.FragmentAnnoClassName, allScope);
        if (fragmentAnnotation == null) {
            return;
        }

        Collection<PsiClass> fragmentPsiClasses = AnnotatedElementsSearch
                .searchPsiClasses(fragmentAnnotation, allScope)
                .findAll();

        List<PsiAnnotation> psiAnnotationList = new ArrayList<>();

        for (PsiClass fragmentClass : fragmentPsiClasses) {
            // Fragment 上的注解
            PsiAnnotation routerClassAnnotation = fragmentClass.getAnnotation(fragmentAnnotation.getQualifiedName());
            if (routerClassAnnotation != null) {
                psiAnnotationList.add(routerClassAnnotation);
            }
        }

        PsiAnnotation targetAnno = null;

        for (int i = psiAnnotationList.size() - 1; i >= 0; i--) {
            PsiAnnotation psiAnnotation = psiAnnotationList.get(i);
            List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
            String fragmentFlag = null;
            for (JvmAnnotationAttribute attribute : attributes) {
                if (Constants.FragmentAnnoValueName.equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                    fragmentFlag = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
                }
            }
            if (mFragmentFalg.equals(fragmentFlag)) {
                targetAnno = psiAnnotation;
                break;
            }
        }

        if (targetAnno != null && targetAnno instanceof Navigatable) {
            ((Navigatable) targetAnno).navigate(true);
        }

    }

}
