package cn.langya.module.impl.player;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.InventoryUtil;
import cn.langya.utils.MoveUtil;
import cn.langya.utils.TimerUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class AutoArmor extends Module {
    private final NumberValue delay = new NumberValue("Delay", 150, 300, 0, 10);
    private final BooleanValue onlyWhileNotMoving = new BooleanValue("Stop when moving", false);
    private final BooleanValue invOnly = new BooleanValue("Inventory only", false);
    private final TimerUtil timer = new TimerUtil();

    public AutoArmor() {
        super(Category.Player);
    }

    @EventTarget
    public void onMotionEvent(EventMotion e) {
        if (e.isPost()) return;
        if ((invOnly.getValue() && !(mc.currentScreen instanceof GuiInventory)) || (onlyWhileNotMoving.getValue() && MoveUtil.isMoving())) {
            return;
        }
        if (mc.thePlayer.openContainer instanceof ContainerChest) {
            // so it doesn't put on armor immediately after closing a chest
            timer.reset();
        }
        if (timer.hasReached(delay.getValue().intValue())) {
            for (int armorSlot = 5; armorSlot < 9; armorSlot++) {
                if (equipBest(armorSlot)) {
                    timer.reset();
                    break;
                }
            }
        }
    }

    private boolean equipBest(int armorSlot) {
        int equipSlot = -1, currProt = -1;
        ItemArmor currItem = null;
        ItemStack slotStack = mc.thePlayer.inventoryContainer.getSlot(armorSlot).getStack();
        if (slotStack != null && slotStack.getItem() instanceof ItemArmor) {
            currItem = (ItemArmor) slotStack.getItem();
            currProt = currItem.damageReduceAmount
                    + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, mc.thePlayer.inventoryContainer.getSlot(armorSlot).getStack());
        }
        // find best piece
        for (int i = 9; i < 45; i++) {
            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (is != null && is.getItem() instanceof ItemArmor) {
                int prot = ((ItemArmor) is.getItem()).damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, is);
                if ((currItem == null || currProt < prot) && isValidPiece(armorSlot, (ItemArmor) is.getItem())) {
                    currItem = (ItemArmor) is.getItem();
                    equipSlot = i;
                    currProt = prot;
                }
            }
        }
        // equip best piece (if there is a better one)
        if (equipSlot != -1) {
            if (slotStack != null) {
                InventoryUtil.drop(armorSlot);
            } else {
                InventoryUtil.click(equipSlot, 0, true);
            }
            return true;
        }
        return false;
    }

    private boolean isValidPiece(int armorSlot, ItemArmor item) {
        String unlocalizedName = item.getUnlocalizedName();
        return armorSlot == 5 && unlocalizedName.startsWith("item.helmet")
                || armorSlot == 6 && unlocalizedName.startsWith("item.chestplate")
                || armorSlot == 7 && unlocalizedName.startsWith("item.leggings")
                || armorSlot == 8 && unlocalizedName.startsWith("item.boots");
    }
}
