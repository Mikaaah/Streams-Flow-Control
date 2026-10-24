package nl.townsandkingdoms.streamcontrol;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class ControlNetwork {
    /** Server-authoritative snapshot used to open the controller's client GUI. */
    public record Open(long pos, int radius, int vertical, double strength, double maxStrength,
                       int maxRadius, boolean connectedOnly, boolean still, boolean powered, int facing)
        implements CustomPacketPayload {
        public static final Type<Open> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(StreamControl.ID, "open_controller"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = new StreamCodec<>() {
            public Open decode(RegistryFriendlyByteBuf b) {
                return new Open(b.readLong(), b.readVarInt(), b.readVarInt(), b.readDouble(), b.readDouble(),
                    b.readVarInt(), b.readBoolean(), b.readBoolean(), b.readBoolean(), b.readVarInt());
            }
            public void encode(RegistryFriendlyByteBuf b, Open p) {
                b.writeLong(p.pos); b.writeVarInt(p.radius); b.writeVarInt(p.vertical); b.writeDouble(p.strength);
                b.writeDouble(p.maxStrength); b.writeVarInt(p.maxRadius); b.writeBoolean(p.connectedOnly);
                b.writeBoolean(p.still); b.writeBoolean(p.powered); b.writeVarInt(p.facing);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Never trust client-supplied dimensions, speed or access rights. */
    public record Configure(long pos, int radius, double strength, boolean connectedOnly, int facing)
        implements CustomPacketPayload {
        public static final Type<Configure> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(StreamControl.ID, "configure_controller"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Configure> CODEC = new StreamCodec<>() {
            public Configure decode(RegistryFriendlyByteBuf b) {
                return new Configure(b.readLong(), b.readVarInt(), b.readDouble(), b.readBoolean(), b.readVarInt());
            }
            public void encode(RegistryFriendlyByteBuf b, Configure p) {
                b.writeLong(p.pos); b.writeVarInt(p.radius); b.writeDouble(p.strength);
                b.writeBoolean(p.connectedOnly); b.writeVarInt(p.facing);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Update(String dimension, boolean reset, CompoundTag region, long remove) implements CustomPacketPayload {
        public static final Type<Update> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(StreamControl.ID, "region"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Update> CODEC = new StreamCodec<>() {
            public Update decode(RegistryFriendlyByteBuf b) { return new Update(b.readUtf(256), b.readBoolean(), b.readNbt(), b.readLong()); }
            public void encode(RegistryFriendlyByteBuf b, Update p) { b.writeUtf(p.dimension, 256); b.writeBoolean(p.reset); b.writeNbt(p.region); b.writeLong(p.remove); }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");
        registrar.playToClient(Update.TYPE, Update.CODEC, (p, context) -> context.enqueueWork(() -> {
            nl.townsandkingdoms.streamcontrol.client.ClientUpdates.apply(p);
        }));
        registrar.playToClient(Open.TYPE, Open.CODEC, (p, context) -> context.enqueueWork(() ->
            nl.townsandkingdoms.streamcontrol.client.ControllerScreen.open(p)));
        registrar.playToServer(Configure.TYPE, Configure.CODEC, (p, context) -> context.enqueueWork(() -> {
            Player player = context.player();
            BlockPos pos = BlockPos.of(p.pos());
            if (!(player.level() instanceof ServerLevel) || player.blockPosition().distSqr(pos) > 64
                || !player.mayBuild() || !player.level().mayInteract(player, pos)) return;
            if (!(player.level().getBlockEntity(pos) instanceof ControllerEntity be)) return;
            Direction d = Direction.from3DDataValue(p.facing());
            if (!d.getAxis().isHorizontal()) return;
            be.configure(d, ControlConfig.radius(p.radius()), ControlConfig.strength(p.strength()), p.connectedOnly());
        }));
    }
    public static void open(ServerPlayer player, ControllerEntity be) {
        var state = be.getBlockState();
        PacketDistributor.sendToPlayer(player, new Open(be.getBlockPos().asLong(), be.radius(),
            ControlConfig.VERTICAL.get(), be.strength(), ControlConfig.MAX_SPEED.get(),
            ControlConfig.MAX_RADIUS.get(), be.connectedOnly(), ((ControllerBlock)state.getBlock()).still,
            state.getValue(ControllerBlock.POWERED), state.getValue(ControllerBlock.FACING).get3DDataValue()));
    }
    public static void update(ServerLevel level, FlowRegion r) { PacketDistributor.sendToPlayersInDimension(level, new Update(level.dimension().location().toString(), false, r.save(), 0)); }
    public static void remove(ServerLevel level, long key) { PacketDistributor.sendToPlayersInDimension(level, new Update(level.dimension().location().toString(), false, null, key)); }
    public static void sync(ServerPlayer player) {
        var level = player.serverLevel(); var dim = level.dimension().location().toString();
        PacketDistributor.sendToPlayer(player, new Update(dim, true, null, 0));
        for (var r : ControlData.get(level).all()) PacketDistributor.sendToPlayer(player, new Update(dim, false, r.save(), 0));
    }
    public static void login(PlayerEvent.PlayerLoggedInEvent e) { if (e.getEntity() instanceof ServerPlayer p) sync(p); }
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { if (e.getEntity() instanceof ServerPlayer p) sync(p); }
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) { if (e.getEntity() instanceof ServerPlayer p) sync(p); }
}
