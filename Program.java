import java.io.*;

public class Program {

    private String programName;      // student folder name
    private String folderPath;       // folder containing all .java files
    private String mainJavaFile;     // full path to the file containing main()
    private String mainClassName;    // class name extracted from mainJavaFile

    public Program(String programName, String folderPath, String mainJavaFile) {
        this.programName = programName;
        this.folderPath = folderPath;
        this.mainJavaFile = mainJavaFile;
        this.mainClassName = extractClassName(mainJavaFile);
    }

    public String getProgramName() {
        return programName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    /**
     * Extract class name from a file name.
     * Example: /Users/.../MainProgram.java → "MainProgram"
     */
    private String extractClassName(String filePath) {
        if (filePath == null) return null;

        File f = new File(filePath);
        String name = f.getName(); // e.g., MainProgram.java

        if (name.endsWith(".java"))
            return name.substring(0, name.length() - 5);

        return name;
    }


    // ================================================================
    //                       COMPILE STUDENT CODE
    // ================================================================
    public String compileAndReturnErrors() {

        try {
            // Compile ALL Java files inside the folder
            Process p = Runtime.getRuntime().exec(
                    "javac *.java",
                    null,
                    new File(folderPath)
            );

            p.waitFor();

            BufferedReader err =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String firstErrorLine = err.readLine();
            if (firstErrorLine != null) {
                return firstErrorLine;  // A compilation error occurred
            }

            return null; // No errors

        } catch (Exception e) {
            return "Unexpected compilation error.";
        }
    }


    // ================================================================
    //                        RUN STUDENT CODE
    // ================================================================
    public String run(String input) {
        try {

            // MUST USE THE MAIN CLASS NAME, NOT FOLDER NAME
            Process p = Runtime.getRuntime().exec(
                    "java " + mainClassName,
                    null,
                    new File(folderPath)
            );

            // Send input to the running program
            PrintWriter pw = new PrintWriter(p.getOutputStream());
            pw.println(input);
            pw.flush();

            // Read ONE line of output
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String out = in.readLine();

            p.waitFor();

            return (out == null ? "" : out.trim());

        } catch (Exception e) {
            return "";
        }
    }
}
