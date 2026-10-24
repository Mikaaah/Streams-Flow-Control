package nl.townsandkingdoms.streamcontrol;
import dev.streamsreflowing.flow.StreamFlowGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
public final class FlowOverrides {
    private static volatile RegionIndex client = new RegionIndex(java.util.List.of());
    private static final java.util.Map<Long, FlowRegion> CLIENT_REGIONS = new java.util.LinkedHashMap<>();
    private static volatile String dimension = "";
    public static synchronized FlowRegion clientByKey(long key) { return CLIENT_REGIONS.get(key); }
    public static synchronized void receive(String dim, boolean reset, FlowRegion region, long remove) {
        if (reset || !dim.equals(dimension)) { CLIENT_REGIONS.clear(); dimension = dim; }
        if (region != null) CLIENT_REGIONS.put(region.key(), region); else if (!reset) CLIENT_REGIONS.remove(remove);
        client = new RegionIndex(CLIENT_REGIONS.values());
    }
    public static FlowRegion at(BlockGetter level, int x, int y, int z) {
        if (!ControlConfig.ENABLED.get() || y == Integer.MIN_VALUE) return null;
        if (level instanceof ServerLevel server) return ControlData.get(server).at(x, y, z);
        if (level instanceof Level world && !world.dimension().location().toString().equals(dimension)) return null;
        if (!(level instanceof Level) && !(level instanceof BlockAndTintGetter)) return null;
        return client.at(x, y, z);
    }
    public static FlowRegion clientAt(int x, int y, int z) {
        return ControlConfig.ENABLED.get() ? client.at(x, y, z) : null;
    }
    /** A still region must not expose the wand/controller's stored facing to client flow rendering. */
    public static byte visualAngle(FlowRegion region) {
        return region.still() ? StreamFlowGrid.encode(0, 0) : StreamFlowGrid.encode(region.dx(), region.dz());
    }
    public static Vec3 apply(Vec3 original, BlockGetter level, BlockPos pos, FluidState state) {
        if (!state.is(FluidTags.WATER)) return original;
        var r = at(level, pos.getX(), pos.getY(), pos.getZ()); return r == null ? original : r.vector();
    }
}
