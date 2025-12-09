import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StudentResult implements Serializable 
{

    private String studentName;
    private boolean compileFailed = false;
    private Map<String, Boolean> outcomes = new HashMap<>();

    public StudentResult(String name) {
        this.studentName = name;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setCompileFailed() {
        compileFailed = true;
    }

    public boolean didCompileFail() {
        return compileFailed;
    }

    public void setOutcome(String testName, boolean passed) {
        outcomes.put(testName, passed);
    }

    public String computeSuccessRate() {
        if (compileFailed) return "NO_COMPILE";
        int passedCount = 0;
        for (Boolean b : outcomes.values())
            if (b) passedCount++;
        return passedCount + "/" + outcomes.size();
    }
}
