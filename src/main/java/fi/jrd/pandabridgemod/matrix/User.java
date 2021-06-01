package fi.jrd.pandabridgemod.matrix;

import java.io.IOException;

import com.google.gson.Gson;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;

import fi.jrd.pandabridgemod.PandabridgeMod;

public class User {
    private Homeserver server;
    private static final Gson GSON = new Gson();

    private Profile profile;

    User(String id, Homeserver server) {
        this.server = server;

        String profileUrl = String.format("%s/_matrix/client/r0/profile/%s", this.server.getAddress(), id);

        try {
            Response res = Request.get(profileUrl).addHeader("Authorization", PandabridgeMod.authorization).execute();
            if (res.returnResponse().getCode() != 200) {
                PandabridgeMod.logger.error("Failed to construct User id {}: {}", id,
                        res.returnResponse().getReasonPhrase());
            }

            this.profile = GSON.fromJson(res.returnContent().asString(), Profile.class);
        } catch (IOException e) {
            PandabridgeMod.logger.error("IO error while constructing user id {}: {}", id, e);
        }
    }

    public String getDisplayName() {
        return this.profile.displayname;
    }

    public String getAvatarURL() {
        return this.profile.avatar_url;
    }

    class Profile {
        private String displayname;
        private String avatar_url;
    }
}
