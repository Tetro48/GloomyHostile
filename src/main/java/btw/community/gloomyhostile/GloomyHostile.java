package btw.community.gloomyhostile;

import btw.AddonHandler;
import btw.BTWAddon;
import btw.BTWMod;
import btw.world.util.data.BTWWorldData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.io.*;
import java.util.List;
import java.util.Map;

public class GloomyHostile extends BTWAddon {
    private static GloomyHostile instance;

    public static int worldState = 0;
    public static int forcedWorldState = 0;
    public static long forcedStateDuration = 0;
    public static int postWitherSunTicks = 0;
    public static int postNetherMoonTicks = 0;

    public final static int sunTransitionTime = 240;
    public final static int moonTransitionTime = 240;

    public static boolean enableGloomEverywhere;
    public static boolean keepGloomPostDragon;

    public GloomyHostile() {
        super();
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
        if (!MinecraftServer.getIsServer()) {
            initClientPacketInfo();
        }
        registerAddonCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "gloomyhostile";
            }

            @Override
            public String getCommandUsage(ICommandSender iCommandSender) {
                return "/gloomyhostile <get / force <state, duration in ticks / reset> / reset <nether / wither>>";
            }

            @Override
            public void processCommand(ICommandSender iCommandSender, String[] strings) {
                MinecraftServer server = MinecraftServer.getServer();
                if (strings.length == 3 && strings[0].equals("force")) {
                    int customState = parseIntBounded(iCommandSender, strings[1], 0, 3);
                    long duration = parseIntWithMin(iCommandSender, strings[2], 0);
                    GloomyHostile.worldState = customState;
                    GloomyHostile.forcedWorldState = customState;
                    GloomyHostile.forcedStateDuration = duration;
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state is set to " + customState + " for " + duration / 20 + " seconds. This doesn't persist between restarts."));
                }
                else if (strings.length == 2 && strings[0].equals("reset") && strings[1].equals("wither")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Wither summoned boolean is now FALSE."));
                    server.worldServers[0].setData(BTWWorldData.WITHER_SUMMONED, false);
                }
                else if (strings.length == 2 && strings[0].equals("reset") && strings[1].equals("nether")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Nether accessed boolean is now FALSE."));
                    server.worldServers[0].setData(BTWWorldData.NETHER_ACCESSED, false);
                }
                else if (strings.length == 2 && strings[0].equals("force") && strings[1].equals("reset")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state is no longer forced."));
                    GloomyHostile.forcedWorldState = 0;
                    GloomyHostile.forcedStateDuration = 0;
                }
                else if (strings.length == 1 && strings[0].equals("get")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state: " + GloomyHostile.worldState));
                }
                else {
                    throw new WrongUsageException(getCommandUsage(iCommandSender));
                }
            }

            @Override
            public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
                return iCommandSender.canCommandSenderUseCommand(4, getCommandName());
            }
        });
    }
    
    @Environment(EnvType.CLIENT)
    private void initClientPacketInfo() {
        AddonHandler.registerPacketHandler("gloomyhostile|state", (packet, player) -> {
            DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(packet.data));
            int worldState = -1;
            try {
                worldState = dataStream.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (worldState != -1) {
                GloomyHostile.worldState = worldState;
            }
        });
        AddonHandler.registerPacketHandler("gloomyhostile|onJoin", (packet, player) -> {
            postWitherSunTicks = 999;
            postNetherMoonTicks = 999;
        });
    }

    @Override
    public void serverPlayerConnectionInitialized(NetServerHandler serverHandler, EntityPlayerMP playerMP) {
        sendWorldStateToClient(serverHandler);
        Packet250CustomPayload onJoinPacket = new Packet250CustomPayload("gloomyhostile|onJoin", new byte[0]);
        serverHandler.sendPacketToPlayer(onJoinPacket);
    }

    private static void sendWorldStateToClient(NetServerHandler serverHandler) {
        Packet250CustomPayload packet = createWorldStatePacket();
        serverHandler.sendPacketToPlayer(packet);
    }

    private static Packet250CustomPayload createWorldStatePacket() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeInt(GloomyHostile.worldState);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload("gloomyhostile|state", byteStream.toByteArray());
        return packet;
    }

    @Override
    public void handleConfigProperties(Map<String, String> propertyValues) {
        enableGloomEverywhere = Boolean.parseBoolean(propertyValues.get("EnableGloomEverywhere"));
        keepGloomPostDragon = Boolean.parseBoolean(propertyValues.get("KeepGloomPostDragon"));
    }
    @Override
    public void preInitialize() {
        this.registerProperty("EnableGloomEverywhere", "False", "! WARNING ! IF YOU'RE SURE YOU WANT THIS OUTSIDE HOSTILE DIFFICULTY, SET THIS TO TRUE ! WARNING !");
        this.registerProperty("KeepGloomPostDragon", "False", "! WARNING ! IF YOU'RE SURE YOU WANT TO KEEP ETERNAL NIGHT POST-DRAGON, SET THIS TO TRUE ! WARNING !");
    }

    public static void sendWorldStateToAllPlayers() {
        Packet250CustomPayload packet = createWorldStatePacket();
        for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacketToPlayer(packet);
            }
        }
    }

}