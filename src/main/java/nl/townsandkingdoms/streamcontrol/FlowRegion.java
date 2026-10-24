package nl.townsandkingdoms.streamcontrol;

import java.util.UUID;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/** Immutable cylinder. Block regions have priority, then stillness, then newest sequence. */
public record FlowRegion(long key, BlockPos center, int radius, int vertical, double dx, double dz,
                         double strength, boolean still, boolean controller, UUID owner, long sequence,
                         boolean connectedOnly, long[] connectedMask) {
    /** Existing wand edits and old saves remain unrestricted by default. */
    public FlowRegion(long key, BlockPos center, int radius, int vertical, double dx, double dz,
                      double strength, boolean still, boolean controller, UUID owner, long sequence) {
        this(key, center, radius, vertical, dx, dz, strength, still, controller, owner, sequence, false, new long[0]);
    }
    public FlowRegion {
        connectedMask = connectedMask == null ? new long[0] : connectedMask.clone();
    }
    @Override public long[] connectedMask() { return connectedMask.clone(); }
    @Override public boolean equals(Object obj) {
        if (!(obj instanceof FlowRegion r)) return false;
        return key == r.key && center.equals(r.center) && radius == r.radius && vertical == r.vertical
            && Double.compare(dx,r.dx) == 0 && Double.compare(dz,r.dz) == 0
            && Double.compare(strength,r.strength) == 0 && still == r.still && controller == r.controller
            && owner.equals(r.owner) && sequence == r.sequence && connectedOnly == r.connectedOnly
            && Arrays.equals(connectedMask, r.connectedMask);
    }
    @Override public int hashCode() {
        return 31 * Objects.hash(key, center, radius, vertical, dx, dz, strength, still, controller,
            owner, sequence, connectedOnly) + Arrays.hashCode(connectedMask);
    }
    public boolean contains(int x, int y, int z) {
        long ax = (long)x - center.getX(), az = (long)z - center.getZ();
        if (ax * ax + az * az > (long)radius * radius || Math.abs((long)y - center.getY()) > vertical) return false;
        if (!connectedOnly) return true;
        int width = radius * 2 + 1;
        int bit = (((y - center.getY() + vertical) * width) + (z - center.getZ() + radius)) * width
            + (x - center.getX() + radius);
        return (bit >>> 6) < connectedMask.length && (connectedMask[bit >>> 6] & (1L << (bit & 63))) != 0;
    }
    public Vec3 vector() { return still ? Vec3.ZERO : new Vec3(dx * strength, 0, dz * strength); }
    public int priority() { return (controller ? 2 : 0) + (still ? 1 : 0); }
    public CompoundTag save() {
        var n = new CompoundTag(); n.putLong("Key", key); n.putLong("Pos", center.asLong());
        n.putInt("Radius", radius); n.putInt("Vertical", vertical); n.putDouble("DX", dx); n.putDouble("DZ", dz);
        n.putDouble("Strength", strength); n.putBoolean("Still", still); n.putBoolean("Controller", controller);
        n.putBoolean("ConnectedOnly", connectedOnly);
        if (connectedOnly) n.putLongArray("WaterMask", connectedMask);
        n.putUUID("Owner", owner); n.putLong("Sequence", sequence); return n;
    }
    public static FlowRegion load(CompoundTag n) {
        double dx = n.getDouble("DX"), dz = n.getDouble("DZ"), length = Math.hypot(dx, dz);
        if (!Double.isFinite(length) || length < 1e-6) { dx = 1; dz = 0; length = 1; }
        double speed = n.getDouble("Strength");
        return new FlowRegion(n.getLong("Key"), BlockPos.of(n.getLong("Pos")), Math.clamp(n.getInt("Radius"), 1, 32),
            Math.clamp(n.getInt("Vertical"), 0, 16), dx / length, dz / length,
            Double.isFinite(speed) ? Math.clamp(speed, 0.05, 2) : 1, n.getBoolean("Still"), n.getBoolean("Controller"),
            n.hasUUID("Owner") ? n.getUUID("Owner") : new UUID(0, 0), n.getLong("Sequence"),
            n.getBoolean("ConnectedOnly"), n.getLongArray("WaterMask"));
    }
}
