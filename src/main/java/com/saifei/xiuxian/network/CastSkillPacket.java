package com.saifei.xiuxian.network;

import com.saifei.xiuxian.skill.SkillExecutor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 -> 服务端 的“释放技能”请求包。
 * skillName 为空字符串时，由服务端使用玩家当前激活的功法（activeSkill）。
 */
public class CastSkillPacket {

    private final String skillName;

    public CastSkillPacket(String skillName) {
        this.skillName = skillName == null ? "" : skillName;
    }

    public static void encode(CastSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.skillName);
    }

    public static CastSkillPacket decode(FriendlyByteBuf buf) {
        return new CastSkillPacket(buf.readUtf(64));
    }

    public static void handle(CastSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillExecutor.cast(player, msg.skillName);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
