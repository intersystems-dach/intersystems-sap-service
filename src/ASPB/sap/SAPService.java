package ASPB.sap;

import com.intersystems.gateway.GatewayContext;
import com.intersystems.jdbc.IRIS;
import com.intersystems.jdbc.IRISObject;

import ASPB.pex.TestServer;
import ASPB.utils.Buffer;
import ASPB.utils.Callback;
import ASPB.utils.Logger;
import ASPB.utils.Server;

public class SAPService extends com.intersystems.enslib.pex.BusinessService implements Callback<String> {

    /**
     * Service configuration
     */
    public static boolean toJSON = false;

    public static int burstSize = 10;

    public static int maxBufferSize = 100;

    public static String logFilePath = "";

    public static String programmID;

    public static String gatewayHostAdress;

    public static String gatewayService;

    public static String gatewayUser;

    public static String businessPartner;

    /**
     * Attributes
     */

    private Buffer<String> buffer;

    private IRIS iris;

    private Server server;

    @Override
    public void OnInit() throws Exception {
        // get iris connection
        iris = GatewayContext.getIRIS();
        // set the log file path
        Logger.setFilePath(logFilePath);
        // initialize the buffer
        buffer = new Buffer<String>(maxBufferSize);
        // start the server

        server = new TestServer();

        server.registerCallback(this);
        if (server.start()) {
            Logger.log("SAPService started");
            LOGINFO("SAPService started");
        } else {
            Logger.log("SAPService could not be started");
            LOGERROR("SAPService could not be started");
        }
    }

    @Override
    public Object OnProcessInput(Object msg) throws Exception {
        // wird von handle request aufgerufen und schickt nachricht weiter

        // nachrichten in buffer und dann über for loop einzelen senden
        // burst mit zb 10 elementen

        // buffer max size
        int burst = buffer.size();
        if (burst > burstSize && burstSize > 0)
            burst = burstSize;

        boolean result = true;

        for (int i = 0; i < burst; i++) {
            String s = buffer.poll();
            IRISObject request = (IRISObject) iris.classMethodObject("Ens.StringRequest", "%New", s);
            try {
                this.SendRequestAsync(SAPService.businessPartner, request);
            } catch (Exception e) {
                result = false;
            }
        }

        return result;
    }

    @Override
    public void OnTearDown() throws Exception {
        // was passiert mit dem buffer wenn der service beendet wird???
        if (server.stop()) {
            Logger.log("SAPService stopped");
            LOGINFO("SAPService stopped");
        } else {
            Logger.log("SAPService could not be stopped");
            LOGERROR("SAPService could not be stopped");
        }
    }

    @Override
    public boolean call(String arg0) throws Exception {
        return buffer.add(arg0);
    }

}
