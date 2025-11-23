import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    // Stored test suite and test cases (UI-level)
    private String createdSuiteTitle = null;            // Only one suite allowed in this design
    private List<TestCaseView> testCases = new ArrayList<>();

    @Override
    public void start(Stage stage) {

        // -----------------------------
        // HOME SCREEN TITLE
        // -----------------------------
        Label titleLabel = new Label("Assignment Compiler");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // -----------------------------
        // MAIN BUTTONS
        // -----------------------------
        Button btnCreateSuite = new Button("Create Test Suite");
        Button btnGetSuite = new Button("Get Test Suite");
        Button btnCreateCase = new Button("Create Test Case");
        Button btnExecuteSuite = new Button("Execute Test Suite");

        // Disable unavailable actions at start
        btnGetSuite.setDisable(true);
        btnCreateCase.setDisable(true);
        btnExecuteSuite.setDisable(true); // will be enabled later when needed

        // Actions
        btnCreateSuite.setOnAction(e -> showCreateTestSuitePopup(stage, btnGetSuite, btnCreateCase));
        btnGetSuite.setOnAction(e -> showGetTestSuitePopup(stage));
        btnCreateCase.setOnAction(e -> showCreateTestCasePopup(stage));

        // Layout
        VBox layout = new VBox(20, titleLabel, btnCreateSuite, btnGetSuite, btnCreateCase, btnExecuteSuite);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        Scene scene = new Scene(layout, 450, 450);
        stage.setTitle("Assignment Compiler");
        stage.setScene(scene);
        stage.show();
    }

    // =========================================================================
    // 1. CREATE TEST SUITE POPUP
    // =========================================================================
    private void showCreateTestSuitePopup(Stage owner, Button btnGetSuite, Button btnCreateCase) {

        Stage popup = new Stage();
        popup.setTitle("Create Test Suite");

        Label label = new Label("Enter Test Suite Title:");
        TextField titleField = new TextField();
        Button btnDone = new Button("Done");
        btnDone.setDisable(true);

        titleField.textProperty().addListener((obs, o, n) -> btnDone.setDisable(n.trim().isEmpty()));

        btnDone.setOnAction(e -> {
            createdSuiteTitle = titleField.getText().trim();
            popup.close();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setHeaderText(null);
            success.setTitle("Success");
            success.setContentText("Test Suite \"" + createdSuiteTitle + "\" created successfully!");
            success.showAndWait();

            btnGetSuite.setDisable(false);
            btnCreateCase.setDisable(false);

            showListOfTestSuiteWindow(owner);
        });

        VBox root = new VBox(15, label, titleField, btnDone);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 350, 200));
        popup.initOwner(owner);
        popup.show();
    }

    // =========================================================================
    // 2. GET TEST SUITE POPUP
    // =========================================================================
    private void showGetTestSuitePopup(Stage owner) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Get Test Suite");
        dialog.setHeaderText("Enter the title of the Test Suite:");
        dialog.setContentText("Title:");

        dialog.showAndWait().ifPresent(title -> {
            if (createdSuiteTitle != null && title.equals(createdSuiteTitle)) {
                showTestSuiteDetails(owner);
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "No Test Suite found with that name.");
                err.show();
            }
        });
    }

    // =========================================================================
    // 3. SHOW TEST SUITE DETAILS
    // =========================================================================
    private void showTestSuiteDetails(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("Test Suite Details");

        Label title = new Label("Test Suite: " + createdSuiteTitle);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> list = new ListView<>();
        for (TestCaseView tc : testCases) list.getItems().add(tc.getTitle());

        Label empty = new Label("No test cases added yet.");
        empty.setStyle("-fx-text-fill: gray;");

        VBox root = new VBox(15, title);
        root.setPadding(new Insets(20));

        if (testCases.isEmpty()) root.getChildren().add(empty);
        else root.getChildren().add(list);

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());
        root.getChildren().add(back);

        stage.setScene(new Scene(root, 350, 350));
        stage.initOwner(owner);
        stage.show();
    }

    // =========================================================================
    // 4. SHOW LIST OF TEST SUITES
    // =========================================================================
    private void showListOfTestSuiteWindow(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("ListOfTestSuite");

        Label header = new Label("Test Suites");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> view = new ListView<>();
        view.getItems().add(createdSuiteTitle);

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, view, back);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 350, 350));
        stage.initOwner(owner);
        stage.show();
    }

    // =========================================================================
    // 5. CREATE TEST CASE POPUP (MANUAL ENTRY + UPLOAD OPTION)
    // =========================================================================
    private void showCreateTestCasePopup(Stage owner) {

        Stage popup = new Stage();
        popup.setTitle("Create Test Case");

        Label titleLabel = new Label("Test Case Title:");
        TextField titleField = new TextField();

        Label inputLabel = new Label("Input:");
        TextArea inputArea = new TextArea();

        Label expectedLabel = new Label("Expected Output:");
        TextArea expectedArea = new TextArea();

        Button uploadBtn = new Button("Upload File");
        Button doneBtn = new Button("Done");
        doneBtn.setDisable(true);

        // Validation for manual path: require title + expected (input optional)
        Runnable validate = () -> {
            boolean ok = !titleField.getText().trim().isEmpty()
                    && !expectedArea.getText().trim().isEmpty();
            doneBtn.setDisable(!ok);
        };

        titleField.textProperty().addListener((o, ov, nv) -> validate.run());
        expectedArea.textProperty().addListener((o, ov, nv) -> validate.run());

        // Manual "Done" handler
        doneBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String input = inputArea.getText();
            String expected = expectedArea.getText();

            testCases.add(new TestCaseView(title, input, expected, null));

            popup.close();
            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + title + "\" created successfully!"
            ).showAndWait();

            showListOfTestCaseScreen(owner);
        });

        // Upload path: user chooses a file instead of typing everything
        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Test Case File");
            File file = fc.showOpenDialog(popup);
            if (file != null) {
                // Close manual create popup and continue with upload flow
                popup.close();
                showUploadFileTestCaseFlow(owner, file);
            }
        });

        HBox bottomButtons = new HBox(10, uploadBtn, doneBtn);
        bottomButtons.setAlignment(Pos.CENTER);

        VBox root = new VBox(12,
                titleLabel, titleField,
                inputLabel, inputArea,
                expectedLabel, expectedArea,
                bottomButtons
        );
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 450, 500));
        popup.initOwner(owner);
        popup.show();
    }

    // =========================================================================
    // 5b. UPLOAD-FILE TEST CASE FLOW
    // =========================================================================
    private void showUploadFileTestCaseFlow(Stage owner, File file) {

        Stage popup = new Stage();
        popup.setTitle("Upload Test Case From File");

       String fullName = file.getName();
String baseName = (fullName.lastIndexOf('.') > 0)
        ? fullName.substring(0, fullName.lastIndexOf('.'))
        : fullName;

Label infoLabel = new Label("Test Case created from file:");
Label titleLabel = new Label("Title: " + baseName);
titleLabel.setStyle("-fx-font-weight: bold;");

Label fileLabel = new Label("📄 " + fullName);

Button doneBtn = new Button("Done");
doneBtn.setOnAction(e -> {
    testCases.add(new TestCaseView(baseName, "", "", file));
    popup.close();
    new Alert(Alert.AlertType.INFORMATION,
            "Test Case \"" + baseName + "\" created successfully from file!"
    ).showAndWait();
    showListOfTestCaseScreen(owner);
});

        VBox root = new VBox(15, infoLabel, titleLabel, fileLabel, doneBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 400, 250));
        popup.initOwner(owner);
        popup.show();
    }

    // =========================================================================
    // 6. LIST OF TEST CASES SCREEN
    // =========================================================================
    private void showListOfTestCaseScreen(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("ListOfTestCase");

        Label header = new Label("List of Test Cases");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> view = new ListView<>();
        for (TestCaseView tc : testCases)
            view.getItems().add(tc.getTitle());

        Button addButton = new Button("Add to Test Suite");
        addButton.setOnAction(e -> showAddToSuitePopup(stage));

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, view, addButton, back);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 400, 450));
        stage.initOwner(owner);
        stage.show();
    }

    // =========================================================================
    // 7. ADD TEST CASE TO TEST SUITE POPUP
    // =========================================================================
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

            if (createdSuiteTitle == null || !suiteName.equals(createdSuiteTitle)) {
                new Alert(Alert.AlertType.ERROR, "Incorrect Test Suite name").show();
                return;
            }

            TestCaseView match = findTestCaseByTitle(caseName);
            if (match == null) {
                new Alert(Alert.AlertType.ERROR, "Test Case name not found").show();
                return;
            }

            popup.close();

            // In your real system, this is where you'd call:
            // coordinator.addTestCaseToSuite(caseName);
            // For now, we just show success and suite contents.

            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + caseName + "\" added to Test Suite!"
            ).showAndWait();

            showTestSuiteWithCases(owner);
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

    // =========================================================================
    // 8. SHOW SUITE WITH TEST CASES (TREE VIEW)
    // =========================================================================
    private void showTestSuiteWithCases(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("Test Suite Content");

        Label header = new Label("Test Suite: " + createdSuiteTitle);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TreeItem<String> rootItem = new TreeItem<>(createdSuiteTitle);
        rootItem.setExpanded(true);

        for (TestCaseView tc : testCases)
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

    // Utility
    private TestCaseView findTestCaseByTitle(String title) {
        for (TestCaseView tc : testCases)
            if (tc.getTitle().equals(title))
                return tc;
        return null;
    }

    // Simple view model for test cases (UI-side)
    private static class TestCaseView {
        private final String title;
        private final String input;
        private final String expected;
        private final File sourceFile; // can be null if manually created

        public TestCaseView(String title, String input, String expected, File sourceFile) {
            this.title = title;
            this.input = input;
            this.expected = expected;
            this.sourceFile = sourceFile;
        }

        public String getTitle() { return title; }
        public String getInput() { return input; }
        public String getExpected() { return expected; }
        public File getSourceFile() { return sourceFile; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
