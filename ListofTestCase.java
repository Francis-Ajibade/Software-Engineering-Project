import java.util.ArrayList;
import java.util.List;

public class ListofTestCase {

    private List<TestCase> testCases;

    public ListofTestCase() {
        this.testCases = new ArrayList<>();
    }

    public void addTestCase(TestCase tc) {
        testCases.add(tc);
    }

    public void removeTestCase(TestCase tc) {
        testCases.remove(tc);
    }

    public List<TestCase> getAllTestCases() {
        return testCases;
    }

    public int size() {
        return testCases.size();
    }
}
