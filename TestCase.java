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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
}
