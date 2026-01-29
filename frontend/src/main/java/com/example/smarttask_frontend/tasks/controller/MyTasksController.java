package com.example.smarttask_frontend.tasks.controller;

import com.example.smarttask_frontend.entity.Task;
import com.example.smarttask_frontend.session.UserSession;
import com.example.smarttask_frontend.subtasks.controller.SubtaskController;
import com.example.smarttask_frontend.tasks.service.TaskService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MyTasksController implements Initializable {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> categoryColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, Void> subTasksColumn;
    @FXML private TableColumn<Task, Void> shareColumn;

    private final TaskService taskService = new TaskService();

    private final ObservableList<String> statusOptions =
            FXCollections.observableArrayList("TODO", "IN_PROGRESS", "DONE");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        setupSubtaskButtonColumn();
        setupShareButtonColumn();
        loadTasks();
        taskTable.setEditable(true);
    }

    // ========================= COLUMNS =========================
    private void setupColumns() {

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));

        categoryColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getCategoryName() != null
                                ? cell.getValue().getCategoryName()
                                : "General"
                )
        );

        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        statusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus())
        );

        statusColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(statusOptions)
        );

        statusColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            String newStatus = event.getNewValue();
            task.setStatus(newStatus);
            taskService.updateTaskStatus(task.getId(), newStatus);
        });
    }

// ========================= SUBTASK BUTTON =========================
    private void setupSubtaskButtonColumn() {
        subTasksColumn.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button("Show");

            {
                // LINK TO CSS: Add the specific class here
                button.getStyleClass().add("action-button");
                button.getStyleClass().add("subtask-btn");

                button.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    openSubtasksWindow(task.getId(), task.getTitle());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
                // Center the button in the cell
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
    }

    // ========================= SHARE BUTTON =========================
    private void setupShareButtonColumn() {
        shareColumn.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button("Share");

            {
                // LINK TO CSS: Add the specific class here
                button.getStyleClass().add("action-button");
                button.getStyleClass().add("share-btn");

                button.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    openShareTaskDialog(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
                // Center the button in the cell
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
    }

    // ========================= LOAD TASKS =========================
    private void loadTasks() {
        try {
            Long userId = UserSession.getUserId();
            List<Task> tasks = taskService.getTasksByUser(userId);
            taskTable.setItems(FXCollections.observableArrayList(tasks));
        } catch (Exception e) {
            showError("Failed to load tasks");
        }
    }

    // ========================= WINDOWS =========================
    private void openSubtasksWindow(Long taskId, String taskTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Subtasks.fxml"));
            Parent root = loader.load();

            SubtaskController controller = loader.getController();
            controller.setTaskId(taskId);

            Stage stage = new Stage();
            stage.setTitle("Subtasks - " + taskTitle);
            stage.setScene(new Scene(root, 800, 500));
            stage.show();

        } catch (IOException e) {
            showError("Unable to open Subtasks window");
        }
    }

    private void openShareTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ShareTaskView.fxml"));
            Parent root = loader.load();

            ShareTaskController controller = loader.getController();
            controller.setTaskId(task.getId(), task.getTitle());

            Stage stage = new Stage();
            stage.setTitle("Share Task");
            stage.setScene(new Scene(root, 350, 300));
            stage.show();

        } catch (IOException e) {
            showError("Unable to open Share dialog");
        }
    }

    @FXML
    private void createTask() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/CreateTaskView.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Create Task");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void aiGenerate() {
        // future AI logic
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
