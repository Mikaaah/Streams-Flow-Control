package nl.townsandkingdoms.streamcontrol;

import java.util.ArrayDeque;
import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;

/** A bounded six-direction water flood-fill; it never loads an unloaded chunk. */
public final class ConnectedWater {
    private ConnectedWater() {}

    public static long[] scan(ServerLevel level, BlockPos center, Direction facing, int radius, int vertical) {
        int width = radius * 2 + 1;
        int area = width * width;
        int volume = area * (vertical * 2 + 1);
        BitSet seen = new BitSet(volume);
        BitSet wet = new BitSet(volume);
        ArrayDeque<Integer> open = new ArrayDeque<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        // The controller may be waterlogged; otherwise prefer water directly in front.
        Direction[] starts = { facing, Direction.DOWN, facing.getClockWise(), facing.getCounterClockWise(),
            facing.getOpposite(), Direction.UP };
        int seed = waterAt(level, center, center, radius, vertical, width, cursor);
        if (seed < 0) for (Direction d : starts) {
            seed = waterAt(level, center, center.relative(d), radius, vertical, width, cursor);
            if (seed >= 0) break;
        }
        if (seed < 0) return new long[0];
        wet.set(seed);
        seen.set(seed);
        open.add(seed);
        while (!open.isEmpty()) {
            int index = open.removeFirst();
            int relY = index / area - vertical;
            int relZ = index / width % width - radius;
            int relX = index % width - radius;
            for (Direction d : Direction.values()) {
                int x = relX + d.getStepX();
                int y = relY + d.getStepY();
                int z = relZ + d.getStepZ();
                if (Math.abs(y) > vertical || x * x + z * z > radius * radius) continue;
                int next = ((y + vertical) * width + z + radius) * width + x + radius;
                if (seen.get(next)) continue;
                seen.set(next);
                cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                if (level.hasChunkAt(cursor) && level.getFluidState(cursor).is(FluidTags.WATER)) {
                    wet.set(next);
                    open.addLast(next);
                }
            }
        }
        return wet.toLongArray();
    }

    private static int waterAt(ServerLevel level, BlockPos center, BlockPos pos,
                               int radius, int vertical, int width, BlockPos.MutableBlockPos cursor) {
        int x = pos.getX() - center.getX(), y = pos.getY() - center.getY(), z = pos.getZ() - center.getZ();
        if (Math.abs(y) > vertical || x * x + z * z > radius * radius) return -1;
        cursor.set(pos);
        if (!level.hasChunkAt(cursor) || !level.getFluidState(cursor).is(FluidTags.WATER)) return -1;
        return ((y + vertical) * width + z + radius) * width + x + radius;
    }
}
