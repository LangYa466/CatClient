package cn.langya.module.impl.world;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.*;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

public class Scaffold extends Module {
    private ScaffoldUtil.BlockCache blockCache;

    public Scaffold() {
        super(Category.Move);
    }

    private final BooleanValue autoPlaceValue = new BooleanValue("AutoPlace",true);
    private final BooleanValue sprintValue = new BooleanValue("Sprint",false);
    private final NumberValue delayValue = new NumberValue("Delay", 0, 2, 0, 0.05F);
    private final TimerUtil delayTimer = new TimerUtil();

    private static Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    private static Block getBlockUnderPlayer(final EntityPlayer player) {
        return getBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ));
    }

    @EventTarget
    public void rotation(EventMotion e) {
        // Setting Block Cache
        blockCache = ScaffoldUtil.getBlockInfo();

        if (blockCache != null) {
            float yaw = RotationUtil.getEnumRotations(blockCache.getFacing());
            RotationUtil.setRotations(new float[]{yaw, 77});
        }
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (e.isPre()) {
            mc.thePlayer.setSprinting(sprintValue.getValue());
            if (getBlockUnderPlayer(mc.thePlayer) instanceof BlockAir) {
                if (mc.thePlayer.onGround) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            } else if (mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }

            if (!autoPlaceValue.getValue() || blockCache == null) return;

            if (delayTimer.hasReached(delayValue.getValue().intValue() * 1000)) {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld,
                        mc.thePlayer.inventory.getStackInSlot(ScaffoldUtil.getBlockSlot()),
                        blockCache.getPosition(), blockCache.getFacing(),
                        ScaffoldUtil.getHypixelVec3(blockCache))) {
                    mc.thePlayer.swingItem();
                }
                delayTimer.reset();
            }
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
