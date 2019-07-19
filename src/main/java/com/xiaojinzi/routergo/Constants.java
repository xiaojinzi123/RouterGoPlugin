package com.xiaojinzi.routergo;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * 常量类
 */
public class Constants {

    public static Icon ROUTER_LINK = IconLoader.getIcon("logo.png");

    public static Icon INTERCEPTOR_LINK = IconLoader.getIcon("interceptor_link.png");

    public static String InterceptorAnnoClassName = "com.xiaojinzi.component.anno.InterceptorAnno";
    public static String InterceptorAnnoClassShortName = "InterceptorAnno";
    public static String RouterAnnoClassName = "com.xiaojinzi.component.anno.RouterAnno";
    public static String RouterAnnoClassShortName = "RouterAnno";
    public static String RouterClassName = "com.xiaojinzi.component.impl.Router";
    public static String RouterRequestBuilderClassName = "com.xiaojinzi.component.impl.RouterRequest.Builder";
    public static String RouterBuilderClassName = "com.xiaojinzi.component.impl.Navigator";
    public static String RxRouterBuilderClassName = "com.xiaojinzi.component.impl.RxRouter.Builder";
    public static String RxRouterClassName = "com.xiaojinzi.component.impl.RxRouter";
    public static String RouterHostMethodName = "host";
    public static String RouterHostAndPathMethodName = "hostAndPath";
    public static String RouterInterceptorNameMethodName = "interceptorNames";
    public static String RouterAnnoInterceptorName = "interceptorNames";
    public static String InterceptorAnnoValueName = "value";
    public static String RouterAnnoHostName = "host";
    public static String RouterAnnoPathName = "path";
    public static String RouterAnnoHostAndPathName = "hostAndPath";

}
