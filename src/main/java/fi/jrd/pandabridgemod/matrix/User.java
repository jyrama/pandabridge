package fi.jrd.pandabridgemod.matrix;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;

import fi.jrd.pandabridgemod.PandabridgeMod;

public class User {
    private Homeserver server;
    private static final Gson GSON = new Gson();

    private Profile profile;
    private String userId;
    private Header userAuthHeader;

    User(String userId, Homeserver server) {
        this.userId = userId;
        this.server = server;

        String profileUrl = String.format("%s/_matrix/client/r0/profile/%s", this.server.getAddress(), userId);

        try {
            Response res = Request.get(profileUrl).addHeader("Authorization", PandabridgeMod.authorization).execute();
            if (res.returnResponse().getCode() != 200) {
                PandabridgeMod.logger.error("Failed to construct User id {}: {}", userId,
                        res.returnResponse().getReasonPhrase());
            }

            this.profile = GSON.fromJson(res.returnContent().asString(), Profile.class);
        } catch (IOException e) {
            PandabridgeMod.logger.error("IO error while constructing user id {}: {}", userId, e);
        }
    }

    public String getDisplayName() {
        return this.profile.displayname;
    }

    public String getAvatarURL() {
        return this.profile.avatar_url;
    }

    public void setAvatarURL(URL url) {
        if(!url.getProtocol().equals("mxc")) {
            PandabridgeMod.logger.error("Setiing user avatar failed, protocol not MXC: {}", url.toString());
        }

        String endpoint = String.format("%s/_matrix/client/r0/profile/%s/avatar_url", this.server.getAddress(), this.userId);
        String payload = String.format("{\"avatar_url\": \"%s\"}", url.toString());

        try {
            Response res = Request.put(endpoint).addHeader(this.userAuthHeader).bodyString(payload, ContentType.APPLICATION_JSON).execute();
            if (res.returnResponse().getCode() != 200) {
                PandabridgeMod.logger.error("Setting avatar URL failed: {}", res.returnContent());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    class Profile {
        private String displayname;
        private String avatar_url;
    }

}
