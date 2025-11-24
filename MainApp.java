import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainApp extends Application{

    // Backend
    private Coordinator coordinator = new Coordinator();

    // UI-side list of test cases (for titles & listing)
    private List<TestCaseView> testCases = new ArrayList<>();

    // Buttons we need to enable/disable based on state
    private Button btnGetSuite;
    private Button btnCreateCase;
    private Button btnSaveCase;
    private Button btnExecuteSuite;

    @Override
    public void start(Stage stage) {

        Label titleLabel = new Label("Assignment Compiler");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Button btnCreateSuite = new Button("Create Test Suite");
        btnGetSuite = new Button("Get Test Suite");
        btnCreateCase = new Button("Create Test Case");
        btnSaveCase = new Button("Save Test Case");
        btnExecuteSuite = new Button("Execute Test Suite");

        btnGetSuite.setDisable(true);
        btnCreateCase.setDisable(true);
        btnSaveCase.setDisable(true);
        btnExecuteSuite.setDisable(true);

        btnCreateSuite.setOnAction(e -> showCreateTestSuitePopup(stage));
        btnGetSuite.setOnAction(e -> showGetTestSuitePopup(stage));
        btnCreateCase.setOnAction(e -> showCreateTestCasePopup(stage));
        btnSaveCase.setOnAction(e -> showSaveTestCasePopup(stage));
        btnExecuteSuite.setOnAction(e -> showExecuteTestSuitePopup(stage));

        VBox layout = new VBox(20,
                titleLabel,
                btnCreateSuite,
                btnGetSuite,
                btnCreateCase,
                btnSaveCase,
                btnExecuteSuite
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        Scene scene = new Scene(layout, 480, 520);
        stage.setTitle("Assignment Compiler");
        stage.setScene(scene);
        stage.show();
    }

    // ============= CREATE SUITE =============
    private void showCreateTestSuitePopup(Stage owner) {

        Stage popup = new Stage();
        popup.setTitle("Create Test Suite");

        Label label = new Label("Enter Test Suite Title:");
        TextField titleField = new TextField();
        Button btnDone = new Button("Done");
        btnDone.setDisable(true);

        titleField.textProperty().addListener((obs, o, n) -> btnDone.setDisable(n.trim().isEmpty()));

        btnDone.setOnAction(e -> {
            String title = titleField.getText().trim();

            coordinator.createTestSuite(title);
            TestSuite suite = coordinator.getCurrentSuite();

            popup.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Test Suite \"" + suite.getTitle() + "\" created successfully!"
            ).showAndWait();

            btnGetSuite.setDisable(false);
            btnCreateCase.setDisable(false);
            btnExecuteSuite.setDisable(false);

            showListOfTestSuiteWindow(owner);
        });

        VBox root = new VBox(15, label, titleField, btnDone);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 350, 200));
        popup.initOwner(owner);
        popup.show();
    }

    // ============= GET SUITE =============
    private void showGetTestSuitePopup(Stage owner) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Get Test Suite");
        dialog.setHeaderText("Enter the title of the Test Suite:");
        dialog.setContentText("Title:");

        dialog.showAndWait().ifPresent(userTitle -> {
            TestSuite suite = coordinator.getTestSuite(userTitle);

            if (suite != null) {
                showTestSuiteDetails(owner, suite);
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "No Test Suite found with that name."
                ).showAndWait();
            }
        });
    }

    private void showTestSuiteDetails(Stage owner, TestSuite suite) {

        Stage stage = new Stage();
        stage.setTitle("Test Suite Details");

        Label title = new Label("Test Suite: " + suite.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> list = new ListView<>();
        if (suite.getTestCases().isEmpty()) {
            list.getItems().add("(No test cases added yet)");
        } else {
            for (TestCase tc : suite.getTestCases())
                list.getItems().add(tc.getTitle());
        }

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, title, list, back);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 350, 350));
        stage.initOwner(owner);
        stage.show();
    }

    private void showListOfTestSuiteWindow(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("ListOfTestSuite");

        Label header = new Label("Test Suites");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> view = new ListView<>();
        TestSuite suite = coordinator.getCurrentSuite();
        if (suite != null)
            view.getItems().add(suite.getTitle());

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, view, back);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 350, 350));
        stage.initOwner(owner);
        stage.show();
    }

    // ============= CREATE TEST CASE (manual + upload) =============
    private void showCreateTestCasePopup(Stage owner) {

        Stage popup = new Stage();
        popup.setTitle("Create Test Case");

        Label titleLabel = new Label("Test Case Title:");
        TextField titleField = new TextField();

        Label inputLabel = new Label("Input:");
        TextArea inputArea = new TextArea();

        Label expectedLabel = new Label("Expected Output:");
        TextArea expectedArea = new TextArea();

        Button uploadBtn = new Button("Upload From File");
        Button doneBtn = new Button("Done");
        doneBtn.setDisable(true);

        Runnable validate = () -> {
            boolean ok = !titleField.getText().trim().isEmpty()
                    && !expectedArea.getText().trim().isEmpty();
            doneBtn.setDisable(!ok);
        };

        titleField.textProperty().addListener((o, ov, nv) -> validate.run());
        expectedArea.textProperty().addListener((o, ov, nv) -> validate.run());

        doneBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String input = inputArea.getText();
            String expected = expectedArea.getText();

            coordinator.createTestCase(title, input, expected);
            testCases.add(new TestCaseView(title, input, expected));

            btnSaveCase.setDisable(false);

            popup.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + title + "\" created successfully!"
            ).showAndWait();

            showListOfTestCaseScreen(owner);
        });

        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Test Case File");
            File file = fc.showOpenDialog(popup);
            if (file != null) {
                TestCase tc = coordinator.loadTestCaseFromFile(file);
                if (tc == null) {
                    showInvalidFormatModal(owner);
                } else {
                    testCases.add(new TestCaseView(tc.getTitle(), tc.getInput(), tc.getExpectedOutput()));
                    btnSaveCase.setDisable(false);

                    popup.close();

                    new Alert(Alert.AlertType.INFORMATION,
                            "Test Case \"" + tc.getTitle() + "\" uploaded successfully!"
                    ).showAndWait();

                    showListOfTestCaseScreen(owner);
                }
            }
        });

        HBox buttonRow = new HBox(10, uploadBtn, doneBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox root = new VBox(12,
                titleLabel, titleField,
                inputLabel, inputArea,
                expectedLabel, expectedArea,
                buttonRow
        );
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 450, 500));
        popup.initOwner(owner);
        popup.show();
    }

    private void showInvalidFormatModal(Stage owner) {
        Stage popup = new Stage();
        popup.setTitle("Invalid Test Case File");

        Label message = new Label("File format invalid.\n\nRequired format:\n\n" +
                "TITLE: TC_AddTwoNumbers\n\n" +
                "INPUT:\n4 5\n\n" +
                "OUTPUT:\n9");

        message.setWrapText(true);

        Button ok = new Button("OK");
        ok.setOnAction(e -> popup.close());

        VBox root = new VBox(15, message, ok);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 400, 250));
        popup.initOwner(owner);
        popup.show();
    }

    private void showListOfTestCaseScreen(Stage owner) {

    Stage stage = new Stage();
    stage.setTitle("ListOfTestCase");

    Label header = new Label("List of Test Cases");
    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    ListView<String> view = new ListView<>();
    for (TestCaseView tc : testCases)
        view.getItems().add(tc.title);

    Button addButton = new Button("Add to Test Suite");
    addButton.setOnAction(e -> showAddToSuitePopup(stage));

    Button createAnotherBtn = new Button("Create Test Case");
    createAnotherBtn.setOnAction(e -> {
        stage.close();
        showCreateTestCasePopup(owner);
    });

    Button back = new Button("Back");
    back.setOnAction(e -> stage.close());

    HBox buttonRow = new HBox(15, addButton, createAnotherBtn, back);
    buttonRow.setAlignment(Pos.CENTER);

    VBox root = new VBox(15, header, view, buttonRow);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(20));

    stage.setScene(new Scene(root, 430, 450));
    stage.initOwner(owner);
    stage.show();
}

    // ============= ADD TEST CASE TO SUITE =============
    private void showAddToSuitePopup(Stage owner) {

        Stage popup = new Stage();
        popup.setTitle("Add Test Case to Test Suite");

        Label suiteLabel = new Label("Test Suite Title:");
        TextField suiteField = new TextField();

        Label tcLabel = new Label("Test Case Title:");
        TextField tcField = new TextField();

        Button done = new Button("Done");

        done.setOnAction(e -> {
            String suiteName = suiteField.getText().trim();
            String caseName = tcField.getText().trim();

            TestSuite suite = coordinator.getCurrentSuite();

            if (suite == null || !suite.getTitle().equals(suiteName)) {
                new Alert(Alert.AlertType.ERROR, "Incorrect Test Suite title").show();
                return;
            }

            TestCaseView testCaseView = findTestCaseByTitle(caseName);
            if (testCaseView == null) {
                new Alert(Alert.AlertType.ERROR, "Test Case title is wrong or does not exist.").show();
                return;
            }

            coordinator.addTestCaseToSuite(caseName);

            popup.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + caseName + "\" added to Test Suite!"
            ).showAndWait();

            showTestSuiteWithCases(owner, suite);
        });

        VBox root = new VBox(12,
                suiteLabel, suiteField,
                tcLabel, tcField,
                done
        );
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 350, 300));
        popup.initOwner(owner);
        popup.show();
    }

    private void showTestSuiteWithCases(Stage owner, TestSuite suite) {

        Stage stage = new Stage();
        stage.setTitle("Test Suite Content");

        Label header = new Label("Test Suite: " + suite.getTitle());
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TreeItem<String> rootItem = new TreeItem<>(suite.getTitle());
        rootItem.setExpanded(true);

        for (TestCase tc : suite.getTestCases())
            rootItem.getChildren().add(new TreeItem<>(tc.getTitle()));

        TreeView<String> tree = new TreeView<>(rootItem);

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, tree, back);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 350, 450));
        stage.initOwner(owner);
        stage.show();
    }

    // ============= SAVE TEST CASE =============
    private void showSaveTestCasePopup(Stage owner) {

        Stage popup = new Stage();
        popup.setTitle("Save Test Case");

        Label tcLabel = new Label("Enter Test Case Title:");
        TextField tcField = new TextField();

        Label fileLabel = new Label("Enter filename (e.g., testcase1.txt):");
        TextField fileField = new TextField();

        Button nextBtn = new Button("Next");

        nextBtn.setOnAction(e -> {
            String tcTitle = tcField.getText().trim();
            String filename = fileField.getText().trim();

            if (tcTitle.isEmpty()) {
                new Alert(Alert.AlertType.ERROR,
                        "Test Case title is required."
                ).showAndWait();
                return;
            }
            if (filename.isEmpty()) {
                new Alert(Alert.AlertType.ERROR,
                        "Filename is required."
                ).showAndWait();
                return;
            }

            TestCaseView view = findTestCaseByTitle(tcTitle);
            if (view == null) {
                new Alert(Alert.AlertType.ERROR,
                        "Test Case title is wrong or does not exist."
                ).showAndWait();
                return;
            }

            popup.close();
            showSaveTestCasePreviewWindow(owner, tcTitle, filename);
        });

        VBox root = new VBox(12,
                tcLabel, tcField,
                fileLabel, fileField,
                nextBtn
        );
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 380, 250));
        popup.initOwner(owner);
        popup.show();
    }

    private void showSaveTestCasePreviewWindow(Stage owner, String testCaseTitle, String filename) {

        Stage stage = new Stage();
        stage.setTitle("Confirm Save Test Case");

        Label tcLabel = new Label("Test Case: " + testCaseTitle);
        tcLabel.setStyle("-fx-font-weight: bold;");

        Label fileLabel = new Label("📄 " + filename);

        Button saveBtn = new Button("Choose location and save");
        saveBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Test Case");
            chooser.setInitialFileName(filename);
            File dest = chooser.showSaveDialog(stage);
            if (dest != null) {
                boolean ok = coordinator.saveTestCaseToFile(testCaseTitle, dest.getAbsolutePath());
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR,
                            "Could not save test case. Please try again."
                    ).showAndWait();
                } else {
                    new Alert(Alert.AlertType.INFORMATION,
                            "Test Case saved successfully 😊"
                    ).showAndWait();
                    stage.close();
                }
            }
        });

        VBox root = new VBox(15, tcLabel, fileLabel, saveBtn);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 380, 200));
        stage.initOwner(owner);
        stage.show();
    }

    // ============= EXECUTE TEST SUITE (with loading) =============
    private void showExecuteTestSuitePopup(Stage owner) {

        Stage popup = new Stage();
        popup.setTitle("Execute Test Suite");

        Label suiteLabel = new Label("Test Suite Title:");
        TextField suiteField = new TextField();

        Label pathLabel = new Label("Submissions Folder Path:");
        TextField pathField = new TextField();

        Button browseBtn = new Button("Browse...");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Submissions Folder");
            File dir = chooser.showDialog(popup);
            if (dir != null) {
                pathField.setText(dir.getAbsolutePath());
            }
        });

        HBox pathRow = new HBox(10, pathField, browseBtn);
        pathRow.setAlignment(Pos.CENTER_LEFT);

        Button executeBtn = new Button("Execute");
        Button cancelBtn = new Button("Cancel");

        executeBtn.setOnAction(e -> {
            String suiteName = suiteField.getText().trim();
            String folderPath = pathField.getText().trim();

            if (suiteName.isEmpty() || folderPath.isEmpty()) {
                new Alert(Alert.AlertType.ERROR,
                        "Both Test Suite title and Submissions folder path are required."
                ).showAndWait();
                return;
            }

            // Loading dialog
            Stage loading = new Stage();
            Label loadingLabel = new Label("Executing Test Suite…");
            ProgressIndicator spinner = new ProgressIndicator();
            VBox loadingBox = new VBox(15, loadingLabel, spinner);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(20));
            loading.setScene(new Scene(loadingBox, 260, 150));
            loading.initOwner(owner);
            loading.show();

            Task<List<Result>> task = new Task<>() {
                @Override
                protected List<Result> call() {
                    return coordinator.executeTestSuite(suiteName, folderPath);
                }
            };

            task.setOnSucceeded(ev -> {
                loading.close();
                List<Result> results = task.getValue();
                if (results.isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION,
                            "Execution finished, but no results were produced."
                    ).showAndWait();
                } else {
                    popup.close();
                    showClassReportWindow(owner, results);
                }
            });

            task.setOnFailed(ev -> {
                loading.close();
                Throwable ex = task.getException();
                new Alert(Alert.AlertType.ERROR,
                        "Execution failed: " + (ex == null ? "" : ex.getMessage())
                ).showAndWait();
                ex.printStackTrace();
            });

            new Thread(task).start();
        });

        cancelBtn.setOnAction(e -> popup.close());

        HBox buttonRow = new HBox(10, executeBtn, cancelBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox root = new VBox(12,
                suiteLabel, suiteField,
                pathLabel, pathRow,
                buttonRow
        );
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 500, 230));
        popup.initOwner(owner);
        popup.show();
    }

    // ============= CLASS REPORT (color-coded + save report) =============
    private void showClassReportWindow(Stage owner, List<Result> results) {

        Stage stage = new Stage();
        stage.setTitle("Class Report");

        Label header = new Label("=== CLASS REPORT ===");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Map<String, List<Result>> byStudent = results.stream()
                .collect(Collectors.groupingBy(Result::getProgramName));

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        for (Map.Entry<String, List<Result>> entry : byStudent.entrySet()) {
            String student = entry.getKey();
            List<Result> studentResults = entry.getValue();

            VBox studentBox = new VBox(5);
            Label studentLabel = new Label("Student: " + student);
            studentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Separator sep = new Separator();
            studentBox.getChildren().addAll(studentLabel, sep);

            boolean hasCompilationError = studentResults.stream()
                    .anyMatch(Result::isCompilationError);

            if (hasCompilationError) {
                Result comp = studentResults.stream()
                        .filter(Result::isCompilationError)
                        .findFirst().orElse(null);

                Label compErr = new Label("COMPILATION ERROR — No test cases executed");
                compErr.setStyle("-fx-text-fill: red;");

                Button viewBtn = new Button("View");
                Result finalComp = comp;
                viewBtn.setOnAction(e ->
                        showResultDetailsWindow(student, finalComp)
                );

                HBox row = new HBox(10, compErr, viewBtn);
                row.setAlignment(Pos.CENTER_LEFT);
                studentBox.getChildren().add(row);

            } else {
                for (Result r : studentResults) {
                    String status = r.isPassed() ? "PASS" : "FAIL";
                    Label line = new Label(
                            String.format("%-20s ............ %s", r.getTestCaseTitle(), status)
                    );
                    if (r.isPassed()) {
                        line.setStyle("-fx-text-fill: green;");
                    } else {
                        line.setStyle("-fx-text-fill: red;");
                    }

                    Button viewBtn = new Button("View");
                    viewBtn.setOnAction(e ->
                            showResultDetailsWindow(student, r)
                    );

                    HBox row = new HBox(10, line, viewBtn);
                    row.setAlignment(Pos.CENTER_LEFT);

                    studentBox.getChildren().add(row);
                }
            }

            content.getChildren().add(studentBox);
            content.getChildren().add(new Separator());
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);

        Button saveReportBtn = new Button("Save Class Report");
        saveReportBtn.setOnAction(e -> saveClassReportToFile(stage, results));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> stage.close());

        HBox bottomRow = new HBox(10, saveReportBtn, closeBtn);
        bottomRow.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, header, scroll, bottomRow);
        root.setPadding(new Insets(15));

        stage.setScene(new Scene(root, 650, 520));
        stage.initOwner(owner);
        stage.show();
    }

    // Save whole class report as formatted text
    private void saveClassReportToFile(Stage owner, List<Result> results) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Class Report");
        chooser.setInitialFileName("ClassReport.txt");
        File dest = chooser.showSaveDialog(owner);
        if (dest == null) return;

        Map<String, List<Result>> byStudent = results.stream()
                .collect(Collectors.groupingBy(Result::getProgramName));

        try (PrintWriter out = new PrintWriter(dest)) {
            out.println("=== CLASS REPORT ===");
            out.println();

            for (Map.Entry<String, List<Result>> entry : byStudent.entrySet()) {
                String student = entry.getKey();
                List<Result> studentResults = entry.getValue();

                out.println("Student: " + student);
                out.println("----------------------------------------");

                boolean hasCompilationError = studentResults.stream()
                        .anyMatch(Result::isCompilationError);

                if (hasCompilationError) {
                    Result comp = studentResults.stream()
                            .filter(Result::isCompilationError)
                            .findFirst().orElse(null);
                    out.println("COMPILATION ERROR:");
                    if (comp != null && comp.getCompileError() != null) {
                        out.println(comp.getCompileError());
                    }
                    out.println();
                } else {
                    for (Result r : studentResults) {
                        String status = r.isPassed() ? "PASS" : "FAIL";
                        out.printf("%s ............ %s%n", r.getTestCaseTitle(), status);

                        if (!r.isPassed()) {
                            out.println("Expected Output:");
                            out.println(r.getExpected());
                            out.println();
                            out.println("Actual Output:");
                            out.println(r.getActual());
                            out.println();
                        }
                    }
                    out.println();
                }
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "Class report saved successfully."
            ).showAndWait();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to save class report: " + ex.getMessage()
            ).showAndWait();
        }
    }

    // ============= RESULT DETAILS =============
    private void showResultDetailsWindow(String studentName, Result result) {

        Stage stage = new Stage();
        stage.setTitle("Result Details");

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));

        Label studentLabel = new Label("Student: " + studentName);
        Label testLabel = new Label("Test Case: " + result.getTestCaseTitle());
        root.getChildren().addAll(studentLabel, testLabel);

        if (result.isCompilationError()) {
            Label status = new Label("COMPILATION ERROR:");
            status.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

            TextArea area = new TextArea(result.getCompileError());
            area.setEditable(false);
            area.setWrapText(true);

            root.getChildren().addAll(status, area);

        } else {
            Label status = new Label("Status: " + (result.isPassed() ? "PASS" : "FAIL"));
            status.setStyle(result.isPassed()
                    ? "-fx-text-fill: green; -fx-font-weight: bold;"
                    : "-fx-text-fill: red; -fx-font-weight: bold;");

            Label expectedLabel = new Label("Expected Output:");
            TextArea expectedArea = new TextArea(result.getExpected());
            expectedArea.setEditable(false);

            Label actualLabel = new Label("Actual Output:");
            TextArea actualArea = new TextArea(result.getActual());
            actualArea.setEditable(false);

            root.getChildren().addAll(
                    status,
                    expectedLabel, expectedArea,
                    actualLabel, actualArea
            );
        }

        Button saveBtn = new Button("Save Result");
        saveBtn.setOnAction(e -> saveResultToFile(studentName, result, stage));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> stage.close());

        HBox row = new HBox(10, saveBtn, closeBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(row);

        stage.setScene(new Scene(root, 430, 460));
        stage.show();
    }

    // save single result
    private void saveResultToFile(String studentName, Result result, Stage owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Test Result");
        chooser.setInitialFileName(result.getTestCaseTitle() + "_" + studentName + ".txt");
        File dest = chooser.showSaveDialog(owner);
        if (dest == null) return;

        try (PrintWriter out = new PrintWriter(dest)) {
            out.println("Student: " + studentName);
            out.println("Test Case: " + result.getTestCaseTitle());
            out.println("----------------------------------------");

            if (result.isCompilationError()) {
                out.println("COMPILATION ERROR:");
                out.println(result.getCompileError());
            } else {
                out.println("Status: " + (result.isPassed() ? "PASS" : "FAIL"));
                out.println();
                out.println("Expected Output:");
                out.println(result.getExpected());
                out.println();
                out.println("Actual Output:");
                out.println(result.getActual());
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "Result saved successfully."
            ).showAndWait();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to save result: " + ex.getMessage()
            ).showAndWait();
        }
    }

    // ============= UTIL =============
    private TestCaseView findTestCaseByTitle(String title) {
        for (TestCaseView tc : testCases)
            if (tc.title.equals(title))
                return tc;
        return null;
    }

    private static class TestCaseView {
        private final String title;
        private final String input;
        private final String expected;

        public TestCaseView(String title, String input, String expected) {
            this.title = title;
            this.input = input;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
