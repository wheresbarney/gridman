package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import org.apache.log4j.Logger;
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
public class DemoSecurityGUI implements ActionListener {
    private static final Logger logger = Logger.getLogger(DemoSecurityGUI.class);

    private JTextField userField;
    private JTextField roleField;
    private JTextField resourceField;
    private JComboBox permissionBox;
    private JComboBox actionBox;
    private JFrame frame;

    private Object[] PERMISSIONS = {"Read", "Write", "Invoke"};
    private Object[] ACTIONS = {"Add", "Remove", "Check"};

    public static void main(String[] args) {
        new DemoSecurityGUI();
    }
    
    private DemoSecurityGUI() {

        // Start the cluster.
        // ClusterStarter.getInstance().ensureCluster("/coherence/security/demo/securityDemoCluster.properties");

        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoDefault.properties");
        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoClient.properties");

        JPanel panel = new JPanel(new GridLayout(6,2,5,5));

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

        frame = new JFrame("Security GUI");
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

    }

    @Override public void actionPerformed(ActionEvent e) {
        try {
            String action = (String)actionBox.getSelectedItem();
            final String permissionCommand = (String)permissionBox.getSelectedItem();
            final DemoSecurityPermission permission = new DemoSecurityPermission(   roleField.getText(),
                                                                            resourceField.getText(),
                                                                            !permissionCommand.equals("Invoke"),
                                                                            !permissionCommand.equals("Write"));
            logger.debug("Permission " + permission);
            PrivilegedAction<Object> pAction;
            if(action.equals("Add")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        return CacheFactory.getCache(DemoServer.PERMISSION_CACHE).put(permission, permission);
                    }
                };
            } else if(action.equals("Remove")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        Object value = CacheFactory.getCache(DemoServer.PERMISSION_CACHE).remove(permission);
                        if(value==null) { throw new RuntimeException("Nothing to remove!"); }
                        return null;
                    }
                };
            } else if(action.equals("Check")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        if(permissionCommand.equals("Read")) {
                            CacheFactory.getCache(resourceField.getText()).get(1);
                        } else if(permissionCommand.equals("Write")) {
                            CacheFactory.getCache(resourceField.getText()).put(1,"A");
                        } else if(permissionCommand.equals("Invoke")) {
                            ((InvocationService)CacheFactory.getService(DemoServer.CLIENT_INVOKE_SERVICE)).query(new NullInvokable(),null);
                        } else {
                            throw new RuntimeException("Invalid action : " + permissionCommand);
                        }
                        return null;
                    }
                };
            } else {
                throw new Exception("Invalid action : " + action);
            }
            logger.debug("Doing it..." + userField.getText());
            Subject.doAs(CoherenceUtils.getSimpleSubject(userField.getText()), pAction);
            JOptionPane.showMessageDialog(frame,"Rock on!","Success",JOptionPane.INFORMATION_MESSAGE);
        } catch(Throwable t) {
            JOptionPane.showMessageDialog(frame,t.toString(),"Failed",JOptionPane.ERROR_MESSAGE);
            t.printStackTrace();
        }
    }
}
