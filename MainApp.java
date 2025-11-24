import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

        // Title
        Label titleLabel = new Label("Assignment Compiler");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // Buttons
        Button btnCreateSuite = new Button("Create Test Suite");
        btnGetSuite = new Button("Get Test Suite");
        btnCreateCase = new Button("Create Test Case");
        btnSaveCase = new Button("Save Test Case");
        btnExecuteSuite = new Button("Execute Test Suite");

        // Initial state
        btnGetSuite.setDisable(true);
        btnCreateCase.setDisable(true);
        btnSaveCase.setDisable(true);
        btnExecuteSuite.setDisable(true); // exec wired later

        // Actions
        btnCreateSuite.setOnAction(e -> showCreateTestSuitePopup(stage));
        btnGetSuite.setOnAction(e -> showGetTestSuitePopup(stage));
        btnCreateCase.setOnAction(e -> showCreateTestCasePopup(stage));
        btnSaveCase.setOnAction(e -> showSaveTestCasePopup(stage));
        btnExecuteSuite.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION,
                        "Execute Test Suite UI will be implemented later."
                ).showAndWait()
        );

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

        Scene scene = new Scene(layout, 450, 500);
        stage.setTitle("Assignment Compiler");
        stage.setScene(scene);
        stage.show();
    }

    // =========================================================================
    // CREATE TEST SUITE POPUP
    // =========================================================================
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

            // backend
            coordinator.createTestSuite(title);
            TestSuite suite = coordinator.getCurrentSuite();

            // debug
            System.out.println("[DEBUG] Created TestSuite: " + suite.getTitle());

            popup.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Test Suite \"" + suite.getTitle() + "\" created successfully!"
            ).showAndWait();

            // enable related buttons
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
    // GET TEST SUITE POPUP
    // =========================================================================
    private void showGetTestSuitePopup(Stage owner) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Get Test Suite");
        dialog.setHeaderText("Enter the title of the Test Suite:");
        dialog.setContentText("Title:");

        dialog.showAndWait().ifPresent(userTitle -> {
            // go through coordinator
            TestSuite suite = coordinator.getTestSuite(userTitle);

            if (suite != null) {
                System.out.println("[DEBUG] Retrieved TestSuite from Coordinator: " + suite.getTitle());
                showTestSuiteDetails(owner, suite);
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "No Test Suite found with that name."
                ).showAndWait();
            }
        });
    }

    // =========================================================================
    // SHOW TEST SUITE DETAILS
    // =========================================================================
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

    // =========================================================================
    // LIST OF TEST SUITES WINDOW
    // =========================================================================
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

    // =========================================================================
    // CREATE TEST CASE POPUP (MANUAL + CASE 2 UPLOAD)
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

        Button uploadBtn = new Button("Upload From File"); // Case 2
        Button doneBtn = new Button("Done");
        doneBtn.setDisable(true);

        // validate manual fields
        Runnable validate = () -> {
            boolean ok = !titleField.getText().trim().isEmpty()
                    && !expectedArea.getText().trim().isEmpty();
            doneBtn.setDisable(!ok);
        };

        titleField.textProperty().addListener((o, ov, nv) -> validate.run());
        expectedArea.textProperty().addListener((o, ov, nv) -> validate.run());

        // Manual done
        doneBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String input = inputArea.getText();
            String expected = expectedArea.getText();

            // backend
            coordinator.createTestCase(title, input, expected);

            // UI list
            testCases.add(new TestCaseView(title, input, expected));

            btnSaveCase.setDisable(false);

            popup.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + title + "\" created successfully!"
            ).showAndWait();

            showListOfTestCaseScreen(owner);
        });

        // Upload from file (Case 2)
        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Test Case File");
            File file = fc.showOpenDialog(popup);
            if (file != null) {
                // let backend try to parse and create TestCase
                TestCase tc = coordinator.loadTestCaseFromFile(file);
                if (tc == null) {
                    // invalid format: show the required format in a modal
                    showInvalidFormatModal(owner);
                } else {
                    // success
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

    // =========================================================================
    // MODAL TO SHOW REQUIRED FILE FORMAT (Case 2 invalid format)
    // =========================================================================
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

    // =========================================================================
    // LIST OF TEST CASES SCREEN
    // =========================================================================
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
    // ADD TEST CASE TO TEST SUITE POPUP
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

            // backend: actually add it
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

    // =========================================================================
    // SHOW SUITE WITH TEST CASES (TREE VIEW)
    // =========================================================================
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

    // =========================================================================
    // SAVE TEST CASE POPUP (HOME SCREEN BUTTON) – Case 3 (step 1)
    // =========================================================================
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

            // validate title exists
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

    // =========================================================================
    // SAVE TEST CASE PREVIEW WINDOW – Case 3 (step 2)
    // =========================================================================
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

    // =========================================================================
    // UTILITY
    // =========================================================================
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
