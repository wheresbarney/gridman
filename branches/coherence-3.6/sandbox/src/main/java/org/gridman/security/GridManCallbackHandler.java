package org.gridman.security;

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public class GridManCallbackHandler implements CallbackHandler {
    public static final String PROP_USERNAME = "gridman.security.username";
    public static final String PROP_PASSWORD = "gridman.security.password";
    
    private String principal;

    private String password;

    public GridManCallbackHandler() {
    }

    public GridManCallbackHandler(String principal, String password) {
        this.principal = principal;
        this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                NameCallback nc = (NameCallback) callback;
                if (this.principal != null) {
                    nc.setName(principal);
                } else {
                    nc.setName(System.getProperty(PROP_USERNAME));
                }
            } else if (callback instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callback;
                String pwd = this.password;
                if (pwd == null) {
                    pwd = System.getProperty(PROP_PASSWORD);
                }
                pc.setPassword(pwd.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
            }
        }
    }
}
