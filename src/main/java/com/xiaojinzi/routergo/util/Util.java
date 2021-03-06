package com.xiaojinzi.routergo.util;

import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.InterceptorAnnoInfo;
import com.xiaojinzi.routergo.bean.RouterInfo;
import org.jetbrains.android.dom.manifest.Application;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * GlobalSearchScope allScope = ProjectScope.getAllScope(psiElement.getProject());
 * JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(psiElement.getProject());
 *
 * PsiClass routerClass = javaPsiFacade.findClass(Constants.RouterClassName, allScope);
 * PsiMethod psiWithMethodRouter = (PsiMethod) routerClass.findMethodsByName(Constants.RouterWithMethodName)[0];
 */
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
     */
    @NotNull
    public static List<String> getInterceptorNamesFromRouterAnno(@NotNull PsiAnnotation psiAnnotation) {
        try {
            List<String> result = new ArrayList<>();
            JvmAnnotationAttributeValue attributeValue = psiAnnotation.findAttribute(Constants.RouterAnnoInterceptorName).getAttributeValue();
            if (attributeValue instanceof JvmAnnotationConstantValue) {
                result.add((String)((JvmAnnotationConstantValue)attributeValue).getConstantValue());
            }else {
                JvmAnnotationArrayValue psiAnnotationArrayValue = (JvmAnnotationArrayValue) attributeValue;
                List<JvmAnnotationAttributeValue> values = psiAnnotationArrayValue.getValues();
                for (JvmAnnotationAttributeValue value : values) {
                    if (value instanceof JvmAnnotationConstantValue) {
                        result.add((String) ((JvmAnnotationConstantValue) value).getConstantValue());
                    }
                }
            }
            return result;
        } catch (Exception ignore) {
            // ignore
            // System.err.println("getInterceptorNamesFromRouterAnno 失败：" + ignore.getMessage());
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
    public static InterceptorAnnoInfo getInterceptorInfoFromInterceptorAnno(@NotNull PsiAnnotation interceptorAnno) {
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
            routerInfo.host = Util.getHostValueFromModule(routerAnno);
        }
        routerInfo.setHostAndPath(hostAndPath);
        if (routerInfo.host == null || routerInfo.path == null) {
            return null;
        }
        return routerInfo;
    }

    @Nullable
    public static String getHostValueFromModule(@NotNull PsiElement psiElement) {
        String host = null;
        try {
            // 找到对应的 module
            Module module = ModuleUtil.findModuleForPsiElement(psiElement);
            AndroidFacet androidFacet = AndroidFacet.getInstance(module);
            VirtualFile manifestFile = AndroidRootUtil.getPrimaryManifestFile(androidFacet);
            final Manifest manifest = AndroidUtils.loadDomElement(androidFacet.getModule(), manifestFile, Manifest.class);
            Application manifestApplication = manifest.getApplication();
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
            Object nameObj = metaData.getClass().getDeclaredMethod("getName").invoke(metaData);
            Object valueObj = metaData.getClass().getDeclaredMethod("getValue").invoke(metaData);
            String hostName = (String) nameObj.getClass().getDeclaredMethod("getStringValue").invoke(nameObj);
            String hostValue = (String) valueObj.getClass().getDeclaredMethod("getStringValue").invoke(valueObj);
            if (hostName != null && hostName.toLowerCase().startsWith("host_")) {
                if (hostValue == null) {
                    return null;
                }else {
                    return hostValue;
                }
            }
            return null;
        } catch (Exception e) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRouterWithFragmentMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            PsiClass routerClass = javaPsiFacade.findClass(Constants.RouterClassName, allScope);
            PsiClass fragmentNavigatorClass = javaPsiFacade.findClass(Constants.FragmentNavigatorClassName, allScope);
            PsiMethod[] psiMethods = (PsiMethod[]) routerClass.findMethodsByName(Constants.RouterWithMethodName);
            for (int i = 0; i < psiMethods.length; i++) {
                PsiMethod psiMethod = psiMethods[i];
                PsiType returnType = psiMethod.getReturnType();
                if (returnType instanceof PsiClassType) {
                    PsiClass targetReturnClass = ((PsiClassType) returnType).resolve();
                    if (fragmentNavigatorClass.equals(targetReturnClass)) {
                        return psiMethod;
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRxRouterWithFragmentMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            PsiClass rxRouterClass = javaPsiFacade.findClass(Constants.RxRouterClassName, allScope);
            PsiClass fragmentNavigatorClass = javaPsiFacade.findClass(Constants.FragmentRxNavigatorClassName, allScope);
            PsiMethod[] psiMethods = (PsiMethod[]) rxRouterClass.findMethodsByName(Constants.RxRouterWithMethodName);
            for (int i = 0; i < psiMethods.length; i++) {
                PsiMethod psiMethod = psiMethods[i];
                PsiType returnType = psiMethod.getReturnType();
                if (returnType instanceof PsiClassType) {
                    PsiClass targetReturnClass = ((PsiClassType) returnType).resolve();
                    if (fragmentNavigatorClass.equals(targetReturnClass)) {
                        return psiMethod;
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static PsiMethod getRouterRequestHostMethod(@NotNull Project project) {
        try {
            GlobalSearchScope allScope = ProjectScope.getAllScope(project);
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
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

    public static boolean isHostMethod(@NotNull Project project, @NotNull PsiMethod targetPsiMethod) {
        PsiMethod routerRequestHostMethod = Util.getRouterRequestHostMethod(project);
        PsiMethod routerHostMethod = Util.getRouterHostMethod(project);
        PsiMethod rxRouterHostMethod = Util.getRxRouterHostMethod(project);
        boolean isHostMethod = targetPsiMethod.equals(routerRequestHostMethod) ||
                targetPsiMethod.equals(routerHostMethod) ||
                targetPsiMethod.equals(rxRouterHostMethod);
        return isHostMethod;
    }

    public static boolean isHostAndPathMethod(@NotNull Project project, @NotNull PsiMethod targetPsiMethod) {
        PsiMethod routerRequestHostAndPathMethod = Util.getRouterRequestHostAndPathMethod(project);
        PsiMethod routerHostAndPathMethod = Util.getRouterHostAndPathMethod(project);
        PsiMethod rxRouterHostAndPathMethod = Util.getRxRouterHostAndPathMethod(project);
        boolean isHostAndPathMethod = targetPsiMethod.equals(routerRequestHostAndPathMethod) ||
                targetPsiMethod.equals(routerHostAndPathMethod) ||
                targetPsiMethod.equals(rxRouterHostAndPathMethod);
        return isHostAndPathMethod;
    }

    public static boolean isRouteAble(@NotNull PsiReferenceExpression psiReferenceExpression) {
        return getRouterInfoFromPsiReferenceExpression(psiReferenceExpression) == null ? false : true;
    }

    public static boolean isHostMethodReferenceExpression(@NotNull PsiReferenceExpression psiReferenceExpression) {
        Project project = psiReferenceExpression.getProject();
        PsiElement psiElement = psiReferenceExpression.resolve();
        if (psiElement instanceof PsiMethod) {
            PsiMethod targetPsiMethod = (PsiMethod) psiElement;
            PsiMethod routerRequestHostMethod = Util.getRouterRequestHostMethod(project);
            PsiMethod routerHostMethod = Util.getRouterHostMethod(project);
            PsiMethod rxRouterHostMethod = Util.getRxRouterHostMethod(project);
            boolean isHostMethod = targetPsiMethod.equals(routerRequestHostMethod) ||
                    targetPsiMethod.equals(routerHostMethod) ||
                    targetPsiMethod.equals(rxRouterHostMethod);
            return isHostMethod;
        }else {
            return false;
        }
    }

    /**
     * .....host("order") 中拿到 "order" 字符串 也支持 hostAndPath 方法
     *
     * @param psiReferenceExpression
     * @return 返回一个 RouterInfo 对象表示获取到的 Host 和 Path
     */
    @Nullable
    public static RouterInfo getRouterInfoFromPsiReferenceExpression(@NotNull PsiReferenceExpression psiReferenceExpression) {
        PsiElement psiElement = psiReferenceExpression.resolve();
        if ((psiElement instanceof PsiMethod) == false) {
            return null;
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;
        // 声明返回值
        RouterInfo info = new RouterInfo();
        boolean isHostMethod = Util.isHostMethod(psiElement.getProject(), psiMethod);
        if (isHostMethod) {
            // 尝试获取 host() 和 path() 方法写的参数
            try {
                PsiElement psiHostElement = psiReferenceExpression.getParent().getChildren()[1].getChildren()[1];
                PsiElement psiPathElement = psiReferenceExpression.getParent().getParent().getParent().getChildren()[1].getChildren()[1];
                info.host = getStringValue(psiHostElement);
                info.path = getStringValue(psiPathElement);
            } catch (Exception ignore) {
                // ignore
            }
            if (info.isValid()) {
                return info;
            } else {
                return null;
            }
        }

        boolean isHostAndPathMethod = Util.isHostAndPathMethod(psiElement.getProject(), psiMethod);
        if (isHostAndPathMethod) {
            // 尝试获取 hostAndPath
            try {
                if (psiReferenceExpression.getLastChild() instanceof PsiIdentifier &&
                        Constants.RouterHostAndPathMethodName.equals(psiReferenceExpression.getLastChild().getText())) {
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

        return null;

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
                // 引用的类型
                PsiElement targetPsiElement = ((PsiReferenceExpression) psiElement).resolve();
                if (targetPsiElement instanceof PsiLiteralExpression) {
                    value = (String) ((PsiLiteralExpression) ((PsiField) targetPsiElement).getInitializer()).getValue();
                } else if (targetPsiElement instanceof PsiField) { // 如果是一个字段, 那么看下字段的值是啥类型的
                    PsiElement valuePsiElement = targetPsiElement.getLastChild().getPrevSibling();
                    value = getStringValue(valuePsiElement);
                }
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
