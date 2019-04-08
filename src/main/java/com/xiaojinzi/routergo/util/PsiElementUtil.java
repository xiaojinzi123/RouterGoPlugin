package com.xiaojinzi.routergo.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Psi 元素工具类
 */
public class PsiElementUtil {

    public static boolean isLeftParentheses(@Nullable PsiElement psiElement) {
        return psiElement instanceof PsiJavaToken && "(".equals(psiElement.getText());
    }

    public static boolean isRightParentheses(@Nullable PsiElement psiElement) {
        return psiElement instanceof PsiJavaToken && ")".equals(psiElement.getText());
    }

    public static void recursive(@NotNull PsiElement psiElement, @NotNull PsiElementVisitor psiElementVisitor) {
        if (psiElement.getChildren().length == 0) {
            return;
        }
        psiElement.acceptChildren(new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                psiElementVisitor.visitElement(element);
                recursive(element, psiElementVisitor);
            }
        });
    }

    public static void recursive(@NotNull PsiElement psiElement, @NotNull PsiElementVisitor psiElementVisitor,
                                 @Nullable Filter filter) {
        if (psiElement.getChildren().length == 0) {
            return;
        }
        psiElement.acceptChildren(new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                psiElementVisitor.visitElement(element);
                // 是否过滤
                boolean isFilter = filter != null && filter.filter(element);
                if (!isFilter) {
                    recursive(element, psiElementVisitor);
                }
            }
        });
    }

    public interface Filter {

        /**
         *
         * @return
         */
        boolean filter(@NotNull PsiElement psiElement);

    }

}
