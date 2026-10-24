package nl.townsandkingdoms.streamcontrol.mixin;

import dev.streamsreflowing.neoforge.client.StreamParticleScanner;
import net.minecraft.client.multiplayer.ClientLevel;
import nl.townsandkingdoms.streamcontrol.FlowOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Streams Reflowing emits its surface bubbles, leaves, sticks and edge spray
 * independently of fluid physics. Stop the emitter at the precise water cell,
 * rather than disabling its particles globally or relying on its y-less grid.
 */
@Mixin(value = StreamParticleScanner.class, remap = false)
public abstract class SurfaceParticlesMixin {
    @Inject(method = "emitSurfaceAt", at = @At("HEAD"), cancellable = true, remap = false)
    private static void streamcontrol$quietSurface(ClientLevel level, int x, int y, int z,
                                                    byte angle, double drift, boolean debris, CallbackInfo ci) {
        var region = FlowOverrides.clientAt(x, y, z);
        if (region != null && region.still()) ci.cancel();
    }
}
