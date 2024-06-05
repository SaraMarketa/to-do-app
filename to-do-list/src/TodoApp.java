import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TodoApp {
    private static Connection conn;
    private static TaskManager taskManager;
    private static int userId;

    private static void connect() {
        // Database connection
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/todo_app", "root", "saraklea");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("To-Do List App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new CardLayout());

        // Login Panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField loginUsernameField = new JTextField(15);
        JPasswordField loginPasswordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.setPreferredSize(new Dimension(150, 30));
        registerButton.setPreferredSize(new Dimension(150, 30));

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(loginUsernameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(loginPasswordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(loginButton, gbc);
        gbc.gridy = 3;
        loginPanel.add(registerButton, gbc);

        frame.add(loginPanel, "Login");

        // Register Panel
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());

        JTextField registerUsernameField = new JTextField(15);
        JPasswordField registerPasswordField = new JPasswordField(15);
        JButton createAccountButton = new JButton("Create Account");
        JButton backButton = new JButton("Back");

        createAccountButton.setPreferredSize(new Dimension(150, 30));
        backButton.setPreferredSize(new Dimension(150, 30));

        gbc.gridx = 0;
        gbc.gridy = 0;
        registerPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        registerPanel.add(registerUsernameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        registerPanel.add(registerPasswordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(createAccountButton, gbc);
        gbc.gridy = 3;
        registerPanel.add(backButton, gbc);

        frame.add(registerPanel, "Register");

        // Task Panel
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BorderLayout());
        DefaultListModel<String> taskModel = new DefaultListModel<>();
        JList<String> taskList = new JList<>(taskModel);
        JTextField taskField = new JTextField(20);
        JButton addTaskButton = new JButton("Add Task");
        JButton editTaskButton = new JButton("Edit Task");
        JButton deleteTaskButton = new JButton("Delete Task");
        JButton markCompletedButton = new JButton("Mark Completed");
        JButton logoutButton = new JButton("Logout");

        // Create a panel for the buttons and set it to use a BoxLayout with vertical alignment
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addTaskButton);
        buttonPanel.add(editTaskButton);
        buttonPanel.add(deleteTaskButton);
        buttonPanel.add(markCompletedButton);

        taskPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        taskPanel.add(taskField, BorderLayout.NORTH);
        taskPanel.add(buttonPanel, BorderLayout.EAST);
        taskPanel.add(logoutButton, BorderLayout.PAGE_END);

        frame.add(taskPanel, "Tasks");

        // Show frame
        frame.setVisible(true);
        CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), "Login");

        // Event Listeners
        loginButton.addActionListener(e -> {
            String username = loginUsernameField.getText();
            String password = new String(loginPasswordField.getPassword());

            connect();
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ? AND password = ?");
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                    cl.show(frame.getContentPane(), "Tasks");
                    taskManager = new TaskManager(conn, userId);
                    taskManager.loadTasks(taskModel);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid login.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        registerButton.addActionListener(e -> cl.show(frame.getContentPane(), "Register"));

        backButton.addActionListener(e -> cl.show(frame.getContentPane(), "Login"));

        createAccountButton.addActionListener(e -> {
            String username = registerUsernameField.getText();
            String password = new String(registerPasswordField.getPassword());

            connect();
            try {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Account created.");
                cl.show(frame.getContentPane(), "Login");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        addTaskButton.addActionListener(e -> {
            String task = taskField.getText();
            taskManager.addTask(task, taskModel);
            taskField.setText("");
        });

        editTaskButton.addActionListener(e -> {
            String selectedTask = taskList.getSelectedValue();
            String newTask = JOptionPane.showInputDialog(frame, "Edit Task", selectedTask);
            if (newTask != null && !newTask.trim().isEmpty()) {
                taskManager.editTask(selectedTask, newTask);
                taskManager.loadTasks(taskModel);
            }
        });

        deleteTaskButton.addActionListener(e -> {
            String selectedTask = taskList.getSelectedValue();
            if (selectedTask != null) {
                taskManager.deleteTask(selectedTask);
                taskModel.removeElement(selectedTask);
            }
        });

        markCompletedButton.addActionListener(e -> {
            String selectedTask = taskList.getSelectedValue();
            if (selectedTask != null) {
                taskManager.markTaskCompleted(selectedTask);
                taskManager.loadTasks(taskModel);
            }
        });

        logoutButton.addActionListener(e -> {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            cl.show(frame.getContentPane(), "Login");
        });
    }
}