package com.xiaojinzi.routergo;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.util.PsiUtil;
import com.xiaojinzi.routergo.util.PsiElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * com.intellij.psi.search
 */
public class RouterGoMarkerProvider implements LineMarkerProvider {

    // 几种调用的方式

    public static final String CALL_STR1 = "Router\\.with[\\S\\s]*\\.host[\\S\\s]*.path[\\S\\s]*\\.navigate[\\S\\s]*";

    /**
     * 匹配 HOST
     */
    public static final String CALL_STR1_HOST = "Router\\.with[\\S\\s]*\\.host\\([^)]+\\)$";

    /**
     * 匹配 PATH
     */
    public static final String CALL_STR1_PATH = "Router\\.with[\\S\\s]*\\.path\\([^)]+\\)$";

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {

        if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;
            // 得到调用的语句
            String callStr = psiMethodCallExpression.getText();
            if (callStr.matches(CALL_STR1)) { // 如果是一个路由跳转的语句
                final RouterInfo info = getRouterInfo(psiMethodCallExpression);
                if (info != null) {
                    LineMarkerInfo<PsiElement> markerInfo = new LineMarkerInfo<PsiElement>(
                            psiMethodCallExpression,
                            psiMethodCallExpression.getTextRange(),
                            AllIcons.FileTypes.JavaClass, null, new NavigationImpl(info), GutterIconRenderer.Alignment.RIGHT
                    );
                    return markerInfo;
                }
            }
        }

        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        // AllIcons.FileTypes.JavaClass
        /*for (PsiElement element : elements) {
            final LineMarkerInfo info = doGetLineMarkerInfo(element);
            if (info != null) {
                result.add(info);
            }
        }*/
    }

    /**
     * 获取路由的信息
     * String value = (String) ((PsiLiteralExpression)((PsiField)((PsiReferenceExpression)element).resolve()).getInitializer()).getValue();
     *
     * @param psiMethodCallExpression 是一个完整的跳转语法 Router.with(this).host("xxx").path("xxx").navigate();
     * @return
     */
    private RouterInfo getRouterInfo(@NotNull PsiMethodCallExpression psiMethodCallExpression) {

        final RouterInfo info = new RouterInfo();

        // 递归所有的孩子
        PsiElementUtil.recursive(psiMethodCallExpression, new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                // 如果是一个执行语句
                if (element instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;
                    // 得到调用的语句
                    String callStr = psiMethodCallExpression.getText();
                    // 如果匹配了 Router.with(this)......host("xxx")
                    if (callStr.matches(CALL_STR1_HOST)) {
                        getHost(psiMethodCallExpression, info);
                    }
                    // 如果匹配了 Router.with(this)......path("xxx")
                    if (callStr.matches(CALL_STR1_PATH)) {
                        getPath(psiMethodCallExpression, info);
                    }
                    System.out.println("123123");
                }
            }
        }, new PsiElementUtil.Filter() {
            @Override
            public boolean filter(@NotNull PsiElement psiElement) {
                return psiElement instanceof PsiAnonymousClass || psiElement instanceof PsiComment;
            }
        });
        return info;
    }

    /**
     * 从 Router.with(xxx).....host("order") 中拿到 "order" 字符串
     *
     * @param psiMethodCallExpression
     * @param info
     */
    private void getHost(@NotNull PsiMethodCallExpression psiMethodCallExpression, @NotNull final RouterInfo info) {
        PsiElement psiElement = psiMethodCallExpression.getChildren()[1].getChildren()[1];
        if (psiElement instanceof PsiLiteralExpression) {
            // 字符串表达式
            PsiLiteralExpression psiLiteralExpression = (PsiLiteralExpression) psiElement;
            info.host = (String) psiLiteralExpression.getValue();
        }
    }

    /**
     * 从 Router.with(xxx).....path("order") 中拿到 "order" 字符串
     *
     * @param psiMethodCallExpression
     * @param info
     */
    private void getPath(@NotNull PsiMethodCallExpression psiMethodCallExpression, @NotNull final RouterInfo info) {
        PsiElement psiElement = psiMethodCallExpression.getChildren()[1].getChildren()[1];
        if (psiElement instanceof PsiLiteralExpression) {
            // 字符串表达式
            PsiLiteralExpression psiLiteralExpression = (PsiLiteralExpression) psiElement;
            info.path = (String) psiLiteralExpression.getValue();
        }
    }

    //
    @Nullable
    private LineMarkerInfo doGetLineMarkerInfo(PsiElement element) {
        if (element instanceof PsiIdentifier) {
            if ("host".equals(((PsiIdentifier) element).getText())) {
                System.out.println("123123");
            }
        }
        if (!(element instanceof PsiMethodCallExpression)) {
            return null;
        }
        final PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;
        // 得到调用的语句
        String callStr = psiMethodCallExpression.getText();
        if (!callStr.matches(CALL_STR1)) {
            return null;
        }
        LineMarkerInfo<PsiMethodCallExpression> markerInfo = new LineMarkerInfo<PsiMethodCallExpression>(
                psiMethodCallExpression,
                psiMethodCallExpression.getTextRange(),
                AllIcons.FileTypes.JavaClass, null, new NavigationImpl(null), GutterIconRenderer.Alignment.RIGHT
        );
        return markerInfo;
    }

    private class NavigationImpl implements GutterIconNavigationHandler {

        @NotNull
        private RouterInfo info;

        public NavigationImpl(@NotNull RouterInfo info) {
            this.info = info;
        }

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            Messages.showMessageDialog("host = " + info.host + "\npath = " + info.path , "tip", null);
            /*GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
            // 注解类@RouterAnno(.....)
            PsiClass serviceAnnotation = javaPsiFacade.findClass("com.xiaojinzi.component.anno.RouterAnno", allScope);
            if (serviceAnnotation != null) {
                Collection<PsiClass> routerActivities = AnnotatedElementsSearch
                        .searchPsiClasses(serviceAnnotation, allScope)
                        .findAll();
                for (PsiClass psiClassRouterActivity : routerActivities) {
                    psiClassRouterActivity.navigate(true);
                }
            }*/
        }
    }

    private class RouterInfo {

        /**
         * 这个路由关联的元素,
         * 一般是一个 PsiMethodCallExpression 或者 PsiReferenceExpression
         */
        private PsiElement psiElement;

        /**
         * 路由的 host
         */
        private String host;

        /**
         * 路由的 path
         */
        private String path;

        public PsiElement getPsiElement() {
            return psiElement;
        }

        public void setPsiElement(PsiElement psiElement) {
            this.psiElement = psiElement;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }


}
