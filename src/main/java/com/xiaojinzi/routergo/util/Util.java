package com.xiaojinzi.routergo.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.RouterInfo;
import org.jetbrains.android.dom.manifest.Application;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static String getHostFromRouterAnno(@NotNull PsiElement psiAnnotation) {
        String host = null;
        // 找到对应的 module
        Module module = ModuleUtil.findModuleForPsiElement(psiAnnotation);
        // 这里读取GroovyFile文件中的 host
        Application manifestApplication = AndroidFacet.getInstance(module).getManifest().getApplication();
        if (manifestApplication.getMetaDatas() != null) {
            List<Object> metaDatas = new ArrayList<>(manifestApplication.getMetaDatas());
            for (Object metaData : metaDatas) {
                host = readHostFromMetaData(metaData);
                if (host != null) {
                    break;
                }
            }
        }
        return host;
    }

    /**
     * ((MetaData)metaDatas.get(0)).getValue().getStringValue();
     *
     * @param metaData
     * @return
     */
    @Nullable
    private static String readHostFromMetaData(Object metaData) {
        try {
            Object valueObj = metaData.getClass().getDeclaredMethod("getValue").invoke(metaData);
            String hostValue = (String) valueObj.getClass().getDeclaredMethod("getStringValue").invoke(valueObj);
            return hostValue;
        } catch (Exception e) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRouterRequestHostMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterRequestBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostMethodName)[0];
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRouterHostMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostMethodName)[0];
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRxRouterHostMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RxRouterBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostMethodName)[0];
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRouterInterceptorNameMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterInterceptorNameMethodName)[0];
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRxRouterInterceptorNameMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RxRouterBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterInterceptorNameMethodName)[0];
        } catch (Exception ignore) {
        }
        return null;
    }

    public static String getInterceptorName(@NotNull PsiReferenceExpression psiReferenceExpression) {
        try {
            PsiElement psiInterceptorNameElement = psiReferenceExpression.getParent().getChildren()[1].getChildren()[1];
            return getStringValue(psiInterceptorNameElement);
        } catch (Exception ignore) {
            // ignore
        }
        return null;
    }

    /**
     * .....host("order") 中拿到 "order" 字符串
     *
     * @param psiReferenceExpression
     * @param info
     */
    public static void getHostAndPath(@NotNull PsiReferenceExpression psiReferenceExpression, @NotNull final RouterInfo info) {
        try {
            PsiElement psiHostElement = psiReferenceExpression.getParent().getChildren()[1].getChildren()[1];
            PsiElement psiPathElement = psiReferenceExpression.getParent().getParent().getParent().getChildren()[1].getChildren()[1];
            info.host = getStringValue(psiHostElement);
            info.path = getStringValue(psiPathElement);
        } catch (Exception ignore) {
            // ignore
        }
    }

    /**
     * 尝试获取一个元素的 String 文本
     *
     * @param psiElement
     * @return
     */
    public static String getStringValue(@NotNull PsiElement psiElement) {
        String value = null;
        try {
            value = (String) ((PsiLiteralExpression) ((PsiField) ((PsiReferenceExpression) psiElement).resolve()).getInitializer()).getValue();
            return value;
        } catch (Exception ignore) {
            // ignore
        }
        if (psiElement instanceof PsiLiteralExpression) {
            // 字符串表达式
            PsiLiteralExpression psiLiteralExpression = (PsiLiteralExpression) psiElement;
            value = (String) psiLiteralExpression.getValue();
            return value;
        }
        return value;
    }

}
