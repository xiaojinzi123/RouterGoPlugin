import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl;
import com.intellij.psi.search.searches.AnnotationTargetsSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HelloAction extends AnAction {
    public HelloAction() {
        super("Hello");
    }

    public void actionPerformed(AnActionEvent event) {

        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);

        PsiJavaFile psiJavaFile = null;

        if (psiFile instanceof PsiJavaFile) {
            psiJavaFile = ((PsiJavaFile) psiFile);
        } else {
            return;
        }

        StringBuffer sb = new StringBuffer();

        psiJavaFile.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                super.visitAnnotation(annotation);
                String qualifiedName = annotation.getQualifiedName();
                sb.append(qualifiedName + "\n");
            }
        });

        Project project = event.getProject();
        Messages.showMessageDialog(project, sb.toString() + "\n" + psiFile.getViewProvider().getClass().getName(), "Greeting", Messages.getInformationIcon());

    }
}