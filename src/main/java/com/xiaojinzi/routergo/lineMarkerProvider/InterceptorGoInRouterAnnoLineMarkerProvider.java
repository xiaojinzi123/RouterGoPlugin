package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiNameValuePair;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.util.Util;
import org.jetbrains.annotations.NotNull;

/**
 * 拦截器的到达目标的实现,这是 RouterAnno 注解中的 interceptorNames 的属性的使用跳转
 * <p>
 * 这里查找到一个满足跳转的拦截器使用的方式是 <img src="https://xiaojinzi.oss-cn-shanghai.aliyuncs.com/blogImages/20190623180201.png" />
 */
public class InterceptorGoInRouterAnnoLineMarkerProvider extends BaseInterceptorGoLineMarkerProvider {

    @Override
    public boolean isFit(@NotNull PsiElement element) {
        boolean b1 = element.getParent() instanceof PsiNameValuePair &&
                element.getParent().getFirstChild() instanceof PsiIdentifier &&
                Constants.RouterAnnoInterceptorName.equals(element.getParent().getFirstChild().getText());
        boolean b2 = element.getParent() != null &&
                element.getParent().getParent() instanceof PsiNameValuePair &&
                element.getParent().getParent().getFirstChild() instanceof PsiIdentifier &&
                Constants.RouterAnnoInterceptorName.equals(element.getParent().getParent().getFirstChild().getText());
        return (b1 || b2) && Util.isStringExpression(element);
    }

}
