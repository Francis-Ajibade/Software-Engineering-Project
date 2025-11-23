public class Result {

    private String programName;   // Student folder name
    private String testCaseTitle; // Name of test case
    private boolean passed;       // Pass/fail result

    // === Constructor ===
    public Result(String programName, String testCaseTitle, boolean passed) {
        this.programName = programName;
        this.testCaseTitle = testCaseTitle;
        this.passed = passed;
    }

    // === Getters ===
    public String getProgramName() {
        return programName;
    }

    public String getTestCaseTitle() {
        return testCaseTitle;
    }

    public boolean isPassed() {
        return passed;
    }
}
