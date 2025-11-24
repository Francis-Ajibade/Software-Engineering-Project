import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Coordinator{

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

    // Get the suite by title (only one suite stored)
    public TestSuite getTestSuite(String title) {
        TestSuite suite = listOfTestSuites.getTestSuite();
        if (suite != null && suite.getTitle().equals(title)) {
            System.out.println("[Coordinator] getTestSuite(\"" + title + "\") -> found");
            return suite;
        }
        System.out.println("[Coordinator] getTestSuite(\"" + title + "\") -> NOT found");
        return null;
    }

    // helper to get current suite
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
    //                  EXECUTE TEST SUITE
    // ============================================================
    public List<Result> executeTestSuite(String suiteName, String path) {

        List<Result> results = new ArrayList<>();

        TestSuite suite = listOfTestSuites.getTestSuite();
        if (suite == null) return results;

        // 1. generate program objects
        listOfPrograms.generatePrograms(path);

        // 2. For each student program
        for (Program p : listOfPrograms.getPrograms()) {

            boolean compiled = p.compile();
            if (!compiled) {
                results.add(new Result(p.getProgramName(), "Compilation", false));
                continue;
            }

            // 3. For each test case assigned to this suite
            for (TestCase tc : suite.getTestCases()) {

                String actual = p.run(tc.getInput());
                boolean passed = tc.compareOutput(actual);

                results.add(new Result(
                        p.getProgramName(),
                        tc.getTitle(),
                        passed
                ));
            }
        }

        return results;
    }
}
