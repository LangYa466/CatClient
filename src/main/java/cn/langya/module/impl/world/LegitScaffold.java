package cn.langya.module.impl.world;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

public class LegitScaffold extends Module {
    public LegitScaffold() {
        super(Category.Move);
    }

    private final TimerUtil delayTimer = new TimerUtil();

    private static Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    private static Block getBlockUnderPlayer(final EntityPlayer player) {
        return getBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ));
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (e.isPre()) {
            if (getBlockUnderPlayer(mc.thePlayer) instanceof BlockAir) {
                if (mc.thePlayer.onGround) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            } else if (mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }

            int slot = ScaffoldUtil.getBlockSlot();
            if (slot == -1) return;
            mc.thePlayer.inventory.currentItem = slot;
        }
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;
        mc.thePlayer.setSneaking(false);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        super.onDisable();
    }
}
