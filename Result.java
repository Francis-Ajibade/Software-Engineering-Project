public class Result{
    
    private String programName;       // Student / program name
    private String testCaseTitle;     // Test case title, or "Compilation"
    private boolean passed;           // Pass/fail

    private String expected;          // Expected output (for normal tests)
    private String actual;            // Actual output (for normal tests)
    private String compileError;      // Non-null only for compilation errors

    // === Constructor for normal test case result ===
    public Result(String programName,
                  String testCaseTitle,
                  boolean passed,
                  String expected,
                  String actual) {

        this.programName = programName;
        this.testCaseTitle = testCaseTitle;
        this.passed = passed;
        this.expected = expected;
        this.actual = actual;
        this.compileError = null;
    }

    // === Constructor for compilation error result ===
    public Result(String programName, String compileErrorMsg) {
        this.programName = programName;
        this.testCaseTitle = "Compilation";
        this.passed = false;
        this.expected = null;
        this.actual = null;
        this.compileError = compileErrorMsg;
    }

    // === Helper ===
    public boolean isCompilationError() {
        return compileError != null;
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

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }

    public String getCompileError() {
        return compileError;
    }
}
