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
            // Debug log
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
    //               CREATE A NEW TEST CASE
    // ============================================================
    public void createTestCase(String title, String input, String expectedOutput) {
        TestCase tc = new TestCase(title, input, expectedOutput);
        testcaseList.addTestCase(tc);
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
    //                SAVE A TEST CASE TO FILE
    // ============================================================
    /**
     * Save the test case with the given title to a file.
     *
     * @param testCaseTitle title of the test case to save
     * @param filename      file name/path to save to
     * @return true if saved successfully, false if test case not found or error occurred
     */
    public boolean saveTestCase(String testCaseTitle, String filename) {
        TestCase tc = testcaseList.searchByTitle(testCaseTitle);
        if (tc == null) {
            System.out.println("[Coordinator] saveTestCase: no test case with title \"" + testCaseTitle + "\"");
            return false;
        }

        try {
            tc.saveToFile(filename);
            System.out.println("[Coordinator] saveTestCase: \"" + testCaseTitle + "\" saved to " + filename);
            return true;
        } catch (IOException e) {
            System.out.println("[Coordinator] saveTestCase ERROR: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    //                  EXECUTE TEST SUITE
    //  Matches sequence diagram signature: executeTestSuite(suiteName, path)
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
