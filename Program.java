import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Program {

    // === Attributes ===
    private String programName;      // folder name (student name, GitHub handle, etc.)
    private String folderPath;       // absolute path to student's folder
    private String javaFilePath;     // path to the .java file
    private String className;        // class name = file name without .java

    // === Constructor ===
    public Program(String programName, String folderPath, String javaFilePath) {
        this.programName = programName;
        this.folderPath = folderPath;
        this.javaFilePath = javaFilePath;

        File f = new File(javaFilePath);
        this.className = f.getName().replace(".java", "");
    }

    // === Getters ===
    public String getProgramName() { return programName; }
    public String getFolderPath() { return folderPath; }
    public String getJavaFilePath() { return javaFilePath; }
    public String getClassName() { return className; }


    // ============================================================
    //                  COMPILATION (using javac)
    // ============================================================
    public boolean compile() {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "javac",
                    javaFilePath
            );
            builder.directory(new File(folderPath));
            builder.redirectErrorStream(true);     // merge stderr + stdout

            Process process = builder.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[COMPILER] " + line);
            }

            int exitCode = process.waitFor();
            return exitCode == 0;   // success if exit code = 0

        } catch (Exception e) {
            return false;
        }
    }


    // ============================================================
    //               EXECUTION (using "java className")
    // ============================================================
    public String run(String inputData) {
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "java",
                    className
            );

            builder.directory(new File(folderPath));
            builder.redirectErrorStream(true);

            Process process = builder.start();

            // Send input to the program
            process.getOutputStream().write(inputData.getBytes());
            process.getOutputStream().flush();
            process.getOutputStream().close();

            // Read the program output
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();

        } catch (Exception e) {
            return "ERROR";
        }

        return output.toString().trim();  // return actual output
    }

}
