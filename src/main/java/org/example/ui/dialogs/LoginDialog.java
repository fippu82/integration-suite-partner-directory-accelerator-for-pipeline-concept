package org.example.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static org.example.utils.SharedData.*;

public class LoginDialog extends JDialog {

    public LoginDialog(Frame parent ) {
        super(parent, "Login", true); // Modal dialog

        // Set up the layout and components
        setupDialog();

        // Center the dialog on the parent
        setLocationRelativeTo(parent);
    }

    private void setupDialog() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Create components
        JLabel passwordLabel = new JLabel("Please enter the password of your SAP User:");
        JPasswordField passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");

        // Add components to the dialog
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(loginButton, gbc);

        // Add action listener to the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = new String(passwordField.getPassword());

                // Validate input
                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginDialog.this, "Password required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Save the credentials to the global shared data to use later when creating TenantCredentials
                userPassword = password;
                // Close the dialog
                dispose();
            }
        });

        // Set dialog properties
        pack(); // Size the dialog to fit its components
    }
}