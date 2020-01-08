package com.xiaojinzi.routergo;

import com.android.tools.idea.ui.resourcechooser.icons.IconFactory;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.ui.IconManager;
import com.intellij.util.Function;
import com.intellij.util.ui.ColorIcon;

import javax.swing.*;

/**
 * 常量类
 */
public class Constants {

    public static final Function<PsiElement, String> TOOLTIP_PROVIDER_FUNCTION_FOR_ROUTER_USAGE = psiElement -> "查找跳转到此处的跳转";
    public static final Function<PsiElement, String> TOOLTIP_PROVIDER_FUNCTION_FOR_ROUTER = psiElement -> "点击跳转到目标 Activity 或者 自定义 Intent 处";
    public static final Function<PsiElement, String> TOOLTIP_PROVIDER_FUNCTION_FOR_FRAGMENT = psiElement -> "点击跳转到目标 Fragment";

    // public static final Icon ROUTER_LINK = IconLoader.getIcon("logo.png");
    public static final Icon ROUTER = AllIcons.Actions.Find;
    public static final Icon ROUTER_USAGE_FIND = AllIcons.Actions.Find;

    // public static final Icon INTERCEPTOR_LINK = IconLoader.getIcon("interceptor_link.png");
    public static final Icon INTERCEPTOR = AllIcons.FileTypes.JavaClass;
    public static final Icon INTERCEPTOR_USAGE_FIND = AllIcons.Actions.Find;

    // public static final Icon FRAGMENT_LINK = IconLoader.getIcon("fragment.png");
    public static final Icon FRAGMENT_LINK = AllIcons.FileTypes.JavaClass;

    public static String InterceptorAnnoClassName = "com.xiaojinzi.component.anno.InterceptorAnno";
    public static String InterceptorAnnoClassShortName = "InterceptorAnno";
    public static String RouterAnnoClassName = "com.xiaojinzi.component.anno.RouterAnno";
    public static String FragmentAnnoClassName = "com.xiaojinzi.component.anno.FragmentAnno";
    public static String RouterAnnoClassShortName = "RouterAnno";
    public static String RouterClassName = "com.xiaojinzi.component.impl.Router";
    public static String RxRouterClassName = "com.xiaojinzi.component.impl.RxRouter";
    public static String RouterWithMethodName = "with";
    public static String RxRouterWithMethodName = "with";
    public static String RouterRequestBuilderClassName = "com.xiaojinzi.component.impl.RouterRequest.Builder";
    public static String RouterBuilderClassName = "com.xiaojinzi.component.impl.Navigator";
    public static String RxRouterBuilderClassName = "com.xiaojinzi.component.impl.RxRouter.RxNavigator";
    public static String FragmentNavigatorClassName = "com.xiaojinzi.component.impl.FragmentNavigator";
    public static String FragmentRxNavigatorClassName = "com.xiaojinzi.component.impl.RxFragmentNavigator";
    public static String RouterHostMethodName = "host";
    public static String RouterHostAndPathMethodName = "hostAndPath";
    public static String RouterInterceptorNameMethodName = "interceptorNames";
    public static String RouterAnnoInterceptorName = "interceptorNames";
    public static String InterceptorAnnoValueName = "value";
    public static String FragmentAnnoValueName = "value";
    public static String RouterAnnoHostName = "host";
    public static String RouterAnnoPathName = "path";
    public static String RouterAnnoHostAndPathName = "hostAndPath";

}
