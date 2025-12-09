public class ComparisonResult {

    private String studentName;
    private String submission1Rate;
    private String submission2Rate;

    public ComparisonResult(String studentName, String s1, String s2) {
        this.studentName = studentName;
        this.submission1Rate = s1;
        this.submission2Rate = s2;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getSubmission1Rate() {
        return submission1Rate;
    }

    public String getSubmission2Rate() {
        return submission2Rate;
    }
}
