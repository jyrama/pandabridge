package fi.jrd.pandabridgemod.matrix;

import com.google.gson.Gson;

public class Message {
    private static final Gson gson = new Gson();

    private String msgtype = "m.text";
    private String body;

    Message(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
