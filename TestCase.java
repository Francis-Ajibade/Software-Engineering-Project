import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class TestCase{

    private String title;
    private String input;
    private String expectedOutput;

    public TestCase(String title, String input, String expectedOutput) {
        this.title = title;
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public String getTitle() {
        return title;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    // expected vs actual (case-insensitive, ignore extra spaces)
    public boolean compareOutput(String actual) {
        if (actual == null) return false;

        String cleanExpected = expectedOutput.trim().toLowerCase();
        String cleanActual = actual.trim().toLowerCase();

        return cleanExpected.equals(cleanActual);
    }

    /**
     * Saves this test case to a file in the required format:
     *
     * TITLE: <title>
     *
     * INPUT:
     * <input>
     *
     * OUTPUT:
     * <expected>
     */
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.println("TITLE: " + (title == null ? "" : title));
            out.println();
            out.println("INPUT:");
            out.println(input == null ? "" : input);
            out.println();
            out.println("OUTPUT:");
            out.println(expectedOutput == null ? "" : expectedOutput);
        }
    }

    /**
     * Parse a TestCase from a file in the format:
     *
     * TITLE: <title>
     *
     * INPUT:
     * <input>
     *
     * OUTPUT:
     * <single-line-output>
     *
     * Throws IllegalArgumentException if format invalid.
     */
    public static TestCase parseFromFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        // TITLE line
        String firstLine = lines.get(0).trim();
        if (!firstLine.startsWith("TITLE:")) {
            throw new IllegalArgumentException("Missing or invalid TITLE line");
        }
        String title = firstLine.substring("TITLE:".length()).trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("TITLE is empty");
        }

        // Find INPUT: and OUTPUT:
        int inputIndex = -1;
        int outputIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (trimmed.equals("INPUT:")) {
                inputIndex = i;
            } else if (trimmed.equals("OUTPUT:")) {
                outputIndex = i;
            }
        }

        if (inputIndex == -1 || outputIndex == -1 || outputIndex <= inputIndex) {
            throw new IllegalArgumentException("Missing INPUT or OUTPUT section");
        }

        // Input: first non-empty line after "INPUT:"
        String input = "";
        for (int i = inputIndex + 1; i < outputIndex; i++) {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                input = line;
                break;
            }
        }

        // Output: first non-empty line after "OUTPUT:"
        String expected = "";
        for (int i = outputIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                expected = line.trim();
                break;
            }
        }

        if (input.isEmpty()) {
            throw new IllegalArgumentException("INPUT section has no data");
        }
        if (expected.isEmpty()) {
            throw new IllegalArgumentException("OUTPUT section has no data");
        }

        return new TestCase(title, input, expected);
    }
}
