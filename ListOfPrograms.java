import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListOfPrograms {

    private List<Program> programs;

    public ListOfPrograms() {
        this.programs = new ArrayList<>();
    }

    public void generatePrograms(String rootFolderPath) {

        programs.clear();

        File root = new File(rootFolderPath);
        if (!root.exists() || !root.isDirectory()) return;

        File[] subfolders = root.listFiles();
        if (subfolders == null) return;

        for (File sub : subfolders) {

            if (!sub.isDirectory()) continue;

            String programName = sub.getName();
            String expectedJavaFile = programName + ".java";

            File javaFile = new File(sub, expectedJavaFile);

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

    public List<Program> getPrograms() {
        return programs;
    }
}
