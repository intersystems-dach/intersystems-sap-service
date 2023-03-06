package ASPB.sap.handlers;

import java.util.Hashtable;
import java.util.Map;

import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerTIDHandler;

import ASPB.utils.Logger;

public class TIDHandler implements JCoServerTIDHandler {

    Map<String, TIDState> availableTIDs = new Hashtable<String, TIDState>();

    // order: check (if true run handle request), commit, confirm

    // TODO: implement a better TID Handler

    @Override
    public boolean checkTID(JCoServerContext serverCtx, String tid) {

        /*
         * Die Methode checkTID wird durch das SAP-System aufgerufen, wenn ein
         * RFC-Aufruf als tRFC-Aufruf an den Java-Server übergeben wird. Die
         * Methode erhält als Parameter den Serverkontext und die TransaktionsID, mit
         * der der Aufruf verarbeitet werden soll.
         * Die Methode muss sicherstellen, dass ein Aufruf mit einer übergebenen
         * Transaktions-ID nur einmal ausgeführt wird. Daher ist es enorm wichtig,
         * dass Transaktionsnummern nicht verloren gehen, sodass die Implementierung
         * der Methode meist im Zusammenhang mit dem Speichern der ID in einer Datenbank
         * oder einem anderen persistenten Medium realisiert wird. Neben dem reinen
         * Speichern der TID ist es notwendig, auch
         * den Status der Verarbeitung der Transaktions-ID abzulegen. Dadurch
         * können Sie später leicht ein Monitoring für die Transaktionsnummern
         * implementieren.
         * Der Kontrakt dieser Methode definiert, dass ein boolean-Parameter an
         * den Aufrufenden zurückgeliefert wird. Für den Fall, dass die übergebene
         * Transaktions-ID bereits einmal verarbeitet wurde, muss der Wert »false«
         * zurückgeliefert werden, sonst der Wert »true«.
         */

        // This example uses a Hashtable to store status information. Normally, however,
        // you would use a database. If the DB is down throw a RuntimeException at
        // this point. JCo will then abort the tRFC and the R/3 backend will try
        // again later.
        Logger.log("TID Handler: checkTID for " + tid);

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

        /*
         * commit und rollback
         * Die Methode commit wird aufgerufen, wenn alle RFC-Funktionen, die mit
         * derselben Transaktionsnummer ausgeführt wurden, erfolgreich abgearbeitet
         * wurden. Im Gegensatz dazu steht die Methode rollback. Sie wird
         * aufgerufen, wenn eine Transaktion fehlerhaft abgeschlossen wurde.
         */

        Logger.log("TID Handler: commit for " + tid);

        // react on commit, e.g. commit on the database;
        // if necessary throw a RuntimeException, if the commit was not possible
        availableTIDs.put(tid, TIDState.COMMITTED);
    }

    @Override
    public void confirmTID(JCoServerContext serverCtx, String tid) {

        /*
         * Mittels der Methode confirmTID wird bestätigt, dass eine Transaktion mit
         * einer spezifischen Transaktionsnummer erfolgreich ausgeführt werden
         * konnte. Die Methode führt dazu, dass die Transaktionsnummer entweder aus dem
         * Persistenzmedium entfernt oder zumindest mit einem entsprechenden Status
         * versehen wird.
         */

        Logger.log("TID Handler: confirmTID for " + tid);

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
        Logger.log("TID Handler: rollback for " + tid);

        availableTIDs.put(tid, TIDState.ROLLED_BACK);

        // react on rollback, e.g. rollback on the database
    }

    public void execute(JCoServerContext serverCtx) {
        String tid = serverCtx.getTID();
        if (tid != null) {
            Logger.log("TID Handler: execute for " + tid);
            availableTIDs.put(tid, TIDState.EXECUTED);
        }
    }

    private enum TIDState {
        CREATED, EXECUTED, COMMITTED, ROLLED_BACK, CONFIRMED;
    }
}
