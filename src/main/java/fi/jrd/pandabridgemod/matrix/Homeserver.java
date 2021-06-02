package fi.jrd.pandabridgemod.matrix;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;

import fi.jrd.pandabridgemod.PandabridgeMod;

public class Homeserver {
    private static final Gson GSON = new Gson();

    private String address;

    public Homeserver(String address) {
        if (address == null) {
            throw new NullPointerException("Homeserver address was null!");
        }

        this.address = address;
    }

    public Room getRoom(String id) {
        return new Room(id, this);
    }

    public String getAddress() {
        return address;
    }

    public String uploadImage(BufferedImage img) {
        String url = String.format("%s/_matrix/media/r0/upload", this.address);
        InputStream imgInput = null;
        try {
            imgInput = (InputStream) ImageIO.createImageInputStream(img);
            Response httpRes = Request.post(url).bodyStream(imgInput).execute();
            if (httpRes.returnResponse().getCode() != 200) {
                PandabridgeMod.logger.error("Uploading image failed: {}", httpRes.returnResponse().getReasonPhrase());
            }
            ContentUriResponse res = GSON.fromJson(httpRes.returnContent().asString(), ContentUriResponse.class);
            return res.content_uri;
        } catch (IOException e) {
            PandabridgeMod.logger.error("IOException during image upload: {}", e);
        }

        return null;
    }

    class ContentUriResponse {
        private String content_uri;
    }
}
