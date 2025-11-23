public class ListOfTestSuite{

    private TestSuite theOnlySuite;

    public ListOfTestSuite() {
        this.theOnlySuite = null;
    }

    public void setTestSuite(TestSuite suite) {
        this.theOnlySuite = suite;
    }

    public TestSuite getTestSuite() {
        return this.theOnlySuite;
    }
}
