import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskManager {
    private Connection conn;
    private int userId;

    public TaskManager(Connection conn, int userId) {
        this.conn = conn;
        this.userId = userId;
    }

    public void loadTasks(DefaultListModel<String> taskModel) {
        taskModel.clear();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tasks WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                taskModel.addElement(rs.getString("task"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTask(String task, DefaultListModel<String> taskModel) {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO tasks (user_id, task) VALUES (?, ?)");
            stmt.setInt(1, userId);
            stmt.setString(2, task);
            stmt.executeUpdate();
            taskModel.addElement(task);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editTask(String oldTask, String newTask) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE tasks SET task = ? WHERE user_id = ? AND task = ?");
            stmt.setString(1, newTask);
            stmt.setInt(2, userId);
            stmt.setString(3, oldTask);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(String task) {
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE user_id = ? AND task = ?");
            stmt.setInt(1, userId);
            stmt.setString(2, task);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markTaskCompleted(String task) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE tasks SET completed = true WHERE user_id = ? AND task = ?");
            stmt.setInt(1, userId);
            stmt.setString(2, task);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
