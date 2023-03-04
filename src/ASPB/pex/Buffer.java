package ASPB.pex;

import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private List<String> buffer;

    public Buffer() {
        buffer = new ArrayList<String>();
    }

    public void add(String s) {
        // lock einrichten
        buffer.add(s);
        // lock entfernen
    }

    public String get() {
        if (buffer.size() == 0)
            return null;

        String s = buffer.get(0);
        buffer.remove(0);
        return s;
    }

    public int size() {
        return buffer.size();
    }

}
