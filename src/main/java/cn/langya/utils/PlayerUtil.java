package cn.langya.utils;

import cn.langya.Wrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * @author LangYa
 * @since 2024/10/19 01:40
 */
public class PlayerUtil implements Wrapper {
    public static boolean scoreTeam(final EntityPlayer entityPlayer) {
        return mc.thePlayer.isOnSameTeam(entityPlayer);
    }

    public static boolean colorTeam(EntityPlayer entityPlayer) {
        String targetName = entityPlayer.getDisplayName().getFormattedText().replace("\u00a7r", "");
        String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("\u00a7r", "");
        return targetName.startsWith("\u00a7" + clientName.charAt(1));
    }

    public static boolean armorTeam(final EntityPlayer entityPlayer) {
        if (mc.thePlayer.inventory.armorInventory[3] != null && entityPlayer.inventory.armorInventory[3] != null) {
            final ItemStack myHead = mc.thePlayer.inventory.armorInventory[3];
            final ItemArmor myItemArmor = (ItemArmor)myHead.getItem();
            final ItemStack entityHead = entityPlayer.inventory.armorInventory[3];
            final ItemArmor entityItemArmor = (ItemArmor)entityHead.getItem();
            return String.valueOf(entityItemArmor.getColor(entityHead)).equals("10511680") || myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead);
        }
        return false;
    }
}
