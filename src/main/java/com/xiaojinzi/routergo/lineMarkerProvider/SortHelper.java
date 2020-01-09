package com.xiaojinzi.routergo.lineMarkerProvider;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtClass;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortHelper<T> {

    private final List<? extends T> targetList;
    private final Function<T, PsiElement> psiElementFunction;

    public SortHelper(List<? extends T> targetList, Function<T, PsiElement> psiElementFunction) {
        this.targetList = targetList;
        this.psiElementFunction = psiElementFunction;
    }

    public List<T> getSortedList() {
        return targetList.stream()
                .map(item -> new Entity(item))
                .sorted((t1, t2) -> t1.weight - t2.weight)
                .map(item -> item.target)
                .collect(Collectors.toList());
    }

    public class Entity {

        private T target;

        /**
         * 没有名称的排在最前面.
         * 发现一个可以匹配的特性就 + 1, 数字越小的越靠前
         */
        private int weight;

        public Entity(T target) {
            this.target = target;

            // 拿到元素
            PsiElement psiElement = psiElementFunction.apply(target);
            PsiElement temp = psiElement;
            PsiClass psiClass = null;
            KtClass ktClass = null;
            String javaClassName = null;
            String ktClassName = null;

            while (temp != null) {
                if (temp instanceof PsiClass) {
                    psiClass = (PsiClass) temp;
                    javaClassName = psiClass.getQualifiedName();
                    break;
                }
                temp = temp.getParent();
            }

            temp = psiElement;
            while (temp != null) {
                if (temp instanceof KtClass) {
                    ktClass = (KtClass) temp;
                    ktClassName = ktClass.getName();
                    break;
                }
                temp = temp.getParent();
            }

            // 如果没有名称的最前面
            if (ktClass != null) {
                if (ktClassName == null) {
                    weight = 1;
                }else {
                    weight = 2;
                }
            }else {
                if (psiClass != null) {
                    if (javaClassName == null) {
                        weight = 3;
                    }else {
                        // 是否是自动生成的代码
                        boolean b = javaClassName.contains("RouterApiGenerated");
                        if (b) {
                            weight = 5;
                        }else {
                            weight = 4;
                        }
                    }
                }
            }

        }

    }

}
