package org.gridman.testtools.coherence.queries;

/**
 * @author Jonathan Knight
 */
public abstract class BaseQuery<T> implements ClusterQuery<T> {

    private Object[] parameters = new Object[0];

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getMethodName() {
        return "run";
    }

    @Override
    public Class<?>[] getParamTypes() {
        return new Class<?>[0];
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    public abstract T run();
}
