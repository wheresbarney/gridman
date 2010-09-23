package org.gridman.coherence.security.demo.security;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrewwilson
 * Date: Sep 21, 2010
 * Time: 1:10:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityGUI {
    private SecurityGUI() {

        JPanel panel = new JPanel();

        // User
        panel.add(new JLabel("User"));
        JTextField userField = new JTextField(20);
        panel.add(userField);
        // Role
        panel.add(new JLabel("Role"));
        JTextField roleField = new JTextField(20);
        panel.add(roleField);

        // Resource
        panel.add(new JLabel("Resource"));
        JTextField resourceField = new JTextField(20);
        panel.add(resourceField);

        // Permission
        panel.add(new JLabel("Permission"));
        JComboBox permissionBox = new JComboBox(new Object[]{"Read", "Write", "Admin", "Invoke"});
        panel.add(permissionBox);

        // Action (Add, Remove, Check)
        panel.add(new JLabel("Action"));
        JComboBox actionBox = new JComboBox(new Object[]{"Add", "Remove", "Check"});
        panel.add(actionBox);

        JFrame frame = new JFrame("Security GUI");
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        new SecurityGUI();
    }
}
