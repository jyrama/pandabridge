package fi.jrd.pandabridgemod;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
            try {
                File diskFile = new File(this.cacheDir, id + ".png");
                ImageIO.write(image, "png", diskFile);
            } catch (Exception e) {
                PandabridgeMod.logger.error("Failed to save image to skin cache: {}", e);
            }
        }
    }
}
