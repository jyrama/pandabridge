package fi.jrd.pandabridgemod;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.entity.player.EntityPlayer;

public class Skin {
    private BufferedImage skin;

    Skin(EntityPlayer player) {
        MinecraftSessionService sessionService = player.getServer().getMinecraftSessionService();
        Map<Type, MinecraftProfileTexture> textureMap = sessionService.getTextures(player.getGameProfile(), true);
        MinecraftProfileTexture skin = textureMap.get(Type.SKIN);

        BufferedImage buffImage = null;

        try {
            buffImage = ImageIO.read(new URL(skin.getUrl()));
        } catch (IOException e) {
            PandabridgeMod.logger.error(e);
        }

        if (buffImage != null) {
            this.skin = buffImage;
        }
    }

    public BufferedImage getHead() {
        return this.skin.getSubimage(8, 8, 8, 8);
    }

    class Cache {
        private File cacheDir;
        private File[] knownFiles;

        Cache(File cacheDir) {
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }

            if (!cacheDir.isDirectory()) {
                PandabridgeMod.logger.error("Skin cache directory does not point to a directory: {}",
                        cacheDir.getAbsolutePath());
            }

            this.cacheDir = cacheDir;
            knownFiles = cacheDir.listFiles();
        }

        public void insertImage(BufferedImage image, String id) {
            MessageDigest digest = null;
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try {
                digest = MessageDigest.getInstance("sha1");
                ImageIO.write(image, "png", buf);
                digest.update(buf.toByteArray());
            } catch (NoSuchAlgorithmException e1) {
                PandabridgeMod.logger.error("Your Java is bad, it's missing SHA1 provider!");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String hash = Base64.getEncoder().encodeToString(digest.digest());
            String filename = hash + "-" + id + ".png";

            try {
                File diskFile = new File(this.cacheDir, filename);
                ImageIO.write(image, "png", diskFile);
            } catch (Exception e) {
                PandabridgeMod.logger.error("Failed to save image to skin cache: {}", e);
            }
        }
    }
}
