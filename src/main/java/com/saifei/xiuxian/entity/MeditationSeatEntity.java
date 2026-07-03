package com.saifei.xiuxian.entity;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.capability.Realm;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class MeditationSeatEntity extends Entity {

    private int timer = 0;

    // ✅ 【修复】必须传入 EntityType 和 Level 两个参数
    public MeditationSeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setInvisible(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }

        Entity passenger = this.getPassengers().get(0);
        if (passenger instanceof ServerPlayer player) {
            player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                timer++;

                if (timer >= 1200) {
                    timer = 0;
                }

                if (timer % 20 == 0) {
                    Realm realm = cap.getRealm();
                    int maxRecovery = 0;
                    switch (realm) {
                        case MORTAL: maxRecovery = 5; break;
                        case QI_REFINING: maxRecovery = 50; break;
                        case FOUNDATION: maxRecovery = 100; break;
                        case GOLDEN_CORE: maxRecovery = 200; break;
                    }

                    int recover = player.getRandom().nextInt(maxRecovery + 1);
                    if (recover > 0) {
                        cap.addSpiritualPower(recover);
                        XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                                new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
                    }
                }
            });
        }
    }

    @Override
    protected void defineSynchedData() {}
    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag p_20052_) {}
    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag p_20139_) {}
}