package cn.langya.module.impl.player;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.combat.LegitAura;
import cn.langya.utils.InventoryUtil;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;

public class AutoTool extends Module {
    private final BooleanValue autoSword = new BooleanValue("AutoSword", true);

    public AutoTool() {
        super(Category.Player);
    }

    @EventTarget
    public void onMotionEvent(EventMotion e) {
        if (e.isPre()) {
            if (mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.isKeyDown()) {
                MovingObjectPosition objectMouseOver = mc.objectMouseOver;
                if (objectMouseOver.entityHit != null) {
                    switchSword();
                } else if (objectMouseOver.getBlockPos() != null) {
                    Block block = mc.theWorld.getBlockState(objectMouseOver.getBlockPos()).getBlock();
                    updateItem(block);
                }
            } else if (LegitAura.target != null) {
                switchSword();
            }
        }
    }

    private void updateItem(Block block) {
        float strength = 1.0F;
        int bestItem = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null) {
                continue;
            }
            float strVsBlock = itemStack.getStrVsBlock(block);
            if (strVsBlock > strength) {
                strength = strVsBlock;
                bestItem = i;
            }
        }
        if (bestItem != -1) {
            mc.thePlayer.inventory.currentItem = bestItem;
        }
    }

    private void switchSword() {
        if (!autoSword.getValue()) return;
        float damage = 1;
        int bestItem = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack is = mc.thePlayer.inventory.mainInventory[i];
            if (is != null && is.getItem() instanceof ItemSword && InventoryUtil.getSwordStrength(is) > damage) {
                damage = InventoryUtil.getSwordStrength(is);
                bestItem = i;
            }
        }
        if (bestItem != -1) {
            mc.thePlayer.inventory.currentItem = bestItem;
        }
    }
}
