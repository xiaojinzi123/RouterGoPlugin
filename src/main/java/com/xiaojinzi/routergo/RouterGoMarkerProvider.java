package com.xiaojinzi.routergo;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlFile;
import com.xiaojinzi.routergo.util.PsiElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
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
            //Messages.showMessageDialog("host = " + info.host + "\npath = " + info.path , "tip", null);
            GlobalSearchScope allScope = ProjectScope.getAllScope(elt.getProject());
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(elt.getProject());
            // 注解类@RouterAnno(.....)
            PsiClass serviceAnnotation = javaPsiFacade.findClass("com.xiaojinzi.component.anno.RouterAnno", allScope);
            if (serviceAnnotation == null) {
                return;
            }
            List<PsiAnnotation> psiAnnotationList = new ArrayList<>();

            Collection<PsiClass> routerActivities = AnnotatedElementsSearch
                    .searchPsiClasses(serviceAnnotation, allScope)
                    .findAll();
            Collection<PsiMethod> routerStaticMethods = AnnotatedElementsSearch
                    .searchPsiMethods(serviceAnnotation, allScope)
                    .findAll();
                /*for (PsiClass psiClassRouterActivity : routerActivities) {
                    psiClassRouterActivity.navigate(true);
                }*/
            for (PsiClass routerClass : routerActivities) {
                // 静态方法上的注解
                PsiAnnotation routerClassAnnotation = routerClass.getAnnotation(serviceAnnotation.getQualifiedName());
                if (routerClassAnnotation != null) {
                    psiAnnotationList.add(routerClassAnnotation);
                }
            }
            for (PsiMethod routerStaticMethod : routerStaticMethods) {
                // 静态方法上的注解
                PsiAnnotation routerStaticMethodAnnotation = routerStaticMethod.getAnnotation(serviceAnnotation.getQualifiedName());
                if (routerStaticMethodAnnotation != null) {
                    psiAnnotationList.add(routerStaticMethodAnnotation);
                }
            }

            PsiAnnotation targetAnno = null;

            for (int i = psiAnnotationList.size() - 1; i >= 0; i--) {
                PsiAnnotation psiAnnotation = psiAnnotationList.get(i);
                if (isMatchHostAndPath(info, psiAnnotation)) {
                    targetAnno = psiAnnotation;
                    break;
                }
            }

            if (targetAnno != null && targetAnno.canNavigate()) {
                targetAnno.navigate(true);
            }

        }
    }

    private boolean isMatchHostAndPath(@NotNull RouterInfo routerInfo, @NotNull PsiAnnotation psiAnnotation) {
        List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
        String host = null,path = null;
        for (JvmAnnotationAttribute attribute : attributes) {
            if ("host".equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                host = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
            }else if ("path".equals(attribute.getAttributeName()) && attribute.getAttributeValue() instanceof JvmAnnotationConstantValue) {
                path = (String) ((JvmAnnotationConstantValue) attribute.getAttributeValue()).getConstantValue();
            }
        }
        // 可能是默认值,在 build.gradle 文件中
        if (host == null) {
            // 找到对应的 module
            Module module = ModuleUtil.findModuleForPsiElement(psiAnnotation);
            VirtualFile buildGradleFile = LocalFileSystem.getInstance().findFileByIoFile(new File(module.getModuleFile().getParent().getPath() + "/build.gradle"));
            PsiFile buildGradlePsiFile = PsiManager.getInstance(module.getProject()).findFile(buildGradleFile);
            //PsiFile[] buildGradleFiles = FilenameIndex.getFilesByName(module.getProject(), "build.gradle", module.getModuleScope());
            // 这里读取GroovyFile文件中的 host
            // if(buildGradlePsiFile instanceof GroovyFile)
        }
        if (host == null || path == null) {
            return false;
        }
        return host.equals(routerInfo.host) && path.equals(routerInfo.path);
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
