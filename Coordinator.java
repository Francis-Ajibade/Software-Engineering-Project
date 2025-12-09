/***********************************************
 * Coordinator.java
 * Central controller linking UI and backend.
 * Handles:
 * - Creating suites & cases
 * - Executing test suites (V1 + V2)
 * - Saving/Loading TestSuiteResult (V2)
 * - Comparing results (V2)
 ***********************************************/

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Coordinator {

    private ListOfTestSuite listOfTestSuites;
    private ListOfPrograms listOfPrograms;
    private ListofTestCase testcaseList;

    private ResultFileManager rfm = new ResultFileManager();

    public Coordinator() {
        this.listOfTestSuites = new ListOfTestSuite();
        this.listOfPrograms = new ListOfPrograms();
        this.testcaseList = new ListofTestCase();
    }

    // ------------ TEST SUITE --------------
    public void createTestSuite(String title) {
        listOfTestSuites.setTestSuite(new TestSuite(title));
    }

    public TestSuite getTestSuite(String title) {
        TestSuite suite = listOfTestSuites.getTestSuite();
        return (suite != null && suite.getTitle().equals(title)) ? suite : null;
    }

    public TestSuite getCurrentSuite() {
        return listOfTestSuites.getTestSuite();
    }

    // ------------ TEST CASE MGMT ----------
    public void createTestCase(String title, String input, String expectedOutput) {
        testcaseList.addTestCase(new TestCase(title, input, expectedOutput));
    }

    public TestCase loadTestCaseFromFile(File file) {
        try {
            TestCase tc = TestCase.parseFromFile(file);
            testcaseList.addTestCase(tc);
            return tc;
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    public void addTestCaseToSuite(String title) {
        TestCase tc = testcaseList.searchByTitle(title);
        TestSuite suite = listOfTestSuites.getTestSuite();
        if (tc != null && suite != null) suite.addTestCase(tc);
    }

    public boolean saveTestCaseToFile(String testCaseTitle, String fullPath) {
        TestCase tc = testcaseList.searchByTitle(testCaseTitle);
        if (tc == null) return false;
        try {
            tc.saveToFile(fullPath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ------------ VALIDATION --------------
    private void validateSuite(TestSuite suite) {
        if (suite.getTestCases() == null || suite.getTestCases().isEmpty())
            throw new IllegalArgumentException("Test Suite has no test cases.");

        for (TestCase tc : suite.getTestCases()) {
            if (tc.getTitle().trim().isEmpty())
                throw new IllegalArgumentException("Test case has empty title.");
            if (tc.getInput().trim().isEmpty())
                throw new IllegalArgumentException("Test case '" + tc.getTitle() + "' has empty INPUT.");
            if (tc.getExpectedOutput().trim().isEmpty())
                throw new IllegalArgumentException("Test case '" + tc.getTitle() + "' has empty OUTPUT.");
        }
    }

    // ------------ EXECUTE TEST SUITE V1 (UI VIEW RESULT) --------------
    public List<Result> executeTestSuite(String suiteName, String folderPath) {

        TestSuite suite = getTestSuite(suiteName);
        if (suite == null) throw new IllegalArgumentException("Test Suite not found.");

        validateSuite(suite);

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory())
            throw new IllegalArgumentException("Invalid submissions folder.");

        listOfPrograms.generatePrograms(folderPath);

        List<Result> results = new ArrayList<>();

        for (Program p : listOfPrograms.getPrograms()) {

            String compErr = p.compileAndReturnErrors();
            if (compErr != null) {
                results.add(new Result(p.getProgramName(), compErr));
                continue;
            }

            for (TestCase tc : suite.getTestCases()) {
                String actual = p.run(tc.getInput());
                boolean passed = tc.compareOutput(actual);
                results.add(new Result(
                        p.getProgramName(),
                        tc.getTitle(),
                        passed,
                        tc.getExpectedOutput(),
                        actual
                ));
            }
        }
        return results;
    }

    // ------------ EXECUTE TEST SUITE V2 (SAVEABLE FORM) --------------
    public TestSuiteResult executeTestSuiteV2(String suiteName, String folderPath) {

        TestSuite suite = getTestSuite(suiteName);
        if (suite == null) throw new IllegalArgumentException("Test Suite not found.");

        validateSuite(suite);

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory())
            throw new IllegalArgumentException("Invalid submissions folder.");

        listOfPrograms.generatePrograms(folderPath);

        TestSuiteResult tsr = new TestSuiteResult(suiteName);

        for (Program p : listOfPrograms.getPrograms()) {

            StudentResult sr = new StudentResult(p.getProgramName());

            String compErr = p.compileAndReturnErrors();
            if (compErr != null) {
                sr.setCompileFailed();
                tsr.addStudentResult(sr);
                continue;
            }

            for (TestCase tc : suite.getTestCases()) {
                String actual = p.run(tc.getInput());
                boolean passed = tc.compareOutput(actual);
                sr.setOutcome(tc.getTitle(), passed);
            }
            tsr.addStudentResult(sr);
        }

        return tsr;
    }

    // ------------ SAVE & LOAD RESULTS --------------
    public void saveTestSuiteResult(TestSuiteResult tsr, String path) {
        rfm.saveResult(tsr, path);
    }

    public TestSuiteResult loadTestSuiteResult(String path) {
        return rfm.loadResult(path);
    }

    // ------------ COMPARE TWO RESULT FILES --------------
    public List<ComparisonResult> compareResults(String file1, String file2) {

        TestSuiteResult r1 = rfm.loadResult(file1);
        TestSuiteResult r2 = rfm.loadResult(file2);

        if (r1 == null || r2 == null)
            return null;

        Set<String> students = new HashSet<>();
        students.addAll(r1.getAllStudentNames());
        students.addAll(r2.getAllStudentNames());

        List<ComparisonResult> results = new ArrayList<>();

        for (String name : students) {

            StudentResult s1 = r1.getStudentResult(name);
            StudentResult s2 = r2.getStudentResult(name);

            String rate1 = (s1 == null) ? "NO_INITIAL"
                    : s1.didCompileFail() ? "NO_COMPILE"
                    : s1.computeSuccessRate();

            String rate2 = (s2 == null) ? "NO_RESUB"
                    : s2.didCompileFail() ? "NO_COMPILE"
                    : s2.computeSuccessRate();

            results.add(new ComparisonResult(name, rate1, rate2));
        }

        return results;
    }
}
