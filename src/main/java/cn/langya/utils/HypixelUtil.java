package cn.langya.utils;

import cn.langya.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.List;

/**
 * @author LangYa
 * @since 2024/12/6 14:40
 */
public class HypixelUtil implements Wrapper {
    public static boolean isLobby() {
        if (mc.theWorld == null) {
            return true;
        }

        List<Entity> entities = mc.theWorld.getLoadedEntityList();
        for (Entity entity : entities) {
            if (entity != null && entity.getName().equals("§e§lCLICK TO PLAY")) {
                return true;
            }
        }

        boolean hasNetherStar = false;
        boolean hasCompass = false;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null) {
                if (stack.getItem() == Items.nether_star) {
                    hasNetherStar = true;
                }
                if (stack.getItem() == Items.compass) {
                    hasCompass = true;
                }
                if (hasNetherStar && hasCompass) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasFakeInvisible(EntityLivingBase entity) {
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            if (effect.getPotionID() <= 0)
                continue;

            if (effect.getPotionID() == Potion.invisibility.getId() && !effect.getIsShowParticles()) {
                return true;
            }
        }

        return false;
    }
}
