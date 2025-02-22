package com.xrain;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;

public class WorldDisappearManager {
    private static final int LAYER_DISTANCE = 16; // 每圈层的距离
    private static final int TICKS_PER_LAYER = 20 * 10; // 每圈层清除的时间间隔（10秒）
    private int currentLayer = 0;
    private BlockPos centerPos = new BlockPos(0, 64, 0); // 圆心坐标
    private boolean isActive = false;

    private static WorldDisappearManager INSTANCE;

    private WorldDisappearManager() {}

    public static WorldDisappearManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WorldDisappearManager();
        }
        return INSTANCE;
    }

    public void onPlayerKillDragon(ServerPlayerEntity player, DamageSource source) {
        if (source.getAttacker() instanceof EnderDragonEntity) {
            isActive = true; // 触发消失逻辑
            centerPos = player.getWorld().getSpawnPos(); // 设置圆心为出生点
            player.sendMessage(Text.of("主世界开始消失！"), false);
            2msmod1.LOGGER.info("Dragon defeated! World disappearing process started at " + centerPos);
        }
    }

    public void onWorldTick(ServerWorld world) {
        if (!isActive || world.getRegistryKey() != world.getServer().getOverworld().getRegistryKey()) return;

        // 每 TICKS_PER_LAYER 清除一圈
        if (world.getServer().getTicks() % TICKS_PER_LAYER == 0) {
            clearLayer(world, currentLayer);
            currentLayer++;
        }
    }

    private void clearLayer(ServerWorld world, int layer) {
        int minDistance = layer * LAYER_DISTANCE;
        int maxDistance = (layer + 1) * LAYER_DISTANCE;

        // 为了性能优化，每tick只处理一部分方块
        int blocksPerTick = 1000;
        int processedBlocks = 0;

        for (int x = centerPos.getX() - maxDistance; x <= centerPos.getX() + maxDistance && processedBlocks < blocksPerTick; x++) {
            for (int z = centerPos.getZ() - maxDistance; z <= centerPos.getZ() + maxDistance && processedBlocks < blocksPerTick; z++) {
                double distance = Math.sqrt((x - centerPos.getX()) * (x - centerPos.getX()) + 
                                         (z - centerPos.getZ()) * (z - centerPos.getZ()));
                if (distance >= minDistance && distance < maxDistance) {
                    for (int y = world.getBottomY(); y < world.getTopY(); y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!world.getBlockState(pos).isAir()) {
                            world.setBlockState(pos, Blocks.AIR.getDefaultState());
                            if (world.getRandom().nextFloat() < 0.1f) { // 随机生成粒子，减少粒子数量
                                world.spawnParticles(ParticleTypes.EXPLOSION, 
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    1, 0, 0, 0, 0.1);
                            }
                            processedBlocks++;
                        }
                    }
                }
            }
        }

        if (processedBlocks > 0) {
            2msmod1.LOGGER.info("Cleared " + processedBlocks + " blocks in layer " + layer);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void reset() {
        isActive = false;
        currentLayer = 0;
        centerPos = new BlockPos(0, 64, 0);
    }
}