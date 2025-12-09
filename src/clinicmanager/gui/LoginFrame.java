package clinicmanager.gui;

import clinicmanager.dao.UserDAO;
import clinicmanager.database.DatabaseConnection;
import clinicmanager.models.User;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private UserDAO userDAO;

    public LoginFrame() {
        // init db
        try {
            Connection connection = DatabaseConnection.getConnection();
            userDAO = new UserDAO(connection);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Database connection error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setupUI();
    }

    private void setupUI() {
        setTitle("Clinic Manager - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // main panel stuff
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // title at top
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Clinic Manager System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);

        // form area
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // username stuff
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel usernameLabel = new JLabel("Username:");
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);

        // password stuff
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginButton = new JButton("Login");
        cancelButton = new JButton("Cancel");

        loginButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        // info label
        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("<html><center>Default login:<br>Username: admin<br>Password: admin123</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);

        // add all panels
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // event stuff
        loginButton.addActionListener(e -> performLogin());
        cancelButton.addActionListener(e -> System.exit(0));

        // enter key does login
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // focus on username at start
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // check inputs
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your username",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your password",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        // turn off button while checking
        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");

        try {
            // try to authenticate
            User user = userDAO.authenticateUser(username, password);

            if (user != null) {
                // it worked
                JOptionPane.showMessageDialog(this,
                    "Welcome, " + user.getFullName() + "!",
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                // open main screen
                SwingUtilities.invokeLater(() -> {
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                    dispose(); // close login
                });

            } else {
                // nope
                JOptionPane.showMessageDialog(this,
                    "Invalid username or password.\nPlease try again.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // turn button back on
            loginButton.setEnabled(true);
            loginButton.setText("Login");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
