import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestSuiteResult implements Serializable {

    private String suiteName;
    private Map<String, StudentResult> studentResults;

    public TestSuiteResult(String suiteName) {
        this.suiteName = suiteName;
        this.studentResults = new HashMap<>();
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void addStudentResult(StudentResult sr) {
        studentResults.put(sr.getStudentName(), sr);
    }

    public StudentResult getStudentResult(String name) {
        return studentResults.get(name);
    }

    public Set<String> getAllStudentNames() {
        return studentResults.keySet();
    }

    // ⬇⬇⬇ REQUIRED BY THE UI (Load Results + Compare) ⬇⬇⬇
    public String formatAsText() {

        StringBuilder sb = new StringBuilder();

        sb.append("=== Test Suite Result ===\n");
        sb.append("Suite Name: ").append(suiteName).append("\n\n");

        if (studentResults.isEmpty()) {
            sb.append("(No student results stored)\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s %-12s\n", "Student", "Success Rate"));
        sb.append("-----------------------------------------\n");

        for (StudentResult sr : studentResults.values()) {

            String rate;

            if (sr.didCompileFail()) {
                rate = "NO_COMPILE";
            } else {
                rate = sr.computeSuccessRate(); // e.g., "3/5"
            }

            sb.append(String.format("%-20s %-12s\n",
                    sr.getStudentName(),
                    rate
            ));
        }

        return sb.toString();
    }
}
