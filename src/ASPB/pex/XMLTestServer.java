package ASPB.pex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import ASPB.utils.Callback;
import ASPB.utils.Logger;
import ASPB.utils.Server;

public class XMLTestServer implements Server {

    private int timeout = 2000;
    private boolean running = false;
    private Callback<String> callback;

    public String xml1 = "";
    public String xml2 = "";

    public XMLTestServer() {
        running = false;
        timeout = 2000;
        callback = null;
    }

    @Override
    public boolean start() {
        if (running || callback == null)
            return false;

        try {
            readXML();
        } catch (FileNotFoundException e) {
            Logger.error("Could not read XML files: " + e.getMessage());
            return false;
        }

        running = true;
        Thread t = new Thread(thread);
        t.start();
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        return true;
    }

    @Override
    public void registerCallback(Callback<?> callback) {
        this.callback = (Callback<String>) callback;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setProperty(String key, String value) {
        if (key.toLowerCase().equals("timeout"))
            timeout = Integer.parseInt(value);
    }

    private String generateMessage() {
        int random = (int) (Math.random() * 2);
        String s = "";
        if (random == 0) {
            s = xml1;
        } else {
            s = xml2;
        }
        return s;
    }

    private void readXML() throws FileNotFoundException {
        File file = new File("C:\\Users\\pbonin\\Desktop\\SAPService\\xml\\Z_ISH_CASE_VERSAND_INCL_PAT_sample.xml");
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line != null)
                xml1 += line;
        }
        scanner.close();

        file = new File("C:\\Users\\pbonin\\Desktop\\SAPService\\xml\\Z_ISH_PATIENT_VERSAND_sample.xml");
        scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line != null)

                xml2 += line;
        }
        scanner.close();

    }

    private Runnable thread = new Runnable() {

        @Override
        public void run() {
            while (running) {
                try {
                    String message = generateMessage();
                    callback.call(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(timeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
