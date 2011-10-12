package org.gridman.security;

import org.junit.Test;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jonathan Knight
 */
public class GridManCallbackHandlerTest {

    @Test
    public void shouldSetUserNameFromSystemProperty() throws Exception {
        System.setProperty(GridManCallbackHandler.PROP_USERNAME, "knightj");

        NameCallback callback = new NameCallback("Test:");

        GridManCallbackHandler handler = new GridManCallbackHandler();
        handler.handle(new Callback[]{callback});

        assertThat(callback.getName(), is("knightj"));
    }

    @Test
    public void shouldSetPasswordFromSystemProperty() throws Exception {
        System.setProperty(GridManCallbackHandler.PROP_PASSWORD, "Secret1");

        PasswordCallback callback = new PasswordCallback("Test:", false);

        GridManCallbackHandler handler = new GridManCallbackHandler();
        handler.handle(new Callback[]{callback});

        assertThat(callback.getPassword(), is("Secret1".toCharArray()));
    }

    @Test
    public void shouldSetUserNameFromConstructorArgument() throws Exception {
        System.clearProperty(GridManCallbackHandler.PROP_USERNAME);

        NameCallback callback = new NameCallback("Test:");

        GridManCallbackHandler handler = new GridManCallbackHandler("knightj", "Secret1");
        handler.handle(new Callback[]{callback});

        assertThat(callback.getName(), is("knightj"));
    }

    @Test
    public void shouldSetPasswordFromConstructorArgument() throws Exception {
        System.clearProperty(GridManCallbackHandler.PROP_PASSWORD);

        PasswordCallback callback = new PasswordCallback("Test:", false);

        GridManCallbackHandler handler = new GridManCallbackHandler("knightj", "Secret1");
        handler.handle(new Callback[]{callback});

        assertThat(callback.getPassword(), is("Secret1".toCharArray()));
    }
}
