public class TestCase {

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
}
