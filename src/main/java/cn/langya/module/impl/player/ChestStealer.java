package cn.langya.module.impl.player;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventRender2D;
import cn.langya.event.events.EventWorldLoad;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.utils.RotationUtil;
import cn.langya.utils.TimerUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChestStealer extends Module {
    private final NumberValue delay = new NumberValue("Delay", 80, 300, 0, 10);
    private final BooleanValue aura = new BooleanValue("Aura", false);
    private final NumberValue auraRange = new NumberValue("Aura Range", 3, 6, 1, 1);
    public static BooleanValue stealingIndicator = new BooleanValue("Stealing Indicator", false);
    public static BooleanValue titleCheck = new BooleanValue("Title Check", true);
    public static BooleanValue freeLook = new BooleanValue("Free Look", true);
    private final BooleanValue reverse = new BooleanValue("Reverse", false);
    public static final BooleanValue silent = new BooleanValue("Silent", false);
    private final BooleanValue smart = new BooleanValue("Smart", false);

    private final List<BlockPos> openedChests = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();
    public static boolean stealing;
    private boolean clear;

    public ChestStealer() {
        super(Category.Player);
    }

    @Override
    public String getSuffix() {
        return String.valueOf(delay.getValue().intValue());
    }

    @EventTarget
    public void onMotionEvent(EventMotion e) {
        if (e.isPre()) {
            if (aura.getValue()) {
                final int radius = auraRange.getValue().intValue();
                for (int x = -radius; x < radius; x++) {
                    for (int y = -radius; y < radius; y++) {
                        for (int z = -radius; z < radius; z++) {
                            final BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                            if (pos.getBlock() == Blocks.chest && !openedChests.contains(pos)) {
                                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), pos, EnumFacing.UP, new Vec3(pos))) {
                                    mc.thePlayer.swingItem();
                                    final float[] rotations = RotationUtil.getFacingRotations(pos.getX(), pos.getY(), pos.getZ());
                                    e.setRotations(rotations);
                                    openedChests.add(pos);
                                }
                            }
                        }
                    }
                }
            }
            if (mc.thePlayer.openContainer instanceof ContainerChest) {
                ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                IInventory chestInv = chest.getLowerChestInventory();
                if (titleCheck.getValue() && (!(chestInv instanceof ContainerLocalMenu) || !((ContainerLocalMenu) chestInv).realChest))
                    return;
                clear = true;

                List<Integer> slots = new ArrayList<>();
                for (int i = 0; i < chestInv.getSizeInventory(); i++) {
                    ItemStack is = chestInv.getStackInSlot(i);
                    if (is != null && (!smart.getValue() || !(InvManager.isBadItem(is, -1, true) || items.contains(is.getItem())))) {
                        slots.add(i);
                    }
                }

                if (reverse.getValue()) {
                    Collections.reverse(slots);
                }

                slots.forEach(s -> {
                    ItemStack is = chestInv.getStackInSlot(s);
                    Item item = is != null ? is.getItem() : null;
                    if (item != null && !items.contains(item) && (delay.getValue() == 0 || timer.hasReached(delay.getValue().intValue()))) {
                        if (smart.getValue() && !(item instanceof ItemBlock)) {
                            items.add(is.getItem());
                        }
                        mc.playerController.windowClick(chest.windowId, s, 0, 1, mc.thePlayer);
                    }
                });

                if (slots.isEmpty() || isInventoryFull()) {
                    items.clear();
                    clear = false;
                    stealing = false;
                    mc.thePlayer.closeScreen();
                }
            } else if (clear) {
                items.clear();
                clear = false;
            }
        }
    }

    @EventTarget
    public void onRender2DEvent(EventRender2D event) {
        if (stealingIndicator.getValue() && stealing) {
            UFontRenderer fr = FontManager.hanYi(20);
            fr.drawStringWithShadow("§lStealing...", event.getScaledresolution().getScaledWidth() / 2.0F - fr.getStringWidth("§lStealing...") / 2.0F, event.getScaledresolution().getScaledHeight() / 2.0F + 10, -1);
        }
    }

    @Override
    public void onEnable() {
        openedChests.clear();
        super.onEnable();
    }

    private boolean isInventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean canSteal() {
        if (Client.getInstance().getModuleManager().getModule("ChestStealer").isEnabled() && mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            IInventory chestInv = chest.getLowerChestInventory();
            return !titleCheck.getValue() || (chestInv instanceof ContainerLocalMenu && ((ContainerLocalMenu) chestInv).realChest);
        }
        return false;
    }

    @EventTarget
    public void onWorldEvent(EventWorldLoad event) {
        openedChests.clear();
    }
}
