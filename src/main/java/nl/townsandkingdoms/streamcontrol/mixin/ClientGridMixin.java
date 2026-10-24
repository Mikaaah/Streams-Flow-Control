package nl.townsandkingdoms.streamcontrol.mixin;
import dev.streamsreflowing.flow.*;
import nl.townsandkingdoms.streamcontrol.FlowOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = ClientFlowGrids.class, remap = false)
public abstract class ClientGridMixin {
    @Inject(method = "angleAt(III)B", at = @At("HEAD"), cancellable = true, remap = false)
    private static void streamcontrol$angle(int x,int y,int z,CallbackInfoReturnable<Byte> cir) {
        var r = FlowOverrides.clientAt(x,y,z); if (r != null) cir.setReturnValue(FlowOverrides.visualAngle(r));
    }
    @Inject(method = "speedAt(III)D", at = @At("HEAD"), cancellable = true, remap = false)
    private static void streamcontrol$speed(int x,int y,int z,CallbackInfoReturnable<Double> cir) {
        var r = FlowOverrides.clientAt(x,y,z); if (r != null) cir.setReturnValue(r.still() ? 0 : r.strength());
    }
}
