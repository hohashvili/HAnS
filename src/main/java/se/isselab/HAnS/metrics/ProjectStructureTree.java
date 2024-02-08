package se.isselab.HAnS.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.impl.PsiManagerImpl;
import org.jetbrains.annotations.NotNull;
import se.isselab.HAnS.fileAnnotation.psi.FileAnnotationFile;
import se.isselab.HAnS.folderAnnotation.psi.FolderAnnotationFile;
import se.isselab.HAnS.folderAnnotation.psi.FolderAnnotationTokenType;

import java.io.File;
import java.util.*;

public class ProjectStructureTree {

    private String name;
    private String path;
    private Type type;
    private int depth;
    private HashSet<String> featureList;
    private List<ProjectStructureTree> children;

    private enum Type {FOLDER, FILE, LINE}

    public ProjectStructureTree() {}

    public ProjectStructureTree(String name, String path, Type type, int depth) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.depth = depth;
        this.featureList = new HashSet<>();
        this.children = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getDepth() {
        return depth;
    }

    public HashSet<String> getFeatureList() {
        return featureList;
    }

    public List<ProjectStructureTree> getChildren() {
        return children;
    }

    // Method to process the project structure
    public ProjectStructureTree processProjectStructure(Project project, String rootFolderPath) {

        File rootFolder = new File(rootFolderPath);
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            throw new IllegalArgumentException("Invalid root folder path");
        }

        ProjectStructureTree root = new ProjectStructureTree(
                rootFolder.getName(), rootFolderPath, ProjectStructureTree.Type.FOLDER, 0);

        this.processFolder(project, rootFolder, root);

        return root;
    }

    private void processFolder(Project project, File folder, ProjectStructureTree parent) {
        Queue<File> specialFilesQueue = new LinkedList<>(); // save .feature-to-file in the folder for later processing
        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                ProjectStructureTree folderNode = new ProjectStructureTree(
                        file.getName(), file.getAbsolutePath(), ProjectStructureTree.Type.FOLDER, parent.depth + 1);
                parent.children.add(folderNode);
                processFolder(project, file, folderNode);
            } else if (file.isFile()) {
                this.processFile(project, file, parent, specialFilesQueue);
            }
        }

        while (!specialFilesQueue.isEmpty()) {
            File specialFileNode = specialFilesQueue.poll();
            LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
            VirtualFile virtualFile = localFileSystem.findFileByIoFile(specialFileNode);
            if (virtualFile != null) {
                PsiFile foundFile = PsiManagerImpl.getInstance(project).findFile(virtualFile);
                if (foundFile != null) {
                    processFeatureToFile(foundFile, parent);
                }
            }

        }
    }

    private void processFile(Project project, File file, ProjectStructureTree parent, Queue<File> featureToFileQueue) {
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        VirtualFile virtualFile = localFileSystem.findFileByIoFile(file);
        if (virtualFile == null) { return; }

        PsiFile foundFile = PsiManagerImpl.getInstance(project).findFile(virtualFile);

        if (foundFile instanceof FileAnnotationFile) { // Logic for .feature-to-file
            featureToFileQueue.add(file);
        } else if (foundFile instanceof FolderAnnotationFile) { // Logic for .feature-to-folder
            foundFile.accept(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element.getNode().getElementType() instanceof FolderAnnotationTokenType) {
                        parent.featureList.add(element.getText());
                    }
                    super.visitElement(element);
                }
            });
        } else {
            ProjectStructureTree fileNode = new ProjectStructureTree(
                    file.getName(), file.getAbsolutePath(), Type.FILE, parent.depth);
            parent.children.add(fileNode);

            this.processCode(project, file, fileNode);
        }
    }

}
