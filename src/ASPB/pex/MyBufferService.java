package ASPB.pex;

import com.intersystems.gateway.GatewayContext;
import com.intersystems.jdbc.IRIS;
import com.intersystems.jdbc.IRISObject;

import ASPB.tests.TestServer;
import ASPB.utils.Buffer;
import ASPB.utils.annotations.NotForRealUse;
import ASPB.utils.interfaces.Callback;
import ASPB.utils.interfaces.MyServer;

@NotForRealUse
public class MyBufferService extends com.intersystems.enslib.pex.BusinessService implements Callback<String> {

    private Buffer<String> buffer = new Buffer<String>(-1);

    private MyServer server;

    private IRIS iris;

    public String BusinessPartner;

    public int burstSize;

    public int maxBufferSize;

    public int timeout = 2000;

    @Override
    public void OnInit() throws Exception {
        // sap server starten
        iris = GatewayContext.getIRIS();
        server = new TestServer();
        server.registerCallback(this);
        server.start();
        LOGINFO("MyService started");
    }

    @Override
    public Object OnProcessInput(Object arg0) throws Exception {
        // wird von handle request aufgerufen und schickt nachricht weiter

        // nachrichten in buffer und dann über for loop einzelen senden
        // burst mit zb 10 elementen

        // buffer max size
        int max = buffer.size();
        if (max > burstSize && burstSize > 0)
            max = burstSize;

        boolean result = true;

        for (int i = 0; i < max; i++) {
            String s = buffer.poll();
            /*
             * MyMessage msg = new MyMessage();
             * msg.value = "Message: " + s;
             */
            IRISObject request = (IRISObject) iris.classMethodObject("Ens.StringRequest", "%New", "Message: " + s);
            try {
                this.SendRequestAsync(this.BusinessPartner, request);
            } catch (Exception e) {
                result = false;
            }
        }

        return result;
    }

    @Override
    public void OnTearDown() throws Exception {
        // sap server stoppen
        server.stop();
        LOGINFO("MyService stopped");
    }

    @Override
    public boolean call(String arg0) {
        buffer.add((String) arg0);
        return true;
    }

    @Override
    public String getAdapterType() {
        return "";
    }

}
