package intuitive.netherite;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class SoulChargeClientHandler {

    private static final Map<BlockPos, Boolean> prevSoulChargeState = new HashMap<>();

    public static void register() {
        ClientTickEvents.END_WORLD_TICK.register(SoulChargeClientHandler::onWorldTick);
    }

    private static void onWorldTick(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        BlockPos playerPos = client.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterateOutwards(playerPos, 16, 16, 16)) {
            if (!(world.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity)) continue;

            boolean isSoulCharged = world.getBlockState(pos).get(SoulChargeFurnace.SOUL_CHARGED);
            boolean wasSoulCharged = prevSoulChargeState.getOrDefault(pos.toImmutable(), false);

            if (isSoulCharged && !wasSoulCharged) {
                spawnActivationBurst(world, pos);
            }

            prevSoulChargeState.put(pos.toImmutable(), isSoulCharged);
        }

        prevSoulChargeState.entrySet().removeIf(entry ->
            entry.getKey().getManhattanDistance(playerPos) > 64
        );
    }

    private static void spawnActivationBurst(ClientWorld world, BlockPos pos) {
        var state = world.getBlockState(pos);

        Direction facing = state.contains(Properties.HORIZONTAL_FACING)
            ? state.get(Properties.HORIZONTAL_FACING)
            : Direction.NORTH;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        double fx = cx + facing.getOffsetX() * 0.55;
        double fy = cy;
        double fz = cz + facing.getOffsetZ() * 0.55;

        for (int i = 0; i < 30; i++) {
            double spread = (world.random.nextDouble() - 0.5) * 0.6;
            double riseSpread = world.random.nextDouble() * 0.4;

            double vx = facing.getOffsetX() * 0.3 + (facing.getAxis() == Direction.Axis.Z ? spread : 0);
            double vy = 0.1 + riseSpread;
            double vz = facing.getOffsetZ() * 0.3 + (facing.getAxis() == Direction.Axis.X ? spread : 0);

            world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, fx, fy, fz, vx, vy, vz);
        }

        for (int i = 0; i < 8; i++) {
            double spread = (world.random.nextDouble() - 0.5) * 0.8;
            double vx = facing.getOffsetX() * 0.2 + (facing.getAxis() == Direction.Axis.Z ? spread : 0);
            double vy = 0.15 + world.random.nextDouble() * 0.2;
            double vz = facing.getOffsetZ() * 0.2 + (facing.getAxis() == Direction.Axis.X ? spread : 0);

            world.addParticle(ParticleTypes.SOUL, fx, fy, fz, vx, vy, vz);
        }

        world.playSound(
            cx, cy, cz,
            SoundEvents.BLOCK_SOUL_SAND_PLACE,
            SoundCategory.BLOCKS,
            1.0f,
            0.8f + world.random.nextFloat() * 0.4f,
            false
        );
        world.playSound(
            cx, cy, cz,
            SoundEvents.ENTITY_BLAZE_SHOOT,
            SoundCategory.BLOCKS,
            1.0f,
            0.8f + world.random.nextFloat() * 0.4f,
            false
        );
    }
}