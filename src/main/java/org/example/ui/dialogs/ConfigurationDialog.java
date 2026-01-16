package org.example.ui.dialogs;

import org.example.exceptions.TenantNameNotUniqueException;
import org.example.ui.MainFrame;
import org.example.utils.TenantCredentials;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.example.utils.SharedData.*;

public class ConfigurationDialog extends JDialog {
    private final JTextField tenantNameField;
    private final JCheckBox criticalCheckBox;
    private final JTextField urlField;
    private final JTextField clientIdField;

    private TenantCredentials tenantValues;

    private final JButton saveButton;
    private final JButton cancelButton;
    private final JButton uploadButton;
    private final MainFrame mainFrame;
    private final String dialogTitle;

    public ConfigurationDialog(MainFrame parent, String dialogTitle) {
        super(parent, dialogTitle, true);
        this.dialogTitle = dialogTitle;
        mainFrame = parent;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UI_PADDING, UI_PADDING, UI_PADDING, UI_PADDING);

        tenantNameField = new JTextField(UI_TEXT_FIELD_COLUMNS);
        criticalCheckBox = new JCheckBox();
        urlField = new JTextField(UI_TEXT_FIELD_COLUMNS);
        clientIdField = new JTextField(UI_TEXT_FIELD_COLUMNS);

        cancelButton = new JButton(LABEL_CANCEL);
        saveButton = new JButton(LABEL_SAVE);
        saveButton.setPreferredSize(cancelButton.getPreferredSize());
        uploadButton = new JButton(LABEL_SELECT_LOCAL_JSON_FILE);

        addComponents(gbc);
        setupListeners();

        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    public ConfigurationDialog(MainFrame parent, String dialogTitle, TenantCredentials tenant) {
        this(parent, dialogTitle);
        setInputFieldValues(tenant);
    }

    private void addComponents(GridBagConstraints gbc) {
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tenant Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel(colonAsterisk(LABEL_TENANT_NAME)), gbc);
        gbc.gridx = 1;
        add(tenantNameField, gbc);

        // Critical Checkbox
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel(colon(LABEL_CRITICAL)), gbc);
        gbc.gridx = 1;
        add(criticalCheckBox, gbc);

        // URL
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel(colonAsterisk(LABEL_URL)), gbc);
        gbc.gridx = 1;
        add(urlField, gbc);

        // Client ID
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel(colonAsterisk(LABEL_CLIENT_ID)), gbc);
        gbc.gridx = 1;
        add(clientIdField, gbc);

        // Buttons
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelButtons.add(cancelButton);
        panelButtons.add(saveButton);
        add(panelButtons, gbc);

        // Upload Button
        gbc.gridx = 0;
        gbc.gridy = 7;
        add(new JLabel(colon(LABEL_ALTERNATIVE)), gbc);
        gbc.gridx = 1;
        add(uploadButton, gbc);
    }

    private void setupListeners() {
        cancelButton.addActionListener(e -> dispose());

        saveButton.addActionListener(e -> {
            if (areFieldsValid()) {
                TenantCredentials newTenant = new TenantCredentials(tenantNameField.getText().trim(), criticalCheckBox.isSelected(), urlField.getText().trim(), clientIdField.getText().trim(), null, null);

                try {
                    if (dialogTitle.equals(LABEL_EDIT_SELECTED_TENANT)) { // edit tenant
                        if (jsonFileHandler.isNameUniqueReplace(newTenant.getName(), tenantValues.getName())) {
                            jsonFileHandler.replaceTenant(tenantValues, newTenant);
                        } else {
                            throw new TenantNameNotUniqueException();
                        }
                    } else { // add tenant
                        if (jsonFileHandler.isNameUniqueAdd(newTenant.getName())) {
                            jsonFileHandler.addTenant(newTenant);
                        } else {
                            throw new TenantNameNotUniqueException();
                        }
                    }
                    dispose();

                    tenantValues = newTenant;
                    mainFrame.setSelectedTenant(tenantValues.getName());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), LABEL_ERROR, JOptionPane.ERROR_MESSAGE);
                } catch (TenantNameNotUniqueException ex) {
                    JOptionPane.showMessageDialog(mainFrame, LABEL_ERROR_TENANT_NAME_ALREADY_EXISTS, LABEL_ERROR, JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, LABEL_FILL_OUT_ALL_FIELDS, LABEL_ERROR, JOptionPane.ERROR_MESSAGE);
            }
        });

        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    JSONObject jsonObject = new JSONObject(content);

                    String url = jsonObject.getJSONObject(JSON_KEY_OAUTH).getString(JSON_KEY_URL);
                    String clientId = jsonObject.getJSONObject(JSON_KEY_OAUTH).getString(JSON_KEY_CLIENT_ID);

                    urlField.setText(url + PATH_TO_API);
                    clientIdField.setText(clientId);
                } catch (IOException ex) {
                    LOGGER.error(ex);
                    JOptionPane.showMessageDialog(this, LABEL_ERROR_READING_JSON_FILE, LABEL_ERROR, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public void setInputFieldValues(TenantCredentials tenant) {
        setInputFieldValues(tenant.getName(), tenant.isCritical(), tenant.getUrl(), tenant.getUserName());
        tenantValues = tenant;
    }

    public void setEmptyValues() {
        setInputFieldValues(null, false, null, null);
        tenantValues = null;
    }

    public void setInputFieldValues(String name, boolean isCritical, String url, String clientId) {
        tenantNameField.setText(name);
        criticalCheckBox.setSelected(isCritical);
        urlField.setText(url);
        clientIdField.setText(clientId);
    }

    private boolean areFieldsValid() {
        return !tenantNameField.getText().trim().isEmpty() &&
                !urlField.getText().trim().isEmpty() &&

                !clientIdField.getText().trim().isEmpty();
    }
}