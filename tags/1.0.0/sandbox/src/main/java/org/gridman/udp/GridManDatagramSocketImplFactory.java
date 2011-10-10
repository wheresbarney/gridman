package org.gridman.udp;

import java.lang.reflect.Constructor;
import java.net.DatagramSocketImpl;
import java.net.DatagramSocketImplFactory;

/**
 * @author Jonathan Knight
 */
public class GridManDatagramSocketImplFactory implements DatagramSocketImplFactory {

    private Constructor<DatagramSocketImpl> socketConstructor;

    public GridManDatagramSocketImplFactory() {
        this("java.net.PlainDatagramSocketImpl");
    }

    @SuppressWarnings({"unchecked"})
    public GridManDatagramSocketImplFactory(String socketClassname) {
        try {
            Class<DatagramSocketImpl> socketClass = (Class<DatagramSocketImpl>) Class.forName(socketClassname);
            socketConstructor = socketClass.getDeclaredConstructor();
            socketConstructor.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DatagramSocketImpl createDatagramSocketImpl() {
        try {
            return socketConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
