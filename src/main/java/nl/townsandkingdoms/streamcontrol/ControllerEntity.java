package nl.townsandkingdoms.streamcontrol;
import java.util.UUID;
import java.util.Arrays;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class ControllerEntity extends BlockEntity {
    private int radius = -1;
    private double strength = -1;
    private boolean connectedOnly;
    private boolean connectivityDirty = true;
    private boolean initialized;
    public ControllerEntity(BlockPos p, BlockState s) { super(StreamControl.CONTROLLER.get(), p, s); }
    public int radius() { return ControlConfig.radius(radius < 1 ? ControlConfig.DEFAULT_RADIUS.get() : radius); }
    public double strength() { return ControlConfig.strength(strength < 0 ? ControlConfig.SPEED.get() : strength); }
    public boolean connectedOnly() { return connectedOnly; }
    public void setRadius(int r) { radius = ControlConfig.radius(r); connectivityDirty = true; setChanged(); refresh(); }
    public void configure(Direction d, int r, double speed) { configure(d, r, speed, connectedOnly); }
    public void configure(Direction d, int r, double speed, boolean onlyConnected) {
        radius = ControlConfig.radius(r); strength = ControlConfig.strength(speed);
        connectedOnly = onlyConnected;
        connectivityDirty = true;
        if (level != null) level.setBlock(worldPosition, getBlockState().setValue(ControllerBlock.FACING,d),3);
        setChanged(); refresh();
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registry) { super.saveAdditional(tag,registry); tag.putInt("Radius",radius); tag.putDouble("Strength",strength); tag.putBoolean("ConnectedOnly",connectedOnly); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registry) { super.loadAdditional(tag,registry); radius = tag.contains("Radius") ? tag.getInt("Radius") : -1; strength = tag.contains("Strength") ? tag.getDouble("Strength") : -1; connectedOnly = tag.getBoolean("ConnectedOnly"); connectivityDirty = true; }
    public void waterChanged() { connectivityDirty = true; refresh(); }
    public static void tick(Level l, BlockPos p, BlockState s, ControllerEntity be) {
        if (!be.initialized || l.getGameTime() % 20 == 0) { be.initialized = true; be.refresh(); }
    }
    public void refresh() {
        if (!(level instanceof ServerLevel server)) return;
        var data = ControlData.get(server); long key = worldPosition.asLong();
        if (getBlockState().getValue(ControllerBlock.POWERED) || !ControlConfig.ENABLED.get()) { data.remove(server,key); return; }
        Direction d = getBlockState().getValue(ControllerBlock.FACING);
        boolean still = ((ControllerBlock)getBlockState().getBlock()).still;
        double sp = strength();
        var old = data.byKey(key);
        long[] mask = new long[0];
        if (connectedOnly) {
            boolean rebuild = connectivityDirty || old == null || !old.connectedOnly()
                || old.radius() != radius() || old.vertical() != ControlConfig.VERTICAL.get()
                || server.getGameTime() % 100 == 0;
            mask = rebuild ? ConnectedWater.scan(server, worldPosition, d, radius(), ControlConfig.VERTICAL.get()) : old.connectedMask();
        }
        connectivityDirty = false;
        if (old != null && old.radius() == radius() && old.vertical() == ControlConfig.VERTICAL.get()
            && old.dx() == d.getStepX() && old.dz() == d.getStepZ() && old.strength() == sp && old.still() == still
            && old.connectedOnly() == connectedOnly && Arrays.equals(old.connectedMask(), mask)) return;
        data.put(server, new FlowRegion(key, worldPosition.immutable(), radius(), ControlConfig.VERTICAL.get(),
            d.getStepX(), d.getStepZ(), sp, still, true, new UUID(0,0), data.next(), connectedOnly, mask));
    }
}
