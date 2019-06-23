package com.xiaojinzi.routergo.util;

import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.InterceptorAnnoInfo;
import com.xiaojinzi.routergo.bean.RouterInfo;
import org.jetbrains.android.dom.manifest.Application;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Util {

    /**
     * 获取调用了 Router...interceptorNames 方法的所有引用
     *
     * @param project
     * @return
     */
    @NotNull
    public static List<PsiReference> getAllInterceptorMethodReferences(@NotNull Project project) {
        List<PsiReference> referenceMethodList = new ArrayList<>();
        GlobalSearchScope allScope = ProjectScope.getAllScope(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);

        PsiClass routerBuilderClass = javaPsiFacade.findClass(Constants.RouterBuilderClassName, allScope);
        // 这个可能为空
        PsiClass rxRouterBuilderClass = javaPsiFacade.findClass(Constants.RxRouterBuilderClassName, allScope);

        // 搜索 使用中的 interceptorNames 的方法
        PsiMethod psiMethodRouter = (PsiMethod) routerBuilderClass.findMethodsByName(Constants.RouterInterceptorNameMethodName)[0];
        referenceMethodList.addAll(MethodReferencesSearch.search(psiMethodRouter).findAll());

        // 如果用户依赖了 rx 版本的库,那么才去寻找对应的方法
        if (rxRouterBuilderClass != null) {
            PsiMethod psiMethodRxRouter = (PsiMethod) rxRouterBuilderClass.findMethodsByName(Constants.RouterInterceptorNameMethodName)[0];
            referenceMethodList.addAll(MethodReferencesSearch.search(psiMethodRxRouter).findAll());
        }
        return referenceMethodList;
    }

    /**
     * 获取所有使用 RouterAnno 注解的注解集合
     *
     * @param project
     * @return
     */
    @NotNull
    public static List<PsiAnnotation> getAllRouterAnno(@NotNull Project project) {

        GlobalSearchScope allScope = ProjectScope.getAllScope(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);

        PsiClass routerAnnoClass = javaPsiFacade.findClass(Constants.RouterAnnoClassName, allScope);

        // 包括 java 和 kotlin 使用 RouterAnno
        List<PsiAnnotation> psiAnnotationList = new ArrayList<>();

        Collection<PsiClass> routerActivities = AnnotatedElementsSearch
                .searchPsiClasses(routerAnnoClass, allScope)
                .findAll();
        Collection<PsiMethod> routerStaticMethods = AnnotatedElementsSearch
                .searchPsiMethods(routerAnnoClass, allScope)
                .findAll();

        for (PsiClass routerClass : routerActivities) {
            // Activity上的注解
            PsiAnnotation routerClassAnnotation = routerClass.getAnnotation(routerAnnoClass.getQualifiedName());
            if (routerClassAnnotation != null) {
                psiAnnotationList.add(routerClassAnnotation);
            }
        }
        for (PsiMethod routerStaticMethod : routerStaticMethods) {
            // 静态方法上的注解
            PsiAnnotation routerStaticMethodAnnotation = routerStaticMethod.getAnnotation(routerAnnoClass.getQualifiedName());
            if (routerStaticMethodAnnotation != null) {
                psiAnnotationList.add(routerStaticMethodAnnotation);
            }
        }
        return psiAnnotationList;
    }

    /**
     * 从 RouterAnno 注解中获取 interceptorNames 属性的集合
     *
     * @param psiAnnotation
     * @return
     */
    @NotNull
    public static List<String> getInterceptorNamesFromRouterAnno(@NotNull PsiAnnotation psiAnnotation) {
        try {
            List<String> result = new ArrayList<>();
            JvmAnnotationArrayValue psiAnnotationArrayValue = (JvmAnnotationArrayValue) psiAnnotation.findAttribute(Constants.RouterAnnoInterceptorName).getAttributeValue();
            List<JvmAnnotationAttributeValue> values = psiAnnotationArrayValue.getValues();
            for (JvmAnnotationAttributeValue value : values) {
                if (value instanceof JvmAnnotationConstantValue) {
                    result.add((String) ((JvmAnnotationConstantValue) value).getConstantValue());
                }
            }
            return result;
        } catch (Exception ignore) {
            // ignore
        }
        return Collections.emptyList();
    }

    /**
     * 从注解 @InterceptorAnno("login") 中获取拦截器名称信息
     *
     * @param interceptorAnno
     * @return
     */
    @Nullable
    private InterceptorAnnoInfo getInterceptorInfoFromAnno(@NotNull PsiAnnotation interceptorAnno) {
        String interceptorName = null;
        try {
            JvmAnnotationAttributeValue hostAttributeValue = interceptorAnno.findAttribute(Constants.InterceptorAnnoValueName).getAttributeValue();
            if (hostAttributeValue instanceof JvmAnnotationConstantValue) {
                interceptorName = (String) ((JvmAnnotationConstantValue) hostAttributeValue).getConstantValue();
            }
        } catch (Exception ignore) {
            // ignore
        }
        if (interceptorName == null) {
            return null;
        }
        return new InterceptorAnnoInfo(interceptorName);
    }

    @Nullable
    public static RouterInfo getRouterInfoFromAnno(@NotNull PsiAnnotation routerAnno) {
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

    @Nullable
    public static String getHostFromRouterAnno(@NotNull PsiElement psiAnnotation) {
        String host = null;
        try {
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
        } catch (Exception ignore) {
            // ignore
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
    public static PsiMethod getRouterRequestHostAndPathMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterRequestBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostAndPathMethodName)[0];
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
    public static PsiMethod getRouterHostAndPathMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RouterBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostAndPathMethodName)[0];
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
    public static PsiMethod getRxRouterHostAndPathMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // 注解类@RouterAnno(.....)
            PsiClass routerRequestBuilderClass = javaPsiFacade.findClass(Constants.RxRouterBuilderClassName, allScope);
            return (PsiMethod) routerRequestBuilderClass.findMethodsByName(Constants.RouterHostAndPathMethodName)[0];
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

    /**
     * 获取 Router.with().host().path().interceptorNames("xxx","ccc"); 中的拦截器名称的列表
     *
     * @param psiReferenceExpression
     * @return
     */
    @Nullable
    public static List<String> getInterceptorNames(@NotNull PsiReferenceExpression psiReferenceExpression) {
        try {
            List<String> interceptorNames = new ArrayList<>();
            PsiExpressionList psiExpressionList = (PsiExpressionList) psiReferenceExpression.getParent().getChildren()[1];
            for (PsiExpression psiExpression : psiExpressionList.getExpressions()) {
                interceptorNames.add(getStringValue(psiExpression));
            }
            if (interceptorNames.size() == 0) {
                return null;
            } else {
                return interceptorNames;
            }
        } catch (Exception ignore) {
            // ignore
        }
        return null;
    }

    public static boolean isRouteAble(@NotNull PsiReferenceExpression psiReferenceExpression) {
        return getRouterInfoFromPsiReferenceExpression(psiReferenceExpression) == null ? false : true;
    }

    /**
     * .....host("order") 中拿到 "order" 字符串 也支持 hostAndPath 方法
     *
     * @param psiReferenceExpression
     * @return 返回一个 RouterInfo 对象表示获取到的 Host 和 Path
     */
    @Nullable
    public static RouterInfo getRouterInfoFromPsiReferenceExpression(@NotNull PsiReferenceExpression psiReferenceExpression) {
        RouterInfo info = new RouterInfo();
        // 尝试获取 host() 和 path() 方法写的参数
        try {
            PsiElement psiHostElement = psiReferenceExpression.getParent().getChildren()[1].getChildren()[1];
            PsiElement psiPathElement = psiReferenceExpression.getParent().getParent().getParent().getChildren()[1].getChildren()[1];
            info.host = getStringValue(psiHostElement);
            info.path = getStringValue(psiPathElement);
        } catch (Exception ignore) {
            // ignore
        }

        // 尝试获取 hostAndPath
        try {
            if (psiReferenceExpression.getLastChild() instanceof PsiIdentifier && Constants.RouterHostAndPathMethodName.equals(psiReferenceExpression.getLastChild().getText())) {
                PsiElement psiHostAndPathElement = psiReferenceExpression.getParent().getChildren()[1].getChildren()[1];
                info.setHostAndPath(getStringValue(psiHostAndPathElement));
            }
        } catch (Exception ignore) {
            // ignore
        }
        if (info.isValid()) {
            return info;
        } else {
            return null;
        }
    }

    /**
     * 尝试获取一个元素的 String 文本
     *
     * @param psiElement
     * @return
     */
    @Nullable
    public static String getStringValue(@NotNull PsiElement psiElement) {
        String value = null;
        if (psiElement instanceof PsiReferenceExpression) {
            try {
                value = (String) ((PsiLiteralExpression) ((PsiField) ((PsiReferenceExpression) psiElement).resolve()).getInitializer()).getValue();
                return value;
            } catch (Exception ignore) {
                // ignore
            }
        } else if (psiElement instanceof PsiLiteralExpression) {
            // 字符串表达式
            PsiLiteralExpression psiLiteralExpression = (PsiLiteralExpression) psiElement;
            value = (String) psiLiteralExpression.getValue();
            return value;
        } else if (psiElement instanceof PsiPolyadicExpression) {
            PsiExpression[] operands = ((PsiPolyadicExpression) psiElement).getOperands();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < operands.length; i++) {
                sb.append(getStringValue(operands[i]));
            }
            value = sb.toString();
        }
        return value;
    }

    public static boolean isStringExpression(@NotNull PsiElement psiElement) {
        String stringValue = getStringValue(psiElement);
        if (stringValue == null || "".equals(stringValue)) {
            return false;
        }else {
            return true;
        }
    }

}
