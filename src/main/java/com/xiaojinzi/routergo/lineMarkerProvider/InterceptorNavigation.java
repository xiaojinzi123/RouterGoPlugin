package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.util.KtUtil;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InterceptorNavigation implements GutterIconNavigationHandler {

    /**
     * 是一个字符串的表达式,可以通过 {@link Util#getStringValue(PsiElement)} 获取到具体的拦截器名称
     * 可能是 Router.with(xxx)...interceptorNames(xxx) 的 xxx 部分
     * 也可能是 @RouterAnno(interceptorNames(xxxx)) 的 xxx 部分
     * <p>
     * 上面的是描述 java 的形式,也可能是一个 kotlin 的调用
     */
    @NotNull
    private PsiElement psiElement;

    /**
     * for Java
     *
     * @param psiElement 是一个字符串表达式,
     *                   可能是 kotlin 的 https://xiaojinzi.oss-cn-shanghai.aliyuncs.com/blogImages/20190623171723.png
     *                   也可能是 java 的 https://xiaojinzi.oss-cn-shanghai.aliyuncs.com/blogImages/20190623170748.png
     */
    public InterceptorNavigation(@NotNull PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {

        String interceptorName = Util.getStringValue(psiElement);
        if (interceptorName == null || "".equals(interceptorName)) {
            interceptorName = KtUtil.getStringValue(psiElement);
        }

        if (interceptorName == null || "".equals(interceptorName)) {
            return;
        }

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
