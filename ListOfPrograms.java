import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ListOfPrograms {

    private List<Program> programs;

    public ListOfPrograms() {
        this.programs = new ArrayList<>();
    }

    /**
     * Generate Program objects by scanning each student folder.
     * Now supports MULTIPLE .java files per student (Version 2 requirement).
     * 
     * For each folder:
     *  - Look at all .java files
     *  - Read the contents
     *  - Detect which file contains "public static void main("
     *  - Use that file as the MAIN file
     * 
     * Students MUST have exactly one main file in their submission.
     */
    public void generatePrograms(String rootFolderPath) {

        programs.clear();

        File root = new File(rootFolderPath);
        if (!root.exists() || !root.isDirectory()) return;

        File[] studentFolders = root.listFiles();
        if (studentFolders == null) return;

        for (File folder : studentFolders) {

            if (!folder.isDirectory()) continue;

            String studentName = folder.getName();
            File[] javaFiles = folder.listFiles((dir, name) -> name.endsWith(".java"));

            if (javaFiles == null || javaFiles.length == 0) {
                System.out.println("[ListOfPrograms] No Java files found for: " + studentName);
                continue;
            }

            String mainFilePath = null;

            // === SEARCH ALL FILES FOR THE MAIN METHOD ===
            for (File f : javaFiles) {
                try {
                    String content = Files.readString(f.toPath());
                    if (content.contains("public static void main(")) {
                        mainFilePath = f.getAbsolutePath();
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("[ListOfPrograms] Failed to read file: " + f.getName());
                }
            }

            if (mainFilePath == null) {
                System.out.println("[ListOfPrograms] NO MAIN METHOD FOUND for: " + studentName);
                // You may add an option to create a Program with compile error, but better to skip.
                continue;
            }

            // === MAIN FILE FOUND, CREATE PROGRAM OBJECT ===
            Program p = new Program(
                    studentName,
                    folder.getAbsolutePath(),
                    mainFilePath
            );

            programs.add(p);
        }
    }

    public List<Program> getPrograms() {
        return programs;
    }
}
