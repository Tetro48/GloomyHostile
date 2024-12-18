package btw.community.gloomyhostile;

import btw.AddonHandler;
import btw.BTWAddon;
import btw.BTWMod;
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
    public static boolean isDaytime;

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
                return "/gloomyhostile";
            }

            @Override
            public void processCommand(ICommandSender iCommandSender, String[] strings) {
                MinecraftServer server = MinecraftServer.getServer();
                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("World state: " + server.worldServers[0].getData(WORLD_STATE)));
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