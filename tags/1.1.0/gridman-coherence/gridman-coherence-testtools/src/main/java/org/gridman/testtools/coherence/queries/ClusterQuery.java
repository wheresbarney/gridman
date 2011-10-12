package org.gridman.testtools.coherence.queries;

/**
 * @author Jonathan Knight
 */
public interface ClusterQuery<T> {

    String getClassName();

    String getMethodName();

    Class<?>[] getParamTypes();

    Object[] getParameters();

}
