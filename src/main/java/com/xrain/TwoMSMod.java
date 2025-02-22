package com.xrain;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoMSMod implements ModInitializer {
    public static final String MOD_ID = "2msmod1";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private final WorldDisappearManager worldDisappearManager = WorldDisappearManager.getInstance();

    @Override
    public void onInitialize() {
        // 注册实体死亡事件监听器
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                worldDisappearManager.onPlayerKillDragon(player, source);
            }
        });

        // 注册世界tick事件监听器
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            worldDisappearManager.onWorldTick(world);
        });

        LOGGER.info("2msmod1 initialized - World Disappear feature ready!");
    }
}