package org.gridman.security;

import java.security.Principal;

/**
 * @author Jonathan Knight
 */
public class PrincipalStub implements Principal {

    private String name;

    public PrincipalStub(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
