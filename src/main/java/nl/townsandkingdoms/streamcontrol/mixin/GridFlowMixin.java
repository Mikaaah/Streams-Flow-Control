package nl.townsandkingdoms.streamcontrol.mixin;
import dev.streamsreflowing.flow.StreamFlowGrid;
import net.minecraft.world.level.BlockGetter;
import nl.townsandkingdoms.streamcontrol.FlowOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = StreamFlowGrid.class, remap = false)
public abstract class GridFlowMixin {
    @Inject(method = {"angleForPhysics(Lnet/minecraft/world/level/BlockGetter;III)B", "angleForEntityPush(Lnet/minecraft/world/level/BlockGetter;III)B"}, at = @At("HEAD"), cancellable = true, remap = false)
    private static void streamcontrol$angle(BlockGetter level, int x, int y, int z, CallbackInfoReturnable<Byte> cir) {
        var r = FlowOverrides.at(level,x,y,z); if (r != null) cir.setReturnValue(FlowOverrides.visualAngle(r));
    }
    @Inject(method = "speedForPhysics(Lnet/minecraft/world/level/BlockGetter;III)D", at = @At("HEAD"), cancellable = true, remap = false)
    private static void streamcontrol$speed(BlockGetter level, int x, int y, int z, CallbackInfoReturnable<Double> cir) {
        var r = FlowOverrides.at(level,x,y,z); if (r != null) cir.setReturnValue(r.still() ? 0 : r.strength());
    }
}
