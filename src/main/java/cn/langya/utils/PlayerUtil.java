package cn.langya.utils;

import cn.langya.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

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

    public static boolean overVoid(double posX, double posY, double posZ) {
        for (int i = (int) posY; i > -1; i--) {
            if (!(mc.theWorld.getBlockState(new BlockPos(posX, i, posZ)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlockUnder() {
        if (!(mc.thePlayer.posY < 0.0)) {
            for (int offset = 0; offset < (int) mc.thePlayer.posY + 2; offset += 2) {
                AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0, (-offset), 0.0);
                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean isBlockUnder(int distance) {
        for (int y = (int) mc.thePlayer.posY; y >= (int) mc.thePlayer.posY - distance; --y) {
            if (!(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ)).getBlock() instanceof BlockAir)) {
                return true;
            }
        }

        return false;
    }

    public static Block getBlock(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return getBlock(mc.thePlayer.posX + offsetX, mc.thePlayer.posY + offsetY, mc.thePlayer.posZ + offsetZ);
    }

    public static boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (boundingBox) {
            final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -height, 0);

            return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty();
        } else {
            for (int offset = 0; offset < height; offset++) {
                if (blockRelativeToPlayer(0, -offset, 0).isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }
}
