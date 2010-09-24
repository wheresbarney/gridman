package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import org.gridman.classloader.SystemPropertyLoader;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.util.NullInvokable;

import javax.security.auth.Subject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivilegedAction;

/**
 * Gui created for demo
 * @author Andrew Wilson
 */
public class SecurityGUI implements ActionListener {

    private JTextField userField;
    private JTextField roleField;
    private JTextField resourceField;
    private JComboBox permissionBox;
    private JComboBox actionBox;
    private JTextField messageField;

    private Object[] PERMISSIONS = {"Read", "Write", "Admin", "Invoke"};
    private Object[] ACTIONS = {"Add", "Remove", "Check"};

    public static void main(String[] args) {
        new SecurityGUI();
    }
    
    private SecurityGUI() {

        // Start the cluster.
        // ClusterStarter.getInstance().ensureCluster("/coherence/security/demo/securityDemoCluster.properties");

        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoDefault.properties");
        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoClient.properties");

        JPanel panel = new JPanel(new GridLayout(8,2,5,5));

        // User
        panel.add(new JLabel("User"));
        userField = new JTextField(20);
        panel.add(userField);

        // Role
        panel.add(new JLabel("Role"));
        roleField = new JTextField(20);
        panel.add(roleField);

        // Resource
        panel.add(new JLabel("Resource"));
        resourceField = new JTextField(20);
        panel.add(resourceField);

        // Permission
        panel.add(new JLabel("Permission"));
        permissionBox = new JComboBox(PERMISSIONS);
        panel.add(permissionBox);

        // Action (Add, Remove, Check)
        panel.add(new JLabel("Action"));
        actionBox = new JComboBox(ACTIONS);
        panel.add(actionBox);

        // Execute
        panel.add(new JLabel("Execute"));
        JButton button = new JButton("Hit Me!");
        button.addActionListener(this);
        panel.add(button);

        // Message
        panel.add(new JLabel("Message"));
        messageField = new JTextField(20);
        panel.add(messageField);        

        JFrame frame = new JFrame("Security GUI");
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

    }

    @Override public void actionPerformed(ActionEvent e) {
        try {
            String action = (String)actionBox.getSelectedItem();
            int offset = permissionBox.getSelectedIndex();
            final Object permissionCommand = permissionBox.getSelectedItem();
            final SecurityPermission permission = new SecurityPermission(roleField.getText(), resourceField.getText(), offset);
            System.out.println("Permission " + permission);
            PrivilegedAction<Object> pAction;
            if(action.equals("Add")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        System.out.println("Add!!!");
                        return CacheFactory.getCache(SecurityPermission.PERMISSION_CACHE).put(permission, permission);
                    }
                };
            } else if(action.equals("Remove")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        System.out.println("Remove!!!");
                        return CacheFactory.getCache(SecurityPermission.PERMISSION_CACHE).remove(permission);
                    }
                };
            } else if(action.equals("Check")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        System.out.println("Check!!!");
                        NamedCache cache = CacheFactory.getCache(resourceField.getText());
                        if(permissionCommand.equals("Read")) {
                            cache.get(1);
                        } else if(permissionCommand.equals("Write")) {
                            cache.put(1,"A");
                        } else if(permissionCommand.equals("Admin")) {
                            cache.destroy();
                        } else if(permissionCommand.equals("Invoke")) {
                            ((InvocationService)CacheFactory.getService(SecurityPermission.INVOKE_SERVICE)).query(new NullInvokable(),null);
                        } else {
                            throw new RuntimeException("Invalid action : " + permissionCommand);
                        }
                        return null;
                    }
                };
            } else {
                throw new Exception("Invalid action : " + action);
            }
            messageField.setText("OK");
            System.out.println("Doing it..." + userField.getText());
            Subject.doAs(CoherenceUtils.getSimpleSubject(userField.getText()), pAction);            
        } catch(Throwable t) {
            t.printStackTrace();
            messageField.setText(t.toString());
        }
    }
}
