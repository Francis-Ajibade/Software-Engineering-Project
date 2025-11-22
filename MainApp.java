import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.SplitPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * PURE JavaFX UI for your system (no FXML).
 *
 * Features implemented now:
 *  - Create Test Suite (title only)
 *  - Create Test Case (title, input, expected, optional upload)
 *  - List of Test Cases screen with "Add to Test Suite" button
 *  - Folder-style view of Test Suite and its Test Cases
 *
 * All logic passes through your Coordinator class.
 */
public class MainApp extends Application {

    // Your backend coordinator (uses ListOfTestSuite, ListOfProgram, ListofTestCase, etc.)
    private Coordinator coordinator = new Coordinator();

    // UI-side list of created test cases (for display & validation only)
    private ObservableList<TestCaseView> testCases = FXCollections.observableArrayList();

    // Buttons on main menu
    private Button createTestSuiteButton;
    private Button createTestCaseButton;
    private Button executeTestSuiteButton;

    @Override
    public void start(Stage primaryStage) {
        Scene mainScene = buildMainMenuScene(primaryStage);

        primaryStage.setTitle("Programming Assignment Testing Tool");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // ============================================================
    //                        MAIN MENU
    // ============================================================
    private Scene buildMainMenuScene(Stage primaryStage) {
        Label titleLabel = new Label("Assignment Testing Tool");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        createTestSuiteButton = new Button("Create Test Suite");
        createTestSuiteButton.setMaxWidth(Double.MAX_VALUE);
        createTestSuiteButton.setOnAction(e -> showCreateTestSuiteDialog(primaryStage));

        createTestCaseButton = new Button("Create Test Case");
        createTestCaseButton.setMaxWidth(Double.MAX_VALUE);
        createTestCaseButton.setDisable(true); // disabled until we have a suite
        createTestCaseButton.setOnAction(e -> showCreateTestCaseDialog(primaryStage));

        executeTestSuiteButton = new Button("Execute Test Suite");
        executeTestSuiteButton.setMaxWidth(Double.MAX_VALUE);
        executeTestSuiteButton.setDisable(true); // enabled later when suite + at least one TC exist
        executeTestSuiteButton.setOnAction(e ->
                showInfo("Execute Test Suite",
                        "This will be connected to Coordinator.executeTestSuite(suiteName, path) later.")
        );

        VBox buttonBox = new VBox(10,
                createTestSuiteButton,
                createTestCaseButton,
                executeTestSuiteButton
        );
        buttonBox.setPadding(new Insets(20));
        buttonBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setMargin(titleLabel, new Insets(20, 0, 0, 0));
        root.setCenter(buttonBox);

        return new Scene(root, 400, 250);
    }

    // ============================================================
    //                  CREATE TEST SUITE FLOW
    // ============================================================
    private void showCreateTestSuiteDialog(Stage owner) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create Test Suite");
        dialog.setHeaderText("Specify the title for the new Test Suite.");
        dialog.initOwner(owner);

        Label nameLabel = new Label("Test Suite Title:");
        TextField titleField = new TextField();
        titleField.setPromptText("e.g., Assignment1_Suite");

        VBox content = new VBox(10, nameLabel, titleField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType createType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createType);
        createBtn.setDisable(true);

        titleField.textProperty().addListener((obs, oldVal, newVal) ->
                createBtn.setDisable(newVal.trim().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == createType) {
                return titleField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(title -> {
            // Call YOUR Coordinator
            coordinator.createTestSuite(title);

            // Enable Create Test Case now that we have a suite
            createTestCaseButton.setDisable(false);

            showInfo("Success", "Test Suite \"" + title + "\" created successfully.");
            // Not opening any extra window here – the test suite will be visible later
            // in the folder view after adding test cases.
        });
    }

    // ============================================================
    //                  CREATE TEST CASE FLOW
    // ============================================================
    private void showCreateTestCaseDialog(Stage owner) {
        if (coordinator.getCurrentSuite() == null) {
            showError("No Test Suite",
                    "Please create a Test Suite before creating Test Cases.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Create Test Case");
        stage.initOwner(owner);

        TextField titleField = new TextField();
        titleField.setPromptText("Test Case Title");

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Input data...");

        TextArea expectedArea = new TextArea();
        expectedArea.setPromptText("Expected output...");

        Button uploadButton = new Button("Upload From File");
        uploadButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select File");
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    // You can decide: input or expected
                    inputArea.setText(content);
                } catch (IOException ex) {
                    showError("File Error", "Could not read file: " + ex.getMessage());
                }
            }
        });

        Button createButton = new Button("Create Test Case");
        createButton.setDisable(true);

        Runnable updateState = () -> {
            boolean enable = !titleField.getText().trim().isEmpty()
                    && !expectedArea.getText().trim().isEmpty();
            createButton.setDisable(!enable);
        };

        titleField.textProperty().addListener((o, ov, nv) -> updateState.run());
        expectedArea.textProperty().addListener((o, ov, nv) -> updateState.run());

        createButton.setOnAction(e -> {
            String tcTitle = titleField.getText().trim();
            String input = inputArea.getText();
            String expected = expectedArea.getText();

            // Call YOUR Coordinator to store globally
            coordinator.createTestCase(tcTitle, input, expected);

            // Store in UI list for display and validation
            testCases.add(new TestCaseView(tcTitle, input, expected));

            showInfo("Success", "Test Case \"" + tcTitle + "\" created successfully.");

            stage.close();

            // After creating, show the Test Case list screen with "Add to Test Suite"
            showTestCaseListWindow();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        HBox bottomButtons = new HBox(10, uploadButton, createButton, cancelButton);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10,
                new Label("Test Case Title:"), titleField,
                new Label("Input:"), inputArea,
                new Label("Expected Output:"), expectedArea,
                bottomButtons
        );
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    // ============================================================
    //                  TEST CASE LIST + ADD TO SUITE
    // ============================================================
    private void showTestCaseListWindow() {
        Stage stage = new Stage();
        stage.setTitle("List of Test Cases");

        Label header = new Label("Test Cases");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<TestCaseView> listView = new ListView<>(testCases);

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPromptText("Select a test case to see details here.");

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                detailsArea.setText(
                        "Title: " + newSel.getTitle() + "\n\n" +
                        "Input:\n" + newSel.getInput() + "\n\n" +
                        "Expected Output:\n" + newSel.getExpected()
                );
            } else {
                detailsArea.clear();
            }
        });

        Button addToSuiteButton = new Button("Add to Test Suite");
        addToSuiteButton.setDisable(testCases.isEmpty());
        addToSuiteButton.setOnAction(e -> showAddTestCaseToSuiteDialog(stage));

        Button closeButton = new Button("Back");
        closeButton.setOnAction(e -> stage.close());

        HBox bottomButtons = new HBox(10, addToSuiteButton, closeButton);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        bottomButtons.setPadding(new Insets(10));

        SplitPane splitPane = new SplitPane(listView, detailsArea);
        splitPane.setDividerPositions(0.3);

        VBox root = new VBox(10, header, splitPane, bottomButtons);
        root.setPadding(new Insets(10));
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Dialog to ask the user which test case (by title) should be added to the current Test Suite.
     */
    private void showAddTestCaseToSuiteDialog(Stage owner) {
        if (coordinator.getCurrentSuite() == null) {
            showError("No Test Suite", "There is no current Test Suite. Create one first.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Test Case to Test Suite");
        dialog.setHeaderText("Enter the title of the Test Case to add to the current Test Suite.");

        Label label = new Label("Test Case Title:");
        TextField titleField = new TextField();
        titleField.setPromptText("Exact test case title...");

        VBox content = new VBox(10, label, titleField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType addType = new ButtonType("Add", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addType);
        addBtn.setDisable(true);

        titleField.textProperty().addListener((obs, oldVal, newVal) ->
                addBtn.setDisable(newVal.trim().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == addType) {
                return titleField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(tcTitle -> {
            // Validate against UI list first
            TestCaseView view = findTestCaseViewByTitle(tcTitle);
            if (view == null) {
                showError("Invalid Title",
                        "No Test Case found with title: \"" + tcTitle + "\".\n" +
                        "Please make sure you typed it exactly as in the list.");
                return;
            }

            // Call YOUR Coordinator method
            coordinator.addTestCaseToSuite(tcTitle);

            // Now show folder-style suite + test cases view
            showSuiteTreeViewWindow();

            // After at least one testcase is added to the suite, we can enable execute
            executeTestSuiteButton.setDisable(false);

            showInfo("Success",
                    "Test Case \"" + tcTitle + "\" was added to the current Test Suite.");
        });
    }

    private TestCaseView findTestCaseViewByTitle(String title) {
        for (TestCaseView tc : testCases) {
            if (tc.getTitle().equals(title)) {
                return tc;
            }
        }
        return null;
    }

    // ============================================================
    //                  SUITE + TEST CASES FOLDER VIEW
    // ============================================================
    private void showSuiteTreeViewWindow() {
        TestSuite currentSuite = coordinator.getCurrentSuite();
        if (currentSuite == null) {
            showError("No Test Suite", "There is no current Test Suite to display.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Test Suite and Test Cases");

        // Assumes TestSuite has getTitle() and getTestCases() methods
        String suiteTitle;
        try {
            suiteTitle = currentSuite.getTitle();
        } catch (NoSuchMethodError e) {
            suiteTitle = "Current Suite";
        }

        Label header = new Label("Test Suite: " + suiteTitle);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TreeItem<String> rootItem = new TreeItem<>(suiteTitle);
        rootItem.setExpanded(true);

        // Assumes TestSuite.getTestCases() returns a collection of TestCase
        for (TestCase tc : currentSuite.getTestCases()) {
            rootItem.getChildren().add(new TreeItem<>(tc.getTitle()));
        }

        TreeView<String> treeView = new TreeView<>(rootItem);

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPromptText("Select a test case to see its details here.");

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && newSel != rootItem) {
                String tcTitle = newSel.getValue();
                // Map back to UI view model by title so we can show input & expected
                TestCaseView view = findTestCaseViewByTitle(tcTitle);
                if (view != null) {
                    detailsArea.setText(
                            "Title: " + view.getTitle() + "\n\n" +
                            "Input:\n" + view.getInput() + "\n\n" +
                            "Expected Output:\n" + view.getExpected()
                    );
                } else {
                    detailsArea.setText("Details not available for this test case.");
                }
            } else {
                detailsArea.clear();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> stage.close());

        HBox bottom = new HBox(10, backButton);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(10));

        SplitPane splitPane = new SplitPane(treeView, detailsArea);
        splitPane.setDividerPositions(0.3);

        VBox root = new VBox(10, header, splitPane, bottom);
        root.setPadding(new Insets(10));
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    // ============================================================
    //                       UTIL ALERTS
    // ============================================================
    private void showInfo(String title, String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ============================================================
    //                 SIMPLE UI VIEW MODEL CLASS
    // ============================================================
    private static class TestCaseView {
        private final String title;
        private final String input;
        private final String expected;

        public TestCaseView(String title, String input, String expected) {
            this.title = title;
            this.input = input;
            this.expected = expected;
        }

        public String getTitle() {
            return title;
        }

        public String getInput() {
            return input;
        }

        public String getExpected() {
            return expected;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
