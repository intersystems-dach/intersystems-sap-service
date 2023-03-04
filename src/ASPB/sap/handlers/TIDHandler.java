package ASPB.sap.handlers;

import java.util.Hashtable;
import java.util.Map;

import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerTIDHandler;

public class TIDHandler implements JCoServerTIDHandler {

    Map<String, TIDState> availableTIDs = new Hashtable<String, TIDState>();

    @Override
    public boolean checkTID(JCoServerContext serverCtx, String tid) {
        // This example uses a Hashtable to store status information. Normally, however,
        // you would use a database. If the DB is down throw a RuntimeException at
        // this point. JCo will then abort the tRFC and the R/3 backend will try
        // again later.

        TIDState state = availableTIDs.get(tid);
        if (state == null) {
            availableTIDs.put(tid, TIDState.CREATED);
            return true;
        }

        if (state == TIDState.CREATED || state == TIDState.ROLLED_BACK)
            return true;

        return false;
        // "true" means that JCo will now execute the transaction, "false" means
        // that we have already executed this transaction previously, so JCo will
        // skip the handleRequest() step and will immediately return an OK code to R/3.
    }

    @Override
    public void commit(JCoServerContext serverCtx, String tid) {
        // react on commit, e.g. commit on the database;
        // if necessary throw a RuntimeException, if the commit was not possible
        availableTIDs.put(tid, TIDState.COMMITTED);
    }

    @Override
    public void confirmTID(JCoServerContext serverCtx, String tid) {
        try {
            // clean up the resources
        }
        // catch(Throwable t) {} //partner won't react on an exception at
        // this point
        finally {
            availableTIDs.remove(tid);
        }
    }

    @Override
    public void rollback(JCoServerContext serverCtx, String tid) {
        availableTIDs.put(tid, TIDState.ROLLED_BACK);

        // react on rollback, e.g. rollback on the database
    }

    public void execute(JCoServerContext serverCtx) {
        String tid = serverCtx.getTID();
        if (tid != null) {
            availableTIDs.put(tid, TIDState.EXECUTED);
        }
    }

    private enum TIDState {
        CREATED, EXECUTED, COMMITTED, ROLLED_BACK, CONFIRMED;
    }
}
