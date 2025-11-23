import java.util.ArrayList;
import java.util.List;

public class TestSuite {

    private String title;
    private ListofTestCase listOfTestCase;

    public TestSuite(String title) {
        this.title = title;
        this.listOfTestCase = new ListofTestCase();
    }

    public void addTestCase(TestCase tc) {
        listOfTestCase.addTestCase(tc);
    }

    public List<TestCase> getTestCases() {
        return listOfTestCase.getAllTestCases();
    }

    public String getTitle() {
        return title;
    }
}
