package com.intersystems.dach.sap.handlers;

import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerTIDHandler;


public class JCoServerTIDHandlerImpl implements JCoServerTIDHandler {

    @Override
    public boolean checkTID(JCoServerContext serverCtx, String tid) {
        return true;  
    }

    @Override
    public void commit(JCoServerContext serverCtx, String tid) {

    }

    @Override
    public void confirmTID(JCoServerContext serverCtx, String tid) {

    }

    @Override
    public void rollback(JCoServerContext serverCtx, String tid) {

    }

}
