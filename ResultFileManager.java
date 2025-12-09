/***************************************************
 * ResultFileManager.java
 * Saves & loads TestSuiteResult objects via serialization.
 ***************************************************/
import java.io.*;

public class ResultFileManager {

    // Match Coordinator: saveResult(...)
    public void saveResult(TestSuiteResult tsr, String path) {
        if (tsr == null || path == null) {
            System.out.println("Error saving result: null argument.");
            return;
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(tsr);
            out.flush();
            System.out.println("ResultFileManager: Saved TestSuiteResult to " + path);
        } catch (Exception e) {
            System.out.println("Error saving result: " + e.getMessage());
        }
    }

    // Match Coordinator: loadResult(...)
    public TestSuiteResult loadResult(String path) {
        if (path == null) {
            System.out.println("Error loading result: null path.");
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            TestSuiteResult tsr = (TestSuiteResult) in.readObject();
            System.out.println("ResultFileManager: Loaded TestSuiteResult from " + path);
            return tsr;
        } catch (Exception e) {
            System.out.println("Error loading result: " + e.getMessage());
            return null;
        }
    }
}
