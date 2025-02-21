package btw.community.gloomyhostile;

import btw.AddonHandler;
import btw.BTWAddon;
import btw.world.util.data.BTWWorldData;
import btw.world.util.difficulty.Difficulties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.io.*;
import java.util.*;

public class GloomyHostile extends BTWAddon {
    private static GloomyHostile instance;

    public static int worldState = 0;
    public static int forcedWorldState = 0;
    public static long forcedStateDuration = 0;
    public static int postWitherSunTicks = 0;
    public static int postNetherMoonTicks = 0;

    public final static int sunTransitionTime = 360;
    public final static int moonTransitionTime = 240;

    public static boolean enableGloomEverywhere;
    public static boolean keepGloomPostDragon;
    public static int challengeWorldState;

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
            public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] strings) {
                if (strings.length == 2 && strings[0].equals("reset")) {
                    return getListOfStringsMatchingLastWord(strings, "nether", "wither", "end");
                }
                else if (strings.length == 2 && strings[0].equals("force")) {
                    return getListOfStringsMatchingLastWord(strings, "0", "1", "2", "reset");
                }
                else if (strings.length == 1) {
                    return getListOfStringsMatchingLastWord(strings, "get", "force", "reset");
                }
                return null;
            }

            @Override
            public String getCommandUsage(ICommandSender iCommandSender) {
                return "/gloomyhostile <get / force <state, duration in ticks / reset> / reset <nether / wither / end>>";
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
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state is set to " + customState + " for " + duration / 20d + " seconds. This doesn't persist between restarts."));
                }
                else if (strings.length == 2 && strings[0].equals("reset") && strings[1].equals("end")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("End accessed boolean is now FALSE."));
                    server.worldServers[0].setData(BTWWorldData.END_ACCESSED, false);
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
                else if (strings.length == 2 && strings[0].equals("force")) {
                    throw new WrongUsageException("/gloomyhostile force <state, duration in ticks>");
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
        //message on join
        if (challengeWorldState > 0) {
            String challengeText = "Huh, challenging yourself with total darkness during night? Good luck!";
            if (challengeWorldState == 2) {
                challengeText = "You're going to be in pain, stone axe is pretty much mandatory.";
            }
            if (AddonHandler.getModByID("nightmare_mode") != null) {
                if (MinecraftServer.getServer().worldServers[0].getDifficulty() == Difficulties.HOSTILE) {
                    if (MinecraftServer.getIsServer()) challengeText = "Sadism, pure sadism.";
                    else challengeText = "You'll die.";
                }
            }
            ChatMessageComponent message = ChatMessageComponent.createFromText(challengeText);
            message.setItalic(true);
            message.setBold(true);
            message.setColor(EnumChatFormatting.GOLD);
            serverHandler.sendPacketToPlayer(new Packet3Chat(message));

        }
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
        challengeWorldState = Integer.parseInt(propertyValues.get("WorldChallengeLevel"));
        //this is to prevent breaking things, and also for anti-exploit reason.
        if (challengeWorldState >= 3 || challengeWorldState < 0) {
            AddonHandler.logWarning("Illegal world state challenge level of " + challengeWorldState);
            challengeWorldState = MathHelper.clamp_int(challengeWorldState, 0, 2);
        }
    }
    @Override
    public void preInitialize() {
        this.registerProperty("EnableGloomEverywhere", "False", "! WARNING ! IF YOU'RE SURE YOU WANT THIS OUTSIDE HOSTILE DIFFICULTY, SET THIS TO TRUE ! WARNING !");
        this.registerProperty("KeepGloomPostDragon", "False", "! WARNING ! IF YOU'RE SURE YOU WANT TO KEEP ETERNAL NIGHT POST-DRAGON, SET THIS TO TRUE ! WARNING !");
        this.registerProperty("WorldChallengeLevel", "0", "! WARNING ! IF YOU'RE SURE YOU WANT TO CAUSE PAIN ON YOURSELF, SET IT TO 1 FOR GLOOMY NIGHTS, 2 FOR ETERNAL DARKNESS ! WARNING !");
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