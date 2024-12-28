package btw.community.gloomyhostile;

import btw.AddonHandler;
import btw.BTWAddon;
import btw.BTWMod;
import btw.world.util.data.BTWWorldData;
import btw.world.util.data.DataEntry;
import btw.world.util.data.DataProvider;
import btw.world.util.WorldUtils;
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

    public static boolean enableGloomEverywhere;
    public static boolean keepGloomPostDragon;
    
    public static final DataEntry<Integer> WORLD_STATE = DataProvider.getBuilder(Integer.class)
            .global()
            .name("world_state")
            .defaultSupplier(() -> 0)
            .readNBT(NBTTagCompound::getInteger)
            .writeNBT(NBTTagCompound::setInteger)
            .build();

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
                    try {
                        int customState = Integer.parseInt(strings[1]);
                        long duration = Long.parseLong(strings[2]);
                        server.worldServers[0].setData(WORLD_STATE, customState);
                        GloomyHostile.forcedWorldState = customState;
                        GloomyHostile.forcedStateDuration = duration;
                        iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state is set to " + customState + " for " + duration / 20 + " seconds. This doesn't persist between restarts."));
                    } catch (NumberFormatException e) {
                        throw new WrongUsageException("Invalid state or duration length.");
                    }
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
                    GloomyHostile.forcedWorldState = 0;
                    GloomyHostile.forcedStateDuration = 0;
                }
                else if (strings.length == 1 && strings[0].equals("get")) {
                    iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state: " + server.worldServers[0].getData(WORLD_STATE)));
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
                player.worldObj.setData(WORLD_STATE, worldState);
                GloomyHostile.worldState = worldState;
            }
        });
    }

    @Override
    public void serverPlayerConnectionInitialized(NetServerHandler serverHandler, EntityPlayerMP playerMP) {
        sendWorldStateToClient(serverHandler);
    }

    private static void sendWorldStateToClient(NetServerHandler serverHandler) {
        Packet250CustomPayload packet = createWorldStatePacket();
        serverHandler.sendPacketToPlayer(packet);
    }

    private static Packet250CustomPayload createWorldStatePacket() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeInt(MinecraftServer.getServer().worldServers[0].getData(WORLD_STATE));
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
        WORLD_STATE.register();
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