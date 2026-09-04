package com.saifei.xiuxian.item;

import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.skill.SkillType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 功法卷轴：
 * - 未学会时右键使用 -> 消耗卷轴并习得对应功法（持久化存档）。
 * - 已学会时再次右键 -> 不消耗卷轴，将该功法切换为“当前激活功法”（按 R 键释放）。
 */
public class SkillScrollItem extends Item {

    private final SkillType skill;

    public SkillScrollItem(Properties properties, SkillType skill) {
        super(properties);
        this.skill = skill;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            serverPlayer.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
                if (cap.hasLearnedSkill(skill.name())) {
                    // 已学 → 切换当前功法（不消耗卷轴）
                    cap.setActiveSkill(skill.name());
                    serverPlayer.sendSystemMessage(Component.literal(
                            "§b已将功法【" + skill.getDisplayName() + "】设为当前功法，按 [R] 键释放！"));
                } else {
                    // 未学 → 习得功法并消耗卷轴
                    cap.learnSkill(skill.name());
                    cap.setActiveSkill(skill.name());
                    stack.shrink(1);
                    serverPlayer.sendSystemMessage(Component.literal(
                            "§a你参悟卷轴，习得功法【" + skill.getDisplayName() + "】！按下 [R] 键即可释放（消耗灵力并进入冷却）。"));
                }
            });
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7功法：" + skill.getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7灵力消耗：" + skill.getSpiritualCost() + "，冷却：" + skill.getCooldownSeconds() + " 秒")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7右键参悟习得；已学时再次右键切换为当前功法（R 键释放）")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
