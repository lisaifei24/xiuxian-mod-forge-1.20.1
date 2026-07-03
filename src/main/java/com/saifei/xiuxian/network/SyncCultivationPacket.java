package com.saifei.xiuxian.network;

import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.capability.Realm;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCultivationPacket {
    private final int current;
    private final int max;
    private final Realm realm;

    // 构造函数包含：当前灵力、最大灵力、当前境界
    public SyncCultivationPacket(int current, int max, Realm realm) {
        this.current = current;
        this.max = max;
        this.realm = realm;
    }

    // 1. 编码：把数据写入字节流
    public static void encode(SyncCultivationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.current);
        buf.writeInt(msg.max);
        // 使用 writeEnum 写入境界枚举 (Forge 1.20.1 标准序列化方式)
        buf.writeEnum(msg.realm);
    }

    // 2. 解码：从字节流读取数据并还原为对象
    public static SyncCultivationPacket decode(FriendlyByteBuf buf) {
        int current = buf.readInt();
        int max = buf.readInt();
        // 按照写入顺序依次读取
        Realm realm = buf.readEnum(Realm.class);
        return new SyncCultivationPacket(current, max, realm);
    }

    // 3. 处理：在客户端主线程更新 Capability
    public static void handle(SyncCultivationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 获取客户端玩家实例
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                    // 从网络包中读取数据并同步到客户端
                    cap.setSpiritualPower(msg.current);
                    cap.setRealm(msg.realm);
                    // 注：因为你在 Cultivation 实现类里 setRealm 时会自动计算 maxSpiritualPower
                    // 所以这里不需要再手动调用 cap.setMaxSpiritualPower(msg.max)，避免逻辑冲突
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}