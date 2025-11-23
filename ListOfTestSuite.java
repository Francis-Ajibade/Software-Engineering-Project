public class ListOfTestSuite {

    private TestSuite suite;

    public ListOfTestSuite() {
        this.suite = null;
    }

    public void setTestSuite(TestSuite suite) {
        this.suite = suite;
    }

    public TestSuite getTestSuite() {
        return this.suite;
    }

    public boolean hasSuite() {
        return this.suite != null;
    }
}
