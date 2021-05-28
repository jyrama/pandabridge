package fi.jrd.pandabridgemod.matrix;

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import fi.jrd.pandabridgemod.PandabridgeMod;

public class Room {
    private String id; // matrix internal id
    private Homeserver server;

    Room(String id, Homeserver server) {
        this.id = id;
        this.server = server;
    }

    public void sendMessage(String message) {

        long time = new Date().getTime();
        Message payload = new Message(message);
        String url = String.format("%s/_matrix/client/r0/rooms/%s/send/m.room.message/%d", this.server.getAddress(),
                this.id, time);

        StringEntity httpBody = new StringEntity(payload.toString(), ContentType.APPLICATION_JSON);

        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Authorization", PandabridgeMod.authorization);
        httpPut.setEntity(httpBody);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            try (CloseableHttpResponse response = httpclient.execute(httpPut)) {
                int status = response.getStatusLine().getStatusCode();
                if (status != 200) {
                    PandabridgeMod.logger.warn("Sending event to Matrix failed with code {}: {}", status,
                            response.getStatusLine().getReasonPhrase());
                }

                HttpEntity entity2 = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity2);
            }

        } catch (IOException e) {
            PandabridgeMod.logger.warn("IOException occurred when sending event to Matrix: ", e.getMessage());
        } // catch (InterruptedException e) {
          // logger.warn("Sending event to Matrix was interrupted: ", e.getMessage());
          // }

    }

}
