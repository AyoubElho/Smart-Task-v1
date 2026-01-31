package com.example.smarttask_frontend.tasks.controller;

import com.example.smarttask_frontend.auth.service.UserService;
import com.example.smarttask_frontend.tasks.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.smarttask_frontend.tasks.service.NotificationService;
import com.example.smarttask_frontend.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.smarttask_frontend.entity.User;


public class ShareTaskController {

    @FXML
    private Label taskTitleLabel;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    private Long taskId;
    private String taskTitle;


    private final UserService userService = new UserService();
    private final TaskService taskService = new TaskService();
    private final NotificationService notificationService = new NotificationService();


    private final ObservableList<User> users = FXCollections.observableArrayList();

    public void setTaskId(Long taskId, String taskTitle) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;

        taskTitleLabel.setText("Task: " + taskTitle);

        setupUserTable();
        loadUsers();
    }

    private void loadUsers() {
        User connectedUser = UserSession.getUser();

        users.clear();

        userService.getUsers().stream()
                .filter(u -> !u.getId().equals(connectedUser.getId()))
                .forEach(users::add);
    }

    private void setupUserTable() {
        usernameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername())
        );
        
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button shareButton = new Button("Share");
        
            {
                shareButton.setOnAction(event -> {
                    User selectedUser = getTableView().getItems().get(getIndex());
                    boolean success = taskService.shareTaskWithUser(taskId, selectedUser.getId(), taskTitle);
                    if (success) {
                        showInfo("Task shared with " + selectedUser.getUsername());
                    } else {
                        showError("Failed to share task with " + selectedUser.getUsername());
                    }
                });
            }
        
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : shareButton);
            }
        });

        userTable.setItems(users);
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) taskTitleLabel.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
