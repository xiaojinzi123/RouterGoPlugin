package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.psi.PsiElement;
import com.xiaojinzi.routergo.Constants;
import com.xiaojinzi.routergo.util.KtUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtValueArgument;
import org.jetbrains.kotlin.psi.KtValueArgumentName;

/**
 * 拦截器的到达目标的实现,这是 RouterAnno 注解中的 interceptorNames 的属性的使用跳转
 * <p>
 * 这里查找到一个满足跳转的拦截器使用的方式是 <img src="https://xiaojinzi.oss-cn-shanghai.aliyuncs.com/blogImages/20190623180201.png" />
 */
public class InterceptorGoInRouterAnnoLineMarkerProviderForKotlin extends BaseInterceptorGoLineMarkerProvider {

    @Override
    public boolean isFit(@NotNull PsiElement element) {
        boolean b = element.getParent() != null &&
                element.getParent().getParent() instanceof KtValueArgument &&
                element.getParent().getParent().getFirstChild() instanceof KtValueArgumentName &&
                Constants.RouterAnnoInterceptorName.equals(element.getParent().getParent().getFirstChild().getText());
        return b && KtUtil.isStringExpression(element);
    }

}
