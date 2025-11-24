import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Coordinator {

    // === Collaborators managed internally ===
    private ListOfTestSuite listOfTestSuites;
    private ListOfPrograms listOfPrograms;
    private ListofTestCase testcaseList;

    // === Constructor ===
    public Coordinator() {
        this.listOfTestSuites = new ListOfTestSuite();
        this.listOfPrograms = new ListOfPrograms();
        this.testcaseList = new ListofTestCase();
    }

    // ============================================================
    //               CREATE A NEW TEST SUITE
    // ============================================================
    public void createTestSuite(String title) {
        TestSuite suite = new TestSuite(title);
        listOfTestSuites.setTestSuite(suite);
    }

    /**
     * Get the current TestSuite by title.
     * Because ListOfTestSuite only stores ONE suite, this just checks the title.
     */
    public TestSuite getTestSuite(String title) {
        TestSuite suite = listOfTestSuites.getTestSuite();
        if (suite != null && suite.getTitle().equals(title)) {
            System.out.println("[Coordinator] getTestSuite(\"" + title + "\") -> found");
            return suite;
        }
        System.out.println("[Coordinator] getTestSuite(\"" + title + "\") -> NOT found");
        return null;
    }

    // helper to get current suite regardless of title
    public TestSuite getCurrentSuite() {
        return listOfTestSuites.getTestSuite();
    }

    // ============================================================
    //               CREATE A NEW TEST CASE (manual)
    // ============================================================
    public void createTestCase(String title, String input, String expectedOutput) {
        TestCase tc = new TestCase(title, input, expectedOutput);
        testcaseList.addTestCase(tc);
    }

    // ============================================================
    //               LOAD TEST CASE FROM FILE (Case 2)
    // ============================================================
    /**
     * Parse and load a TestCase from a file in the required format.
     * Returns the created TestCase, or null if invalid / error.
     */
    public TestCase loadTestCaseFromFile(File file) {
        try {
            TestCase tc = TestCase.parseFromFile(file);
            testcaseList.addTestCase(tc);
            System.out.println("[Coordinator] Loaded TestCase from file: " + tc.getTitle());
            return tc;
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("[Coordinator] loadTestCaseFromFile ERROR: " + e.getMessage());
            return null;
        }
    }

    // ============================================================
    //        ADD A TEST CASE TO THE CURRENT TEST SUITE
    // ============================================================
    public void addTestCaseToSuite(String testCaseTitle) {

        TestSuite suite = listOfTestSuites.getTestSuite();
        if (suite == null) return;

        // find TestCase from global list
        TestCase tc = testcaseList.searchByTitle(testCaseTitle);

        if (tc != null) {
            suite.addTestCase(tc);   // TestSuite handles its ListOfTestCase
        }
    }

    // ============================================================
    //                SAVE A TEST CASE TO A FILE (Case 3)
    // ============================================================
    /**
     * Save the test case with the given title to a specific file path.
     *
     * @param testCaseTitle title of the test case to save
     * @param fullPath      full path (including filename) to save to
     * @return true if saved successfully, false if test case not found or error occurred
     */
    public boolean saveTestCaseToFile(String testCaseTitle, String fullPath) {
        TestCase tc = testcaseList.searchByTitle(testCaseTitle);
        if (tc == null) {
            System.out.println("[Coordinator] saveTestCaseToFile: no test case with title \"" + testCaseTitle + "\"");
            return false;
        }

        try {
            tc.saveToFile(fullPath);
            System.out.println("[Coordinator] saveTestCaseToFile: \"" + testCaseTitle + "\" saved to " + fullPath);
            return true;
        } catch (IOException e) {
            System.out.println("[Coordinator] saveTestCaseToFile ERROR: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    //  VALIDATE TEST CASES IN A SUITE (helper)
    // ============================================================
    /**
     * Validates that all test cases in the given suite have non-empty
     * title, input, and expected output.
     *
     * @throws IllegalArgumentException if something is missing.
     */
    private void validateTestCasesInSuite(TestSuite suite) {
        if (suite.getTestCases() == null || suite.getTestCases().isEmpty()) {
            throw new IllegalArgumentException("No test cases in test suite \"" + suite.getTitle() + "\".");
        }

        for (TestCase tc : suite.getTestCases()) {
            if (tc.getTitle() == null || tc.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Test case has empty TITLE.");
            }
            if (tc.getInput() == null || tc.getInput().trim().isEmpty()) {
                throw new IllegalArgumentException("Test case \"" + tc.getTitle() + "\" has empty INPUT.");
            }
            if (tc.getExpectedOutput() == null || tc.getExpectedOutput().trim().isEmpty()) {
                throw new IllegalArgumentException("Test case \"" + tc.getTitle() + "\" has empty OUTPUT.");
            }
        }
    }

    // ============================================================
    //                  EXECUTE TEST SUITE
    //  Matches signature: executeTestSuite(suiteName, path)
    // ============================================================
    public List<Result> executeTestSuite(String suiteName, String path) {

        List<Result> results = new ArrayList<>();

        // 1. Validate suite exists
        TestSuite suite = getTestSuite(suiteName);
        if (suite == null) {
            throw new IllegalArgumentException("No Test Suite found with name \"" + suiteName + "\".");
        }

        // 2. Validate submissions folder
        File submissionsDir = new File(path);
        if (!submissionsDir.exists() || !submissionsDir.isDirectory()) {
            throw new IllegalArgumentException("Submissions folder does not exist or is not a directory:\n" + path);
        }

        // 3. Validate test cases in suite
        validateTestCasesInSuite(suite);

        // 4. Generate Program objects
        listOfPrograms.generatePrograms(path);

        // 5. For each student program
        for (Program p : listOfPrograms.getPrograms()) {

            String compErr = p.compileAndReturnErrors();
            if (compErr != null) {
                // Record compilation error result with full error text
                results.add(new Result(p.getProgramName(), compErr));
                continue;
            }

            // 6. For each test case in the suite
            for (TestCase tc : suite.getTestCases()) {
                String actual = p.run(tc.getInput());
                boolean passed = tc.compareOutput(actual);

                results.add(new Result(
                        p.getProgramName(),           // student/folder name
                        tc.getTitle(),                // test case title
                        passed,
                        tc.getExpectedOutput(),
                        actual
                ));
            }
        }

        return results;
    }
}
