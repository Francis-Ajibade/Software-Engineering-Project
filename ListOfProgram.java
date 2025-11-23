import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListOfPrograms {

    private List<Program> programs;

    public ListOfPrograms() {
        this.programs = new ArrayList<>();
    }

    // ============================================================
    //  Generate Program objects from each subfolder under root path
    // ============================================================
    public void generatePrograms(String rootFolderPath) {

        programs.clear(); // reset the list

        File root = new File(rootFolderPath);

        // Validate root folder
        if (!root.exists() || !root.isDirectory()) {
            return;
        }

        File[] subfolders = root.listFiles();
        if (subfolders == null) {
            return;
        }

        // For each student submission folder
        for (File sub : subfolders) {

            if (!sub.isDirectory()) {
                continue;
            }

            // folder name = programName (student name / GitHub ID)
            String programName = sub.getName();

            // expected rule: file name must be same as folder name
            String expectedJavaFile = programName + ".java";

            File javaFile = new File(sub, expectedJavaFile);

            // only accept the folder if it contains the expected java file
            if (javaFile.exists()) {

                Program p = new Program(
                        programName,
                        sub.getAbsolutePath(),
                        javaFile.getAbsolutePath()
                );

                programs.add(p);
            }
        }
    }

    // ============================================================
    //                   Getter for all Programs
    // ============================================================
    public List<Program> getPrograms() {
        return programs;
    }
}
