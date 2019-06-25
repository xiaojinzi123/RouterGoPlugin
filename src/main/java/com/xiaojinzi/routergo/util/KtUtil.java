package com.xiaojinzi.routergo.util;

import com.intellij.psi.*;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.bean.InterceptorAnnoInfo;
import com.xiaojinzi.routergo.bean.RouterInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 有关 Kotlin 的工具类
 */
public class KtUtil {

    /**
     * 尝试获取一个元素的 String 文本
     *
     * @param psiElement
     * @return
     */
    @Nullable
    public static String getStringValue(@NotNull PsiElement psiElement) {
        if (psiElement instanceof KtDotQualifiedExpression) {
            try {
                KtDotQualifiedExpression ktDotQualifiedExpression = (KtDotQualifiedExpression) psiElement;
                PsiReference[] psiReferences = ktDotQualifiedExpression.getSelectorExpression().getReferences();
                PsiReference targetPsiReference = null;
                for (PsiReference psiReference : psiReferences) {
                    if (psiReference instanceof KtSimpleNameReference) {
                        targetPsiReference = psiReference;
                        break;
                    }
                }
                if (targetPsiReference != null) {
                    PsiField psiField = (PsiField) targetPsiReference.resolve();
                    PsiExpression psiFieldInitializer = psiField.getInitializer();
                    if (psiFieldInitializer instanceof PsiLiteralExpression) {
                        return (String) ((PsiLiteralExpression) psiFieldInitializer).getValue();
                    }
                }
            } catch (Exception Ignore) {
                // ignore
            }
        } else if (psiElement instanceof KtStringTemplateExpression) {
            try {
                KtStringTemplateExpression ktStringTemplateExpression = (KtStringTemplateExpression) psiElement;
                return ktStringTemplateExpression.getEntries()[0].getText();
            } catch (Exception Ignore) {
                // ignore
            }
        }
        return null;
    }

    public static RouterInfo getRouterInfo(@NotNull KtAnnotationEntry ktAnnotationEntry) {

        RouterInfo routerInfo = new RouterInfo();

        List<? extends ValueArgument> valueArguments = ktAnnotationEntry.getValueArguments();
        for (ValueArgument valueArgument : valueArguments) {
            KtExpression argumentExpression = valueArgument.getArgumentExpression();
            // 注解的属性的名称
            String annoAttribute = valueArgument.getArgumentName().getAsName().asString();
            if (Constants.RouterAnnoHostName.equals(annoAttribute)) { // 如果是 host
                routerInfo.host = KtUtil.getStringValue(argumentExpression);
            } else if (Constants.RouterAnnoPathName.equals(annoAttribute)) { // 如果是 path
                routerInfo.path = KtUtil.getStringValue(argumentExpression);
            } else if (Constants.RouterAnnoHostAndPathName.equals(annoAttribute)) { // 如果是 hostAndPath
                routerInfo.setHostAndPath(KtUtil.getStringValue(argumentExpression));
            }
        }

        // 可能是默认值
        if (routerInfo.host == null) {
            routerInfo.host = Util.getHostValueFromModule(ktAnnotationEntry);
        }

        if (routerInfo.isValid()) {
            return routerInfo;
        } else {
            return null;
        }

    }

    /**
     * 获取一个元素引用的目标方法对象
     *
     * @param psiElement
     * @return
     */
    public static PsiMethod getTargetRefrenceMethod(@NotNull PsiElement psiElement) {
        PsiReference[] references = psiElement.getReferences();
        for (PsiReference reference : references) {
            if (reference instanceof KtSimpleNameReference) {
                KtSimpleNameReference ktSimpleNameReference = (KtSimpleNameReference) reference;
                PsiElement targetPsiElement = ktSimpleNameReference.resolve();
                if (targetPsiElement instanceof PsiMethod) {
                    return (PsiMethod) targetPsiElement;
                }
            }
        }
        return null;
    }

    @Nullable
    public static RouterInfo getRouterInfoFromKtNameReferenceExpression(@NotNull KtSimpleNameExpression ktSimpleNameExpression) {
        RouterInfo routerInfo = getRouterInfoByHostAndPathFromKtNameReferenceExpression(ktSimpleNameExpression);
        if (routerInfo == null) {
            routerInfo = getRouterInfoByHostFromKtNameReferenceExpression(ktSimpleNameExpression);
        }
        return routerInfo;
    }

    @Nullable
    public static RouterInfo getRouterInfoByHostFromKtNameReferenceExpression(@NotNull KtSimpleNameExpression ktSimpleNameExpression) {
        RouterInfo routerInfo = new RouterInfo();
        try {
            KtValueArgumentList hostKtValueArgumentList = (KtValueArgumentList) ktSimpleNameExpression.getParent().getLastChild();
            KtExpression hostArgumentExpression = hostKtValueArgumentList.getArguments().get(0).getArgumentExpression();
            String host = KtUtil.getStringValue((hostArgumentExpression));
            routerInfo.host = host;

            KtValueArgumentList pathKtValueArgumentList = (KtValueArgumentList) ktSimpleNameExpression.getParent().getParent().getParent().getLastChild().getLastChild();
            KtExpression ktExpression = pathKtValueArgumentList.getArguments().get(0).getArgumentExpression();
            String path = KtUtil.getStringValue(ktExpression);
            routerInfo.path = path;
        } catch (Exception ignore) {
            // ignore
        }

        if (routerInfo.isValid()) {
            return routerInfo;
        } else {
            return null;
        }
    }

    public static RouterInfo getRouterInfoByHostAndPathFromKtNameReferenceExpression(@NotNull KtSimpleNameExpression ktSimpleNameExpression) {
        RouterInfo routerInfo = new RouterInfo();
        try {
            KtValueArgumentList hostAndPathKtValueArgumentList = (KtValueArgumentList) ktSimpleNameExpression.getParent().getLastChild();
            KtExpression hostAndPathArgumentExpression = hostAndPathKtValueArgumentList.getArguments().get(0).getArgumentExpression();
            String hostAndPath = KtUtil.getStringValue((hostAndPathArgumentExpression));
            routerInfo.setHostAndPath(hostAndPath);
        } catch (Exception ignore) {
            // ignore
        }

        if (routerInfo.isValid()) {
            return routerInfo;
        } else {
            return null;
        }
    }

    @NotNull
    public static List<String> getInterceptorNames(@NotNull KtSimpleNameReference ktSimpleNameReference) {
        try {
            List<String> result = new ArrayList<>();
            KtValueArgumentList ktValueArgumentList =
                    (KtValueArgumentList) ktSimpleNameReference.getElement().getParent().getLastChild();
            for (KtValueArgument ktValueArgument : ktValueArgumentList.getArguments()) {
                result.add(getStringValue(ktValueArgument.getArgumentExpression()));
            }
            return result;
        } catch (Exception ignore) {
            // ignore
        }
        return Collections.emptyList();
    }

    public static boolean isStringExpression(@NotNull PsiElement element) {
        String stringValue = getStringValue(element);
        if (stringValue == null || "".equals(stringValue)) {
            return false;
        } else {
            return true;
        }
    }

    public static InterceptorAnnoInfo getInterceptorInfoFromInterceptorAnno(@NotNull KtAnnotationEntry targetPsiAnnotation) {
        String intercepterName = null;
        List<? extends ValueArgument> valueArguments = targetPsiAnnotation.getValueArguments();
        for (ValueArgument valueArgument : valueArguments) {
            KtExpression argumentExpression = valueArgument.getArgumentExpression();
            // 注解的属性的名称
            String annoAttribute = null;
            // kotlin 的 value 不会获取的出来
            if (valueArgument.getArgumentName() == null) {
                annoAttribute = Constants.InterceptorAnnoValueName;
            }else {
                annoAttribute = valueArgument.getArgumentName().getAsName().asString();
            }
            if (Constants.InterceptorAnnoValueName.equals(annoAttribute)) { // 如果是 value
                intercepterName = KtUtil.getStringValue(argumentExpression);
            }
        }
        if (intercepterName == null) {
            return null;
        } else {
            return new InterceptorAnnoInfo(intercepterName);
        }
    }
}
