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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MainApp extends Application {

    private Coordinator coordinator = new Coordinator(); // FULL backend connection

    @Override
    public void start(Stage stage) {

        // ----------------------------------------------------
        // HOME SCREEN
        // ----------------------------------------------------
        Label titleLabel = new Label("Assignment Compiler");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Button btnCreateSuite = new Button("Create Test Suite");
        Button btnGetSuite = new Button("Get Test Suite");
        Button btnCreateCase = new Button("Create Test Case");
        Button btnExecuteSuite = new Button("Execute Test Suite");

        btnGetSuite.setDisable(true);
        btnCreateCase.setDisable(true);
        btnExecuteSuite.setDisable(true);

        btnCreateSuite.setOnAction(e -> showCreateTestSuitePopup(stage, btnGetSuite, btnCreateCase));
        btnGetSuite.setOnAction(e -> showGetTestSuitePopup(stage));
        btnCreateCase.setOnAction(e -> showCreateTestCasePopup(stage));

        VBox layout = new VBox(20, titleLabel, btnCreateSuite, btnGetSuite, btnCreateCase, btnExecuteSuite);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        Scene scene = new Scene(layout, 450, 450);
        stage.setScene(scene);
        stage.setTitle("Assignment Compiler");
        stage.show();
    }

    // ========================================================================
    // CREATE TEST SUITE POPUP (Fully connected to Coordinator)
    // ========================================================================
    private void showCreateTestSuitePopup(Stage owner, Button btnGetSuite, Button btnCreateCase) {

        Stage popup = new Stage();
        popup.setTitle("Create Test Suite");

        Label label = new Label("Enter Test Suite Title:");
        TextField titleField = new TextField();
        Button btnDone = new Button("Done");
        btnDone.setDisable(true);

        titleField.textProperty().addListener((obs, o, n) -> btnDone.setDisable(n.trim().isEmpty()));

        btnDone.setOnAction(e -> {

            // Backend call
            String title = titleField.getText().trim();
            coordinator.createTestSuite(title);

            popup.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Test Suite \"" + title + "\" created successfully!");
            alert.showAndWait();

            // Enable remaining UI
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

    // ========================================================================
    // GET TEST SUITE POPUP
    // ========================================================================
    private void showGetTestSuitePopup(Stage owner) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter Test Suite Title:");
        dialog.setContentText("Title:");

        dialog.showAndWait().ifPresent(input -> {
            TestSuite suite = coordinator.getCurrentSuite();

            if (suite != null && suite.getTitle().equals(input)) {
                showTestSuiteDetailsWindow(owner);
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "No Test Suite found with that name.").show();
            }
        });
    }

    // ========================================================================
    // SHOW LIST OF TEST SUITE (Backend-driven)
    // ========================================================================
    private void showListOfTestSuiteWindow(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("ListOfTestSuite");

        Label header = new Label("Test Suites");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> list = new ListView<>();
        TestSuite suite = coordinator.getCurrentSuite();

        if (suite != null) {
            list.getItems().add(suite.getTitle());
        }

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, list, back);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 350, 300));
        stage.initOwner(owner);
        stage.show();
    }

    // ========================================================================
    // TEST SUITE DETAILS WINDOW (REAL backend data)
    // ========================================================================
    private void showTestSuiteDetailsWindow(Stage owner) {

        TestSuite suite = coordinator.getCurrentSuite();
        Stage stage = new Stage();

        Label header = new Label("Test Suite: " + suite.getTitle());
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> list = new ListView<>();

        if (suite.getTestCases().isEmpty()) {
            list.getItems().add("No Test Cases Added.");
        } else {
            for (TestCase tc : suite.getTestCases()) {
                list.getItems().add(tc.getTitle());
            }
        }

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, list, back);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 350, 350));
        stage.initOwner(owner);
        stage.show();
    }

    // ========================================================================
    // CREATE TEST CASE POPUP (manual + upload)
    // ========================================================================
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

            popup.close();
            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + title + "\" created successfully!").showAndWait();

            showListOfTestCaseScreen(owner);
        });

        uploadBtn.setOnAction(e -> {
            popup.close();
            handleUploadFileCase(owner);
        });

        HBox controls = new HBox(10, uploadBtn, doneBtn);
        controls.setAlignment(Pos.CENTER);

        VBox root = new VBox(12,
                titleLabel, titleField,
                inputLabel, inputArea,
                expectedLabel, expectedArea,
                controls
        );
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 450, 500));
        popup.initOwner(owner);
        popup.show();
    }

    // ========================================================================
    // HANDLE UPLOAD FILE FLOW
    // ========================================================================
    private void handleUploadFileCase(Stage owner) {

        Stage popup = new Stage();
        popup.setTitle("Upload Test Case File");

        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(popup);

        if (file == null) {
            popup.close();
            return;
        }

        String fullName = file.getName();
        String baseName = (fullName.lastIndexOf('.') > 0)
                ? fullName.substring(0, fullName.lastIndexOf('.'))
                : fullName;

        Label title = new Label("Title: " + baseName);
        title.setStyle("-fx-font-weight: bold;");

        Label fileLabel = new Label("📄 " + fullName);

        Button done = new Button("Done");
        done.setOnAction(e -> {
            String fileContent = "";
            try {
                fileContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            } catch (Exception ignore) {}

            coordinator.createTestCase(baseName, fileContent, "");

            popup.close();
            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + baseName + "\" created from file!").showAndWait();

            showListOfTestCaseScreen(owner);
        });

        VBox root = new VBox(15, new Label("Test Case created from file:"), title, fileLabel, done);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 350, 250));
        popup.initOwner(owner);
        popup.show();
    }

    // ========================================================================
    // LIST OF TEST CASES SCREEN (backend driven)
    // ========================================================================
    private void showListOfTestCaseScreen(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("ListOfTestCase");

        Label header = new Label("List of Test Cases");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> list = new ListView<>();

        for (TestCase tc : coordinator.testcaseList.getAllTestCases()) {
            list.getItems().add(tc.getTitle());
        }

        Button add = new Button("Add to Test Suite");
        add.setOnAction(e -> showAddToSuitePopup(stage));

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, list, add, back);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 400, 450));
        stage.initOwner(owner);
        stage.show();
    }

    // ========================================================================
    // ADD TEST CASE TO SUITE (backend integrated)
    // ========================================================================
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
            String tcName = tcField.getText().trim();

            TestSuite suite = coordinator.getCurrentSuite();

            if (suite == null || !suite.getTitle().equals(suiteName)) {
                new Alert(Alert.AlertType.ERROR,
                        "Incorrect Test Suite name").show();
                return;
            }

            // Check testcase exists
            TestCase tc = coordinator.testcaseList.searchByTitle(tcName);
            if (tc == null) {
                new Alert(Alert.AlertType.ERROR,
                        "Test Case name not found").show();
                return;
            }

            coordinator.addTestCaseToSuite(tcName);

            popup.close();
            new Alert(Alert.AlertType.INFORMATION,
                    "Test Case \"" + tcName + "\" added successfully!").showAndWait();

            showTestSuiteWithCases(owner);
        });

        VBox root = new VBox(12, suiteLabel, suiteField, tcLabel, tcField, done);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        popup.setScene(new Scene(root, 350, 300));
        popup.initOwner(owner);
        popup.show();
    }

    // ========================================================================
    // SHOW SUITE CONTENT (REAL backend testcases)
    // ========================================================================
    private void showTestSuiteWithCases(Stage owner) {

        TestSuite suite = coordinator.getCurrentSuite();

        Stage stage = new Stage();
        stage.setTitle("Test Suite Content");

        Label header = new Label("Test Suite: " + suite.getTitle());
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TreeItem<String> rootItem = new TreeItem<>(suite.getTitle());
        rootItem.setExpanded(true);

        // REAL BACKEND TEST CASES
        for (TestCase tc : suite.getTestCases()) {
            rootItem.getChildren().add(new TreeItem<>(tc.getTitle()));
        }

        TreeView<String> tree = new TreeView<>(rootItem);

        Button back = new Button("Back");
        back.setOnAction(e -> stage.close());

        VBox root = new VBox(15, header, tree, back);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 350, 450));
        stage.initOwner(owner);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
