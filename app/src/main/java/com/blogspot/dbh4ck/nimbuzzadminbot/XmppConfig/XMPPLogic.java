package com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig;

import org.jivesoftware.smack.AbstractXMPPConnection;

/**
 * Created by DB on 14-09-2016.
 */

public class XMPPLogic {
    public static AbstractXMPPConnection connection;
    private static XMPPLogic ourInstance;

    static {
        connection = null;
        ourInstance = null;
    }

    public static synchronized XMPPLogic getInstance() {
        XMPPLogic xMPPLogic;
        synchronized (XMPPLogic.class) {
            if (ourInstance == null) {
                ourInstance = new XMPPLogic();
            }
            xMPPLogic = ourInstance;
        }
        return xMPPLogic;
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(AbstractXMPPConnection connection) {
        connection = connection;
    }
}
