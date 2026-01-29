package com.example.smarttask_frontend.dashboard;

import com.example.smarttask_frontend.entity.Task;
import com.example.smarttask_frontend.entity.User;
import com.example.smarttask_frontend.tasks.service.TaskService;
import com.example.smarttask_frontend.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label dashboardLabel;
    @FXML
    private VBox contentArea;


    // Linked to the new IDs in FXML for stats
    @FXML
    private Label totalTasksLabel;
    @FXML
    private Label inProgressLabel;
    @FXML
    private Label completedLabel;

    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> titleColumn;
    @FXML
    private TableColumn<Task, String> statusColumn;
    @FXML
    private TableColumn<Task, String> priorityColumn;
    @FXML
    private TableColumn<Task, String> dueDateColumn;
    // @FXML
    // private TableColumn<Task, String> ownerColumn;

    @FXML
    private TableColumn<Task, String> sharedTitleColumn;
    @FXML
    private TableColumn<Task, String> sharedPriorityColumn;
    @FXML
    private TableColumn<Task, String> sharedDueDateColumn;
    @FXML
    private TableColumn<Task, String> sharedStatusColumn;

    @FXML
    private TableView<Task> sharedTasksTable;

    private final TaskService taskService = new TaskService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        sharedTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        sharedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        sharedDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        sharedStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // === STYLE UPGRADE: Custom Cell Factory for Status Badges ===
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Create a badge label
                    Label badge = new Label(status);
                    badge.getStyleClass().add("status-badge");

                    // Apply CSS class based on status text (Case Insensitive)
                    switch (status.toUpperCase()) {
                        case "COMPLETED":
                        case "DONE":
                            badge.getStyleClass().add("status-done");
                            break;
                        case "IN PROGRESS":
                        case "DOING":
                            badge.getStyleClass().add("status-progress");
                            break;
                        default:
                            badge.getStyleClass().add("status-todo");
                            break;
                    }

                    // Wrap in HBox to handle alignment nicely
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });
    }

    @FXML
    private void logout() {
        try {
            // 1️⃣ Clear session
            UserSession.clear();
            for (Window window : List.copyOf(Window.getWindows())) {
                window.hide();
            }

            // 3️⃣ Open Login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showCalendar() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/CalendarView.fxml")
            );
            Parent root = loader.load();

            Stage calendarStage = new Stage();
            calendarStage.setTitle("Task Calendar");
            calendarStage.setScene(new Scene(root));

            // Optional but recommended
            calendarStage.initOwner(dashboardLabel.getScene().getWindow());
            calendarStage.setResizable(true);

            calendarStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboardData() {
        try {
            User user = UserSession.getUser();
            if (user == null) {
                dashboardLabel.setText("Session Expired");
                return;
            }

            dashboardLabel.setText("Overview");

            // Fetch Data
            List<Task> tasks = taskService.getTasksByUser(user.getId());
            ObservableList<Task> observableTasks = FXCollections.observableArrayList(tasks);
            taskTable.setItems(observableTasks);

            List<Task> sharedTasks = taskService.getSharedTasks(user.getId());
            sharedTasksTable.setItems(FXCollections.observableArrayList(sharedTasks));

            // Calculate Stats Dynamically
            updateStatistics(tasks);

        } catch (Exception e) {
            e.printStackTrace();
            // Optional: Show alert to user
        }
    }


    private void updateStatistics(List<Task> tasks) {
        int total = tasks.size();
        // Assuming status strings match these checks. Adjust to your specific Enum/Strings.
        long inProgress = tasks.stream().filter(t -> "IN PROGRESS".equalsIgnoreCase(t.getStatus()) || "DOING".equalsIgnoreCase(t.getStatus())).count();
        long completed = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()) || "DONE".equalsIgnoreCase(t.getStatus())).count();

        // Update Labels
        totalTasksLabel.setText(String.valueOf(total));
        inProgressLabel.setText(String.valueOf(inProgress));
        completedLabel.setText(String.valueOf(completed));
    }

    public void mytasks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MyTasks.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("My Tasks");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}