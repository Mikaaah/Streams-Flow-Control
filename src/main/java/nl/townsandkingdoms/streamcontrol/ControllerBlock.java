package nl.townsandkingdoms.streamcontrol;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
public final class ControllerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final MapCodec<ControllerBlock> CODEC = simpleCodec(p -> new ControllerBlock(false, p));
    public static final MapCodec<ControllerBlock> ANCHOR_CODEC = simpleCodec(p -> new ControllerBlock(true, p));
    public final boolean still;
    public ControllerBlock(boolean still, Properties p) {
        super(p); this.still = still;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(POWERED, false));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return still ? ANCHOR_CODEC : CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, WATERLOGGED, POWERED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING, c.getHorizontalDirection()).setValue(WATERLOGGED, c.getLevel().getFluidState(c.getClickedPos()).is(net.minecraft.tags.FluidTags.WATER)).setValue(POWERED, c.getLevel().hasNeighborSignal(c.getClickedPos())); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ControllerEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return level.isClientSide ? null : createTickerHelper(type, StreamControl.CONTROLLER.get(), ControllerEntity::tick); }
    @Override protected FluidState getFluidState(BlockState s) { return s.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(s); }
    @Override protected BlockState updateShape(BlockState s, Direction d, BlockState n, LevelAccessor l, BlockPos p, BlockPos np) { if (s.getValue(WATERLOGGED)) l.scheduleTick(p, Fluids.WATER, Fluids.WATER.getTickDelay(l)); return super.updateShape(s,d,n,l,p,np); }
    @Override protected void neighborChanged(BlockState s, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != s.getValue(POWERED)) level.setBlock(pos, s.setValue(POWERED, powered), 3);
            if (level.getBlockEntity(pos) instanceof ControllerEntity be) be.waterChanged();
        }
    }
    @Override protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player p, BlockHitResult hit) {
        if (!p.mayBuild() || !level.mayInteract(p,pos)) return InteractionResult.FAIL;
        if (!level.isClientSide && p instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof ControllerEntity be)
            ControlNetwork.open(player, be);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState old, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!old.is(next.getBlock()) && level instanceof ServerLevel server) ControlData.get(server).remove(server, pos.asLong());
        super.onRemove(old,level,pos,next,moving);
    }
    @Override protected BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState s, Mirror m) { return rotate(s, m.getRotation(s.getValue(FACING))); }
}
