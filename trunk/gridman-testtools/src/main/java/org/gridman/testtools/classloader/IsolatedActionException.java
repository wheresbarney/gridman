package org.gridman.testtools.classloader;

/**
 * @author Jonathan Knight
 */
public class IsolatedActionException extends Exception {

    private Exception exception;

    public IsolatedActionException(Exception exception) {
        super((Throwable)null);
        this.exception = exception;
    }

    /**
     * Returns the the cause of this exception (the exception thrown by
     * the isolated action that resulted in this <code>IsolatedActionException</code>).
     *
     * @return  the cause of this exception.
     */
    public Throwable getCause() {
        return exception;
    }

    public String toString() {
        String s = getClass().getName();
        return (exception != null) ? (s + ": " + exception.toString()) : s;
    }

}
