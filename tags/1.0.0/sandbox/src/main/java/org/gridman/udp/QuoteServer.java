package org.gridman.udp;

/**
 * @author Jonathan Knight
 */
public class QuoteServer {

    public static void main(String[] args) throws Exception {
        new QuoteServerThread().run();
    }
    
}
