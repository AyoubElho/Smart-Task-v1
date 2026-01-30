package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import dao.NotificationDao;
import model.LocalDateTimeAdapter;
import model.Notification;
import service.NotificationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/notifications/*")
public class NotificationController extends HttpServlet {

    private Gson gson;
    private NotificationService notificationService;

    @Override
    public void init() {
        // Initialize Gson with LocalDateTime adapter
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        
        // Initialize NotificationService with NotificationDao
        notificationService = new NotificationService(new NotificationDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            Long userId = getUserId(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String path = req.getPathInfo();

            if (path == null || path.equals("/")) {
                List<Notification> list = notificationService.getUserNotifications(userId);
                write(resp, list);
                return;
            }

            // Handle /unread-count endpoint
            if (path.equals("/unread-count")) {
                int count = new NotificationDao().countUnread(userId);
                resp.getWriter().write(String.valueOf(count));
                return;
            }

            // Handle single notification by ID
            try {
                Long id = Long.parseLong(path.substring(1));
                Notification n = notificationService.getNotification(id, userId);

                if (n == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                write(resp, n);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid notification ID");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            Long userId = getUserId(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Notification payload = gson.fromJson(req.getReader(), Notification.class);
            if (payload == null || payload.getMessage() == null || payload.getMessage().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Message is required");
                return;
            }

            Notification created = notificationService.create(userId, payload.getMessage());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            write(resp, created);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            Long userId = getUserId(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String path = req.getPathInfo();
            if (path == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Handle mark as read endpoint
            if (path.endsWith("/read")) {
                String idStr = path.replace("/read", "").substring(1);
                if (idStr.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                try {
                    Long id = Long.parseLong(idStr);
                    if (!notificationService.markAsRead(id, userId)) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    private Long getUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }

    private void write(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(data));
    }
}