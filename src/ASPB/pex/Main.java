package ASPB.pex;

import java.sql.SQLException;

import com.intersystems.enslib.pex.Director;

import ASPB.utils.Callback;

public class Main implements Callback<String> {
    public static void main(String[] args) {
        /*
         * TestServer.registerSerive(new Main());
         * TestServer.start();
         */ try {
            Director.CreateBusinessService(null, null);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public boolean call(String arg0) {
        System.out.println("Got here:" + arg0 + "!");
        return true;
    }
}
