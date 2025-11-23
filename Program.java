import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Program {

    private String programName;
    private String folderPath;
    private String javaFilePath;
    private String className;

    public Program(String programName, String folderPath, String javaFilePath) {
        this.programName = programName;
        this.folderPath = folderPath;
        this.javaFilePath = javaFilePath;

        File f = new File(javaFilePath);
        this.className = f.getName().replace(".java", "");
    }

    public String getProgramName() { return programName; }

    public boolean compile() {
        try {
            ProcessBuilder builder = new ProcessBuilder("javac", javaFilePath);
            builder.directory(new File(folderPath));
            builder.redirectErrorStream(true);

            Process process = builder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            while (reader.readLine() != null) {}

            int exit = process.waitFor();
            return exit == 0;

        } catch (Exception e) {
            return false;
        }
    }

    public String run(String inputData) {
        StringBuilder out = new StringBuilder();

        try {
            ProcessBuilder builder = new ProcessBuilder("java", className);
            builder.directory(new File(folderPath));
            builder.redirectErrorStream(true);

            Process process = builder.start();

            // send input
            process.getOutputStream().write(inputData.getBytes());
            process.getOutputStream().flush();
            process.getOutputStream().close();

            // read output
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }

            process.waitFor();

        } catch (Exception e) {
            return "ERROR";
        }

        return out.toString().trim();
    }
}
