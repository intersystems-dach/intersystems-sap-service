package ASPB.pex;

import java.util.ArrayList;
import java.util.List;

import com.intersystems.enslib.pex.Message;

import ASPB.tests.TestServer;
import ASPB.utils.Callback;
import ASPB.utils.Server;

public class MyInboundAdapter extends com.intersystems.enslib.pex.InboundAdapter implements Callback<String> {

    private List<String> buffer = new ArrayList<String>();

    public String test;

    private Server server;

    @Override
    public void OnInit() throws Exception {
        LOGINFO("OnInit called");
        server = new TestServer();
        server.registerCallback(this);
        server.start();

    }

    @Override
    public void OnTask() throws Exception {
        LOGINFO("Size: " + buffer.size());
        if (buffer.size() == 0) {
            MyMessage msg = new MyMessage();
            msg.value = "NoMessage";
            msg.more = false;
            BusinessHost.ProcessInput((Message) msg);
            return;
        }
        LOGINFO("OnTask called");
        MyMessage msg = new MyMessage();
        msg.value = "Message: " + buffer.get(0);
        buffer.remove(0);
        if (buffer.size() == 0)
            msg.more = false;
        else
            msg.more = true;
        BusinessHost.ProcessInput((Message) msg);
    }

    @Override
    public void OnTearDown() throws Exception {
        LOGINFO("OnTearDown called");
        server.stop();
    }

    @Override
    public boolean call(String arg0) {
        buffer.add((String) arg0);
        return true;
    }

}