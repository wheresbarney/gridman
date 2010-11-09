package org.gridman.demo.coherence.security;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import org.apache.log4j.Logger;
import org.gridman.coherence.security.simple.CoherenceSecurityUtils;
import org.gridman.testtools.classloader.SystemPropertyLoader;

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
    private boolean isAdminGui;

    private Object[] PERMISSIONS = {"Read", "Write", "Invoke"};
    private Object[] ACTIONS = {"Add", "Remove", "Check"};

    public static void main(String[] args) {
        new DemoSecurityGUI();
    }
    
    private DemoSecurityGUI() {

        isAdminGui = Boolean.getBoolean("demo.isAdmin");

        // Start the cluster.
        // ClusterStarter.getInstance().ensureCluster("/coherence/security/demo/securityDemoCluster.properties");

        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoDefault.properties");
        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoClient.properties");

        JPanel panel = new JPanel(new GridLayout(isAdminGui ? 6 : 4,2,5,5));

        // User
        panel.add(new JLabel("User"));
        userField = new JTextField(20);
        panel.add(userField);

        // Resource
        panel.add(new JLabel("Resource"));
        resourceField = new JTextField(20);
        panel.add(resourceField);

        if(isAdminGui) {
            // Role
            panel.add(new JLabel("Role"));
            roleField = new JTextField(20);
            panel.add(roleField);

            // IsolatedAction (Add, Remove, Check)
            panel.add(new JLabel("Action"));
            actionBox = new JComboBox(ACTIONS);
            panel.add(actionBox);
        }

        // Permission
        panel.add(new JLabel("Permission"));
        permissionBox = new JComboBox(PERMISSIONS);
        panel.add(permissionBox);

        // Execute
        panel.add(new JLabel("Execute"));
        JButton button = new JButton("Hit Me!");
        button.addActionListener(this);
        panel.add(button);

        frame = new JFrame(isAdminGui ? "Admin GUI" : "Security GUI");
        frame.setIconImage(new ImageIcon("src/main/resources/gridman-small.jpg").getImage());
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    @Override public void actionPerformed(ActionEvent e) {
        try {
            String action = isAdminGui ? (String)actionBox.getSelectedItem() : "Check";
            final String permissionCommand = (String)permissionBox.getSelectedItem();
            PrivilegedAction<Object> pAction;
            if(action.equals("Add")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        DemoSecurityPermission permission = new DemoSecurityPermission(   roleField.getText(),
                                                                            resourceField.getText(),
                                                                            !permissionCommand.equals("Invoke"),
                                                                            !permissionCommand.equals("Write"));
                        return CacheFactory.getCache(DemoSecurityProvider.PERMISSION_CACHE).put(permission, permission);
                    }
                };
            } else if(action.equals("Remove")) {
                pAction = new PrivilegedAction<Object>() {
                    @Override public Object run() {
                        DemoSecurityPermission permission = new DemoSecurityPermission(   roleField.getText(),
                                                                            resourceField.getText(),
                                                                            !permissionCommand.equals("Invoke"),
                                                                            !permissionCommand.equals("Write"));
                        Object value = CacheFactory.getCache(DemoSecurityProvider.PERMISSION_CACHE).remove(permission);
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
                            try {
                                Invocable invocable = (Invocable) Class.forName(resourceField.getText()).newInstance();
                                ((InvocationService)CacheFactory.getService(DemoSecurityProvider.CLIENT_INVOKE_SERVICE)).query(invocable,null);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
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
            Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(userField.getText()), pAction);
            JOptionPane.showMessageDialog(frame,"Rock on!","Success",JOptionPane.INFORMATION_MESSAGE);
        } catch(Throwable t) {
            JOptionPane.showMessageDialog(frame,t.toString(),"Failed",JOptionPane.ERROR_MESSAGE);
            t.printStackTrace();
        }
    }
}
