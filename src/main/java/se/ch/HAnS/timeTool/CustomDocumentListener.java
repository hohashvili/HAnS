package se.ch.HAnS.timeTool;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomDocumentListener implements PsiTreeChangeListener {
    final int LOG_INTERVAL = 0; // Milliseconds between being able to log
    private final Project project;
    private final LogWriter logWriter;
    private final CustomTimer timer;
    private final SessionTracker sessionTracker;

    private final AnnotationEventHandler annotationEventHandler;

    public CustomDocumentListener(Project project) {
        this.project = project;
        logWriter = new LogWriter(project, System.getProperty("user.home") + "/Desktop", "log.txt");
        timer = new CustomTimer();
        sessionTracker = new SessionTracker();
        this.annotationEventHandler = new AnnotationEventHandler(project, logWriter, timer);

        // Schedule a task to check for annotation time periodically
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this::checkAnnotationTime, 0, 1, TimeUnit.SECONDS);

        // A VirtualFileListener to track the creation and deletion of files
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                onFileCreated(event);
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                onFileDeleted(event);
            }
        }, new EditorTracker(project));

        // A DocumentListener to detect changes in documents
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {

            // This method is called before a change is made to the document
            @Override
            public void beforeDocumentChange(@NotNull DocumentEvent event) {
                // Get the text that was deleted in the document change event
                Document document = event.getDocument();
                String deletedText = document.getText().substring(event.getOffset(), event.getOffset() + event.getOldLength());

                // Check if the deleted text matches the annotation pattern, including possible leading whitespaces
                Pattern pattern = Pattern.compile("^\\s*//\\s*&(.+?)\\[");
                Matcher matcher = pattern.matcher(deletedText);
                if (matcher.find()) {
                    String annotationName = matcher.group(1).trim();
                    // Get the PsiFile associated with the document and retrieve its fileName
                    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                    if (psiFile != null) {
                        String fileName = psiFile.getName();
                        logWriter.writeToJson(fileName, annotationName, "Deleted annotation: " + deletedText, timer.getCurrentDate());
                    }
                }
            }

            // This method is called after the document has been changed
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
            }
        }, new EditorTracker(project));
    }

    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {
        if (!timer.canLog(LOG_INTERVAL)) {
            return;
        }    // If not enough time has passed to log, returns early

        processFileChange(Objects.requireNonNull(event.getFile()));
        PsiElement psiElement = event.getChild();
        String fileName = psiElement.getContainingFile().getName();

        if (psiElement instanceof PsiComment) {
            PsiComment comment = (PsiComment) psiElement;
            if (isAnnotationComment(comment)) {
                handleAnnotationCommentEvent(comment, "added", fileName);
            }
        }
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        if (!timer.canLog(LOG_INTERVAL)) {
            return;
        }    // If not enough time has passed to log, returns early

        processFileChange(Objects.requireNonNull(event.getFile()));
        PsiElement psiElement = event.getChild();
        String fileName = psiElement.getContainingFile().getName();

        if (psiElement instanceof PsiComment) {
            PsiComment comment = (PsiComment) psiElement;
            if (isAnnotationComment(comment)) {
                handleAnnotationCommentEvent(comment, "removed", fileName);
            }
        }
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        if (!timer.canLog(LOG_INTERVAL)) {
            return;
        }    // If not enough time has passed to log, returns early

        processFileChange(Objects.requireNonNull(event.getFile()));
        PsiElement oldChild = event.getOldChild();
        PsiElement newChild = event.getNewChild();
        String fileName = event.getFile().getName();

        if (oldChild instanceof PsiComment && newChild instanceof PsiComment) {
            PsiComment oldComment = (PsiComment) oldChild;
            PsiComment newComment = (PsiComment) newChild;
            if (isAnnotationComment(oldComment) || isAnnotationComment(newComment)) {
                handleAnnotationCommentEvent(newComment, "replaced", fileName);
            }
        }
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
    }

    // This method checks if the given PsiComment object is an annotation comment (so if it starts with "// &")
    private boolean isAnnotationComment(PsiComment comment) {
        String text = comment.getText();
        return text.startsWith("// &");
    }

    // This method checks if the file created ends with ".feature-to-file" or ".feature-to-folder"
    private void onFileCreated(@NotNull VirtualFileEvent event) {
        String fileName = event.getFileName();

        if (fileName.endsWith(".feature-model") || fileName.endsWith(".feature-to-file") || fileName.endsWith(".feature-to-folder")) {
            logWriter.writeToJson(fileName, "annotation", fileName + " created", timer.getCurrentDate());
            logWriter.writeToLog(fileName + " was created at " + timer.getCurrentDate() + "\n");
        }
    }

    // This method checks if the file deleted ends with ".feature-to-file" or ".feature-to-folder"
    private void onFileDeleted(@NotNull VirtualFileEvent event) {
        String fileName = event.getFileName();

        if (fileName.endsWith(".feature-model") || fileName.endsWith(".feature-to-file") || fileName.endsWith(".feature-to-folder")) {
            logWriter.writeToJson(fileName, "annotation", fileName + " deleted", timer.getCurrentDate());
            logWriter.writeToLog(fileName + " was deleted at " + timer.getCurrentDate() + "\n");
        }
    }

    // This method handles annotation comment events such as adding, removing, or replacing of an annotation comment
    private void handleAnnotationCommentEvent(PsiComment comment, String eventType, String fileName) {
        annotationEventHandler.handleAnnotationCommentEvent(comment, eventType, fileName);
    }

    /*
     * This method checks so that if there has been no annotation activity for more than 10 seconds, it logs the
     * total time spent during that annotation session, and resets the time variables and updates the log files
     */
    private void checkAnnotationTime() {
        annotationEventHandler.checkAnnotationTime();
    }

    // This method processes the file change events and logs specific changes based on the file type
    private void processFileChange(PsiFile psiFile) {
        annotationEventHandler.processFileChange(psiFile);
    }

    // The EditorTracker class is used to track editor events and clean up resources when the project is disposed
    static class EditorTracker implements Disposable {
        private final Project project;

        EditorTracker(Project project) {
            this.project = project;
        }

        @Override
        public void dispose() {
            // Cleanup or unregister resources when the project is disposed
        }
    }

    public void logTotalTime() {
        logWriter.writeTotalTimeToJson("&line", annotationEventHandler.getLineAnnotationTotalTime() + " ms");
        logWriter.writeTotalTimeToJson("&block", annotationEventHandler.getBlockAnnotationTotalTime() + " ms");
        logWriter.writeTotalTimeToJson(".feature-to-file", annotationEventHandler.getFeatureToFileTotalTime() + " ms");
        logWriter.writeTotalTimeToJson(".feature-to-folder", annotationEventHandler.getFeatureToFolderTotalTime() + " ms");
        logWriter.writeTotalTimeToJson(".feature-model", annotationEventHandler.getFeatureModelTotalTime() + " ms");
        logWriter.writeTotalTimeToJson("Developing time", sessionTracker.getTotalActiveTime() + " ms");
    }
}