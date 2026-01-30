package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.LocalDateTimeAdapter;
import model.Status;
import model.Task;
import model.UpdateDueDateRequest;
import service.TaskService;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/tasks/*")
public class TaskController extends HttpServlet {

    private final TaskService taskService = new TaskService();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();

    // ========================= GET =========================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo(); // may be null

        if (path == null || path.equals("/")) {
            write(resp, taskService.getTasks());
            return;
        }

        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        // /test
        if (path.equals("/test")) {
            write(resp, "OK");
            return;
        }

        // /user/{userId}
        if (path.matches("/user/\\d+")) {
            Long userId = Long.parseLong(path.split("/")[2]);
            write(resp, taskService.getTasksByUser(userId));
            return;
        }

        // /shared/{userId}
        if (path.matches("/shared/\\d+")) {
            Long userId = Long.parseLong(path.split("/")[2]);
            write(resp, taskService.getTasksSharedWithUser(userId));
            return;
        }

        // /{taskId}
        if (path.matches("/\\d+")) {
            Long taskId = Long.parseLong(path.substring(1));
            write(resp, taskService.findById(taskId));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        write(resp, "Endpoint not found");
    }

    // ========================= POST =========================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(resp, "Invalid path");
            return;
        }

        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        // /create-task/id/{userId}
        if (path.matches("/create-task/id/\\d+")) {
            String[] parts = path.split("/");
            Long userId = Long.parseLong(parts[3]);

            Task task = readBody(req, Task.class);
            Task saved = taskService.createTask(task, userId);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            write(resp, saved);
            return;
        }

        // /{taskId}/share/{userId}
        if (path.matches("/\\d+/share/\\d+")) {
            String[] parts = path.split("/");
            Long taskId = Long.parseLong(parts[1]);
            Long userId = Long.parseLong(parts[3]);

            taskService.shareTask(taskId, userId);
            write(resp, "Task shared successfully!");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        write(resp, "Endpoint not found");
    }

    // ========================= PUT =========================
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        // /{taskId}/status/{status}
        if (path.matches("/\\d+/status/\\w+")) {
            String[] parts = path.split("/");
            Long taskId = Long.parseLong(parts[1]);
            Status status = Status.valueOf(parts[3]);

            taskService.updateStatus(taskId, status);
            write(resp, "Status updated");
            return;
        }

        // /{taskId}/due-date
        if (path.matches("/\\d+/due-date")) {
            String[] parts = path.split("/");
            Long taskId = Long.parseLong(parts[1]);

            UpdateDueDateRequest body = readBody(req, UpdateDueDateRequest.class);
            taskService.updateDueDate(taskId, body.getDueDate());
            write(resp, "Due date updated");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        write(resp, "Endpoint not found");
    }

    // ========================= HELPERS =========================
    private <T> T readBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        return gson.fromJson(req.getReader(), clazz);
    }

    private void write(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(data));
    }
}
