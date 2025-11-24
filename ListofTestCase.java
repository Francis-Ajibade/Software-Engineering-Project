import java.util.ArrayList;
import java.util.List;

public class ListofTestCase{

    private List<TestCase> list = new ArrayList<>();

    public void addTestCase(TestCase tc) {
        list.add(tc);
    }

    public List<TestCase> getAll() {
        return list;
    }

    public TestCase searchByTitle(String title) {
        for (TestCase tc : list) {
            if (tc.getTitle().equals(title)) return tc;
        }
        return null;
    }
}
