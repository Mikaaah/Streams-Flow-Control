package nl.townsandkingdoms.streamcontrol;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
public final class FlowWand extends Item {
    public FlowWand(Properties properties) { super(properties); }
    public static CompoundTag settings(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    private static void message(Player p, String key, Object... args) { p.displayClientMessage(Component.translatable(key, args), true); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var p = context.getPlayer(); if (p == null) return InteractionResult.PASS;
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ControllerEntity be) {
            if (!context.getLevel().isClientSide && allowed(p, context.getClickedPos(), context.getItemInHand())
                    && p instanceof ServerPlayer player) {
                ControlNetwork.open(player, be);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
    public static boolean allowed(Player p, BlockPos pos, ItemStack stack) {
        return ControlConfig.WAND_ENABLED.get() && (!ControlConfig.OP_ONLY.get() || p.hasPermissions(2))
            && p.mayBuild() && p.level().mayInteract(p, pos) && p.mayUseItemAt(pos, Direction.UP, stack);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level world, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        if (world.isClientSide) return InteractionResultHolder.success(stack);
        var hit = getPlayerPOVHitResult(world, p, ClipContext.Fluid.ANY);
        var n = settings(stack);
        int mode = Math.floorMod(n.getInt("Mode"), 3);
        int radius = ControlConfig.radius(n.contains("Radius") ? n.getInt("Radius") : ControlConfig.DEFAULT_RADIUS.get());
        if (hit.getType() == HitResult.Type.MISS) {
            if (p.isShiftKeyDown()) { mode = (mode + 1) % 3; n.putInt("Mode", mode); message(p, "message.streamcontrol.mode", Component.translatable("mode.streamcontrol." + mode)); }
            else { radius = radius % ControlConfig.MAX_RADIUS.get() + 1; n.putInt("Radius", radius); message(p, "message.streamcontrol.radius", radius); }
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(n)); return InteractionResultHolder.success(stack);
        }
        BlockPos pos = hit.getBlockPos();
        if (!world.getFluidState(pos).is(FluidTags.WATER)) return InteractionResultHolder.pass(stack);
        if (!allowed(p, pos, stack)) { message(p, "message.streamcontrol.denied"); return InteractionResultHolder.fail(stack); }
        var server = (ServerLevel)world; var data = ControlData.get(server);
        if (mode == 2) {
            var old = data.at(pos.getX(), pos.getY(), pos.getZ());
            if (old == null) { message(p, "message.streamcontrol.none"); return InteractionResultHolder.success(stack); }
            if (old.controller()) { message(p, "message.streamcontrol.remove_block"); return InteractionResultHolder.fail(stack); }
            if (!old.owner().equals(p.getUUID()) && !p.hasPermissions(2)) { message(p, "message.streamcontrol.denied"); return InteractionResultHolder.fail(stack); }
            data.remove(server, old.key()); message(p, "message.streamcontrol.restored");
        } else {
            long key = pos.asLong() ^ Long.MIN_VALUE;
            var existing = data.byKey(key);
            if (existing != null && !existing.owner().equals(p.getUUID()) && !p.hasPermissions(2)) { message(p, "message.streamcontrol.denied"); return InteractionResultHolder.fail(stack); }
            if (existing == null && data.owned(p.getUUID()) >= ControlConfig.MAX_PLAYER_REGIONS.get()) { message(p, "message.streamcontrol.limit"); return InteractionResultHolder.fail(stack); }
            var look = p.getLookAngle(); double len = Math.hypot(look.x, look.z);
            double dx = len > 1e-4 ? look.x / len : p.getDirection().getStepX();
            double dz = len > 1e-4 ? look.z / len : p.getDirection().getStepZ();
            var region = new FlowRegion(key, pos.immutable(), radius, ControlConfig.VERTICAL.get(), dx, dz,
                ControlConfig.strength(n.contains("Strength") ? n.getDouble("Strength") : ControlConfig.SPEED.get()), mode == 1, false, p.getUUID(), data.next());
            if (!data.put(server, region)) { message(p, "message.streamcontrol.limit"); return InteractionResultHolder.fail(stack); }
            message(p, "message.streamcontrol.applied", Component.translatable("mode.streamcontrol." + mode), radius);
        }
        world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.2f);
        if (ControlConfig.PARTICLES.get()) for (int i = 0; i < 32; i++) {
            double a = i * Math.PI / 16;
            server.sendParticles(ParticleTypes.END_ROD, pos.getX()+0.5+Math.cos(a)*radius, pos.getY()+1.05, pos.getZ()+0.5+Math.sin(a)*radius, 1, 0, 0, 0, 0);
        }
        p.getCooldowns().addCooldown(this, 5); return InteractionResultHolder.success(stack);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        var n = settings(stack); lines.add(Component.translatable("tooltip.streamcontrol.mode", Component.translatable("mode.streamcontrol." + Math.floorMod(n.getInt("Mode"), 3))));
        lines.add(Component.translatable("tooltip.streamcontrol.wand")); lines.add(Component.translatable("tooltip.streamcontrol.wand_air"));
    }
}
