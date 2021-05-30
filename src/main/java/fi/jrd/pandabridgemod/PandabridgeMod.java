package fi.jrd.pandabridgemod;

import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import fi.jrd.pandabridgemod.matrix.AppService;
import fi.jrd.pandabridgemod.matrix.Homeserver;
import fi.jrd.pandabridgemod.matrix.Room;

@Mod.EventBusSubscriber(value = Side.SERVER, modid = PandabridgeMod.MODID)
@Mod(modid = PandabridgeMod.MODID, name = PandabridgeMod.NAME, version = PandabridgeMod.VERSION, serverSideOnly = true)
public class PandabridgeMod {
    public static final String MODID = "pandabridge";
    public static final String NAME = "Pandabridge Mod";
    public static final String VERSION = "1.0";

    public static Logger logger;

    public static String host;
    public static String authorization;

    private static Homeserver homeserver;
    private static Room mainRoom;

    private static int asPort;

    public static int getApplicationServicePort() {
        return asPort;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException {
        logger = event.getModLog();

        Properties props = new Properties();
        FileInputStream stream;
        try {
            stream = new FileInputStream(event.getSuggestedConfigurationFile());
            props.load(stream);

            host = props.getProperty("host");
            homeserver = new Homeserver(host);

            mainRoom = homeserver.getRoom(props.getProperty("mainRoom"));
            authorization = String.format("Bearer %s", props.getProperty("auth"));

            asPort = Integer.parseInt(props.getProperty("port"));

        } catch (IOException e) {
            logger.error("Loading Pandabridge config failed!");
            throw e;
        }

        try {
            new AppService().run();
            logger.info("AppService started");
        } catch (InterruptedException e) {
            logger.error("Starting appservice server failed!");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Maybe do something fun
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        mainRoom.sendMessage("Server started");
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        String msg = String.format("Logged IN: %s", event.player.getName());
        mainRoom.sendMessage(msg);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        String msg = String.format("Logged OUT: %s", event.player.getName());
        mainRoom.sendMessage(msg);
    }

    @SubscribeEvent
    public static void onServerChatEvent(ServerChatEvent event) {
        String msg = String.format("<%s@mine> %s", event.getUsername(), event.getMessage());
        logger.info(msg);

        mainRoom.sendMessage(msg);
    }

    public static void broadcast(String displayName, String message) {
        PlayerList players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

        message = StringUtils.normalizeSpace(message);

        for (int i = 0; i < message.length(); ++i) {
            if (!ChatAllowedCharacters.isAllowedCharacter(message.charAt(i))) {
                logger.warn("Bad characters in: {}", message);
                return;
            }
        }

        ITextComponent text = new TextComponentTranslation("chat.type.text", displayName,
                new TextComponentString(message));
        players.sendMessage(text, false); // false for not a system message
    }
}
