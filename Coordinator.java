import java.util.ArrayList;
import java.util.List;

public class Coordinator {

    // === Collaborators managed internally ===
    private ListOfTestSuite listOfTestSuites;
    private ListOfProgram listOfPrograms;
    private ListofTestCase testcaseList;

    // === Constructor ===
    public Coordinator() {
        this.listOfTestSuites = new ListOfTestSuite(); 
        this.listOfPrograms = new ListOfProgram();
        this.testcaseList = new ListofTestCase();
    }

    // ============================================================
    //               CREATE A NEW TEST SUITE
    // ============================================================
    public void createTestSuite(String title) {
        TestSuite suite = new TestSuite(title);
        listOfTestSuites.setTestSuite(suite);
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

    // helper to get current suite
    public TestSuite getCurrentSuite() {
        return listOfTestSuites.getTestSuite();
    }
}
