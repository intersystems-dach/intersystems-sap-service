package ASPB.pex;

import java.sql.SQLException;

import com.intersystems.enslib.pex.Director;

import ASPB.utils.Callback;
import ASPB.utils.Server;

public class Main implements Callback<String> {
    public static void main(String[] args) {
        XMLTestServer server = new XMLTestServer();
        server.registerCallback(new Main());
        server.start();
        // System.out.println(server.xml1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        server.stop();

    }

    @Override
    public boolean call(String arg0) {

        System.out.println(arg0);
        System.out.println();
        System.out.println();
        System.out.println();

        return true;
    }
}
