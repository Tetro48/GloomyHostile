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

    public static int sunTransitionTime = 360;
    public static int moonTransitionTime = 240;

    public static long postNetherMoonDelay = -1;
    public static long postWitherSunDelay = -1;

    public static boolean isNightmareModeInstalled;

    public static boolean isCancerMode = false;

    public static boolean enableGloomEverywhere;
    public static boolean keepGloomPostDragon;
    public static int challengeWorldState;
    public static int cancerModeToggle;

    public GloomyHostile() {
        super();
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
        if (!MinecraftServer.getIsServer()) {
            initClientPacketInfo();
        }
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        System.out.println("day & month = " + day + ", " + month);
        if ((day == 1 && month == Calendar.APRIL && cancerModeToggle == 0) || cancerModeToggle == 2) {
            isCancerMode = true;
            sunTransitionTime = 60;
            moonTransitionTime = 40;
        }
        registerAddonCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "gloomyhostile";
            }

            @Override
            public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] strings) {
                if (strings.length == 2 && strings[0].equals("set")) {
                    return getListOfStringsMatchingLastWord(strings, "nether", "wither", "end");
                }
                else if (strings.length == 3 && strings[0].equals("set")) {
                    return getListOfStringsMatchingLastWord(strings, "true", "false");
                }
                else if (strings.length == 2 && strings[0].equals("force")) {
                    return getListOfStringsMatchingLastWord(strings, "0", "1", "2", "reset");
                }
                else if (strings.length == 1) {
                    return getListOfStringsMatchingLastWord(strings, "get", "force", "set");
                }
                return null;
            }

            @Override
            public String getCommandUsage(ICommandSender iCommandSender) {
                return "/gloomyhostile <get / force <state, duration in ticks / reset> / set <nether / wither / end> <true / false>>";
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
                else if (strings.length == 3 && strings[0].equals("set") && strings[1].equals("end")) {
                    boolean parsedBool = Boolean.parseBoolean(strings[2]);
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("End accessed boolean is now " + String.valueOf(parsedBool).toUpperCase() + "."));
                    server.worldServers[0].setData(BTWWorldData.END_ACCESSED, parsedBool);
                }
                else if (strings.length == 3 && strings[0].equals("set") && strings[1].equals("wither")) {
                    boolean parsedBool = Boolean.parseBoolean(strings[2]);
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Wither summoned boolean is now " + String.valueOf(parsedBool).toUpperCase() + "."));
                    server.worldServers[0].setData(BTWWorldData.WITHER_SUMMONED, parsedBool);
                }
                else if (strings.length == 3 && strings[0].equals("set") && strings[1].equals("nether")) {
                    boolean parsedBool = Boolean.parseBoolean(strings[2]);
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Nether accessed boolean is now " + String.valueOf(parsedBool).toUpperCase() + "."));
                    server.worldServers[0].setData(BTWWorldData.NETHER_ACCESSED, parsedBool);
                }
                else if (strings.length == 2 && strings[0].equals("set")) {
                    throw new WrongUsageException("/gloomyhostile set <nether / wither / end> <true / false>");
                }
                else if (strings.length == 2 && strings[0].equals("force") && strings[1].equals("reset")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state is no longer forced."));
                    GloomyHostile.forcedWorldState = 0;
                    GloomyHostile.forcedStateDuration = 0;
                }
                else if (strings.length == 2 && strings[0].equals("force")) {
                    throw new WrongUsageException("/gloomyhostile force <state> <duration in ticks>");
                }
                else if (strings.length == 1 && strings[0].equals("get")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(
                            "World state: " + GloomyHostile.worldState
                            + "\nDelays: Post-nether: " + postNetherMoonDelay + " ticks, post-wither: " + postWitherSunDelay + " ticks."));
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
            if (isNightmareModeInstalled) {
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
        if (isCancerMode) {

            String afText = "You notice that the world is doing something... weird.";
            ChatMessageComponent message = ChatMessageComponent.createFromText(afText);
            message.setItalic(true);
//            message.setBold(true);
            message.setColor(EnumChatFormatting.GRAY);
            serverHandler.sendPacketToPlayer(new Packet3Chat(message));
            String afText2 = "Don't question it ;)";
            ChatMessageComponent message2 = ChatMessageComponent.createFromText(afText2);
            message2.setItalic(true);
            message2.setBold(true);
            message2.setColor(EnumChatFormatting.GOLD);
            serverHandler.sendPacketToPlayer(new Packet3Chat(message2));
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
        cancerModeToggle = Integer.parseInt(propertyValues.get("CancerModeToggle"));
        cancerModeToggle = MathHelper.clamp_int(cancerModeToggle, 0, 2);
    }
    @Override
    public void preInitialize() {
        this.registerProperty("EnableGloomEverywhere", "False", "! WARNING ! IF YOU'RE SURE YOU WANT THIS OUTSIDE HOSTILE DIFFICULTY, SET THIS TO TRUE ! WARNING !");
        this.registerProperty("KeepGloomPostDragon", "False", "! WARNING ! IF YOU'RE SURE YOU WANT TO KEEP ETERNAL NIGHT POST-DRAGON, SET THIS TO TRUE ! WARNING !");
        this.registerProperty("WorldChallengeLevel", "0", "! WARNING ! IF YOU'RE SURE YOU WANT TO CAUSE PAIN ON YOURSELF, SET IT TO 1 FOR GLOOMY NIGHTS, 2 FOR ETERNAL DARKNESS ! WARNING !");
        this.registerProperty("CancerModeToggle", "0", "Set this to 1 if you don't want April Fools to screw you over, lol, or set it to 2 to screw yourself over outside of April Fools.");
    }

    @Override
    public void postInitialize() {
        isNightmareModeInstalled = AddonHandler.getModByID("nightmare_mode") != null;
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