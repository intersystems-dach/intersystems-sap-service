package ASPB.pex;

import ASPB.tests.TestServer;
import ASPB.utils.annotations.NotForRealUse;
import ASPB.utils.interfaces.Callback;
import ASPB.utils.interfaces.MyServer;

@NotForRealUse
public class MyService extends com.intersystems.enslib.pex.BusinessService implements Callback<String> {

    public String BusinessPartner;

    public String test;

    private MyServer server;

    @Override
    public void OnInit() throws Exception {
        // sap server starten

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

        /*
         * LOGINFO("Got here2:" + arg0 + "!");
         * 
         * if (buffer.size() == 0)
         * return null;
         * 
         * if (buffer.size() > 1)
         * this._WaitForNextCallInterval = false;
         * else
         * this._WaitForNextCallInterval = true;
         * 
         * String s = buffer.get(0);
         * buffer.remove(0);
         * 
         * MyMessage msg = new MyMessage();
         * msg.value = "Message: " + s;
         * return this.SendRequestAsync(this.BusinessPartner, msg);
         */
        return null;
    }

    @Override
    public void OnTearDown() throws Exception {
        // sap server stoppen
        server.stop();
        LOGINFO("MyService stopped");
    }

    @Override
    public boolean call(String arg0) {
        // LOGINFO("Got here1:" + arg0 + "!");
        // return OnProcessInput(arg0);
        /*
         * IRISBusinessService service =
         * Director.CreateBusinessService(this.irisHandle.getConnection(),
         * "myService");
         * return service.ProcessInput(arg0);
         */
        // buffer.add((String) arg0);

        MyMessage msg = new MyMessage();
        msg.value = "Message: " + (String) arg0;
        try {

            this.SendRequestAsync(BusinessPartner, msg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
