package nl.townsandkingdoms.streamcontrol.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import nl.townsandkingdoms.streamcontrol.FlowOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Finalize the flow vector seen by vanilla/NeoForge fluid rendering.
 * Streams or another fluid implementation may modify getFlow after our
 * lower-level hooks; the FluidState result is the common read boundary.
 */
@Mixin(FluidState.class)
public abstract class FluidStateFlowMixin {
    @ModifyReturnValue(method = "getFlow", at = @At("RETURN"))
    private Vec3 streamcontrol$finalFlow(Vec3 original, BlockGetter world, BlockPos pos) {
        return FlowOverrides.apply(original, world, pos, (FluidState) (Object) this);
    }
}
