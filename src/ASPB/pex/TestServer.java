package ASPB.pex;

import java.io.FileWriter;

import ASPB.utils.Callback;
import ASPB.utils.Server;

public class TestServer implements Server {

    private int timeout = 2000;
    private boolean running = false;
    private Callback<String> callback;

    public TestServer() {
        running = false;
        timeout = 2000;
        callback = null;
    }

    @Override
    public boolean start() {
        if (running || callback == null)
            return false;

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

    private int generateMessage() {
        int random = (int) (Math.random() * 100);
        return random;
    }

    private Runnable thread = new Runnable() {

        @Override
        public void run() {
            while (running) {
                try {
                    String message = "" + generateMessage();
                    write(message);
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

    private void write(String message) {
        message = "[" + new java.util.Date() + "] " + message;
        try (FileWriter fw = new FileWriter("C:\\Users\\pbonin\\Desktop\\logPEX.txt", true)) {
            // write a timestamp with the message
            fw.write(message + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
