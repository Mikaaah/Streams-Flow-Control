package nl.townsandkingdoms.streamcontrol;

import com.google.gson.GsonBuilder;
import dev.streamsreflowing.flow.StreamFlowGrid;
import dev.streamsreflowing.neoforge.fluid.StreamFluids;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Opt-in dedicated-server test; never runs in a player's normal world. */
public final class IntegrationChecks {
    public static void register() {
        if (Boolean.getBoolean("streamcontrol.integrationTest")) {
            NeoForge.EVENT_BUS.addListener(IntegrationChecks::prepare);
            NeoForge.EVENT_BUS.addListener(IntegrationChecks::run);
        }
    }
    private static void prepare(net.neoforged.neoforge.event.server.ServerAboutToStartEvent event) {
        event.getServer().getWorldData().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void run(ServerStartedEvent event) {
        var server = event.getServer(); var level = server.overworld(); var results = new LinkedHashMap<String,Object>();
        var data = ControlData.get(level); var owner = UUID.randomUUID(); var p = new BlockPos(16, 120, 16);
        try {
            level.setBlock(p.below(), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(p, Blocks.WATER.defaultBlockState(), 3);
            var directed = new FlowRegion(101, p, 5, 2, 1, 0, 0.75, false, false, owner, data.next());
            check(data.put(level,directed),"Cannot add directed region");
            check(data.at(21,120,16) != null && data.at(22,120,16) == null,"Radius boundary");
            check(data.at(16,122,16) != null && data.at(16,123,16) == null,"Height boundary");
            Vec3 flow = level.getFluidState(p).getFlow(level,p);
            check(flow.x > 0.7 && Math.abs(flow.z) < 0.01,"Vanilla water did not follow direction: " + flow);
            check(StreamFlowGrid.angleForEntityPush(level,16,120,16) == StreamFlowGrid.encode(1,0),"Vessel direction hook");
            check(StreamFlowGrid.speedForPhysics(level,16,120,16) == 0.75,"Vessel strength hook");
            results.put("vanillaWaterAndVesselFlow",true);
            for(int dx=-4;dx<=4;dx++) if(dx!=0) {
                int top=dx>0?123:117;
                for(int y=116;y<=124;y++) level.setBlock(new BlockPos(16+dx,y,16),
                    y<=top?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
            }
            flow=level.getFluidState(p).getFlow(level,p);
            check(flow.x>0.7 && Math.abs(flow.z)<0.01,"Explicit Flow Wand direction was overridden by automatic downhill correction: "+flow);
            results.put("manualDirectionRetainsPriorityOverDownhillGuard",true);
            level.setBlock(p, StreamFluids.STREAM_BLOCK.get().defaultBlockState().setValue(dev.streamsreflowing.neoforge.fluid.StreamLiquidBlock.FLOW, 4),3);
            flow = level.getFluidState(p).getFlow(level,p);
            check(flow.x > 0.7 && Math.abs(flow.z) < 0.01,"Baked stream direction: " + flow);
            var still = new FlowRegion(102,p,3,2,1,0,1,true,true,owner,data.next());
            data.put(level,still);
            check(data.at(16,120,16).still(),"Still block precedence");
            check(level.getFluidState(p).getFlow(level,p).lengthSqr() < 1e-10,"Stillness did not cancel baked stream flow");
            check(StreamFlowGrid.speedForPhysics(level,16,120,16) == 0,"Stillness did not cancel vessel drift");
            check(FlowOverrides.visualAngle(still) == StreamFlowGrid.encode(0,0),
                "Stillwater client direction was not neutral");
            check(StreamFlowGrid.angleForPhysics(level,16,120,16) == StreamFlowGrid.encode(0,0),
                "Stillwater vessel direction was not neutral");
            check(FlowOverrides.visualAngle(directed) == StreamFlowGrid.encode(1,0),
                "Directed water client direction changed");
            check(data.at(20,120,16).key() == directed.key(),"Stillness leaked beyond radius");
            results.put("bakedWaterStillnessAndOverlap",true);
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
                level.setBlock(p.offset(x,0,z),StreamFluids.STREAM_BLOCK.get().defaultBlockState()
                    .setValue(dev.streamsreflowing.neoforge.fluid.StreamLiquidBlock.FLOW,4),3);
            var boat = new net.minecraft.world.entity.vehicle.Boat(level,16.5,120.2,16.5);
            level.addFreshEntity(boat);
            for (int tick = 0; tick < 20; tick++) boat.tick();
            check(Math.abs(boat.getX()-16.5) < 0.01 && Math.abs(boat.getZ()-16.5) < 0.01,
                "Boat drifted inside still region: " + boat.position());
            boat.discard(); results.put("mooredBoatTwentyTicks",true);
            var reloaded = ControlData.load(data.save(new CompoundTag(),level.registryAccess()),level.registryAccess());
            check(reloaded.at(16,120,16).still() && reloaded.at(20,120,16).strength() == 0.75,"Saved data round trip");
            results.put("savedDataRoundTrip",true);
            // Two pools inside one radius must not become connected just because the region overlaps both.
            BlockPos connectedTest = p.offset(96,0,0);
            level.setBlock(connectedTest, Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(connectedTest.east(), Blocks.WATER.defaultBlockState(), 3);
            level.setBlock(connectedTest.east(2), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(connectedTest.east(3), Blocks.WATER.defaultBlockState(), 3);
            long[] waterMask = ConnectedWater.scan(level, connectedTest, net.minecraft.core.Direction.EAST, 4, 0);
            var connected = new FlowRegion(103, connectedTest, 4, 0, 1, 0, 0.5, true, true,
                owner, data.next(), true, waterMask);
            check(connected.contains(connectedTest.getX()+1,connectedTest.getY(),connectedTest.getZ()),
                "Connected water was not included");
            check(!connected.contains(connectedTest.getX()+3,connectedTest.getY(),connectedTest.getZ()),
                "Separate water pool was incorrectly included");
            data.put(level, connected);
            var connectedReload = ControlData.load(data.save(new CompoundTag(), level.registryAccess()),level.registryAccess());
            check(connectedReload.byKey(103).connectedOnly()
                && !connectedReload.byKey(103).contains(connectedTest.getX()+3, connectedTest.getY(), connectedTest.getZ()),
                "Connected-water mask did not persist");
            results.put("connectedWaterIsolationAndPersistence", true);
            var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
            try {
                var packet = new ControlNetwork.Update(level.dimension().location().toString(),false,directed.save(),0);
                ControlNetwork.Update.CODEC.encode(buf,packet); var decoded = ControlNetwork.Update.CODEC.decode(buf);
                check(FlowRegion.load(decoded.region()).equals(directed),"Network codec round trip");
                buf.clear();
                var edit = new ControlNetwork.Configure(connectedTest.asLong(), 4, 0.5, true,
                    net.minecraft.core.Direction.EAST.get3DDataValue());
                ControlNetwork.Configure.CODEC.encode(buf,edit);
                check(ControlNetwork.Configure.CODEC.decode(buf).equals(edit), "Controller edit codec round trip");
            } finally { buf.release(); }
            results.put("networkCodecRoundTrip",true);
            var nether = server.getLevel(Level.NETHER);
            check(FlowOverrides.at(nether,16,120,16) == null,"Dimension isolation");
            results.put("dimensionIsolation",true);
            ControlConfig.ENABLED.set(false);
            check(FlowOverrides.at(level,16,120,16)==null,"Master disable ignored");
            ControlConfig.ENABLED.set(true);
            results.put("masterSwitch",true);
            data.remove(level,102); check(!data.at(16,120,16).still(),"Removal did not restore underlying region");
            data.remove(level,101); check(data.at(16,120,16)==null,"Removal left stale region index");
            BlockPos block = p.offset(32,0,0);
            level.setBlock(block,StreamControl.ANCHOR.get().defaultBlockState(),3);
            var be = (ControllerEntity)level.getBlockEntity(block); be.refresh();
            check(data.at(block.getX(),block.getY(),block.getZ()).still(),"Anchor registration");
            level.setBlock(block,level.getBlockState(block).setValue(ControllerBlock.POWERED,true),3);be.refresh();
            check(data.byKey(block.asLong())==null,"Redstone disable");
            level.setBlock(block,level.getBlockState(block).setValue(ControllerBlock.POWERED,false),3);be.refresh();
            check(data.byKey(block.asLong())!=null,"Redstone re-enable");
            level.removeBlock(block,false);check(data.byKey(block.asLong())==null,"Breaking controller left ghost region");
            results.put("controllerRedstoneAndRemoval",true);
            check(server.getCommands().getDispatcher().execute("streamcontrol status",server.createCommandSourceStack())==1,"Status command");
            results.put("passed",true);
        } catch (Throwable t) { results.put("passed",false);results.put("error",t.toString()); t.printStackTrace(); }
        finally { data.remove(level,101);data.remove(level,102);data.remove(level,103); }
        try { Files.writeString(FMLPaths.GAMEDIR.get().resolve("streamcontrol-integration-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(results)); }
        catch (Exception e) { e.printStackTrace(); }
        System.out.println("STREAMCONTROL_TEST " + results); server.halt(false);
    }
}
