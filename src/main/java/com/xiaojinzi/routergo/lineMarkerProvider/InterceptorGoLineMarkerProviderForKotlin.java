package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.xiaojinzi.routergo.util.KtUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;

import java.util.Collection;
import java.util.List;

/**
 * 拦截器的到达目标的实现
 * <p>
 * 这里查找到一个满足跳转的拦截器使用的方式是 <img src="https://xiaojinzi.oss-cn-shanghai.aliyuncs.com/blogImages/20190623171723.png" />
 *
 * @author xiaojinzi
 */
public class InterceptorGoLineMarkerProviderForKotlin extends BaseInterceptorGoLineMarkerProvider {

    @Override
    public boolean isFit(@NotNull PsiElement element) {
        // element 是 xxx 或者 fff 表达式,而这个判断是是否是 Router 或者 RxRouter 的 ..interceptorNames 方法
        if (element.getParent() != null &&
                element.getParent().getParent() != null &&
                element.getParent().getParent().getParent() != null &&
                isRouterInterceptorNamesMethod(element.getParent().getParent().getParent().getFirstChild())) {
            // 二次确认是一个字符串表达式
            String interceptorName = KtUtil.getStringValue(element);
            if (interceptorName != null && !"".equals(interceptorName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 如果是 ...interceptorNames(xxx,fff) 方法
     *
     * @param psiElement
     * @return
     */
    private boolean isRouterInterceptorNamesMethod(@Nullable PsiElement psiElement) {
        if (psiElement instanceof KtSimpleNameExpression) {
            KtSimpleNameExpression ktSimpleNameExpression = (KtSimpleNameExpression) psiElement;
            PsiElement targetPsiElement = KtUtil.getTargetRefrenceMethod(ktSimpleNameExpression);
            if (targetPsiElement instanceof PsiMethod) {
                PsiMethod targetPsiMethod = (PsiMethod) targetPsiElement;
                if (targetPsiMethod.equals(routerInterceptorNameMethod) ||
                        targetPsiMethod.equals(rxRouterInterceptorNameMethod)) {
                    return true;
                }
            }
        }
        return false;
    }

}
