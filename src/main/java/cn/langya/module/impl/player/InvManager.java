package cn.langya.module.impl.player;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventPacket;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.*;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvManager extends Module {
    private final BooleanValue inventoryPackets = new BooleanValue("Send inventory packets", true);
    private final BooleanValue onlyWhileNotMoving = new BooleanValue("Only while not moving", false);
    private final BooleanValue inventoryOnly = new BooleanValue("Inventory only", false);
    private final BooleanValue swapBlocks = new BooleanValue("Swap blocks", false);
    private static final BooleanValue dropArchery = new BooleanValue("Drop archery", false);
    private final BooleanValue moveArrows = new BooleanValue("Move arrows", true);
    private static final BooleanValue dropFood = new BooleanValue("Drop food", false);
    private static final BooleanValue dropShears = new BooleanValue("Drop shears", true);

    private final NumberValue delay = new NumberValue("Delay", 120, 300, 0, 10);
    private static final NumberValue slotWeapon = new NumberValue("Weapon Slot", 1, 9, 1, 1);
    private static final NumberValue slotPick = new NumberValue("Pickaxe Slot", 2, 9, 1, 1);
    private static final NumberValue slotAxe = new NumberValue("Axe Slot", 3, 9, 1, 1);
    private static final NumberValue slotShovel = new NumberValue("Shovel Slot", 4, 9, 1, 1);
    private static final NumberValue slotBow = new NumberValue("Bow Slot", 5, 9, 1, 1);
    private static final NumberValue slotBlock = new NumberValue("Block Slot", 6, 9, 1, 1);
    private static final NumberValue slotGapple = new NumberValue("Gapple Slot", 7, 9, 1, 1);

    private static final String[] blacklist = {"tnt", "stick", "egg", "string", "cake", "mushroom", "flint", "compass", "dyePowder", "feather", "bucket", "chest", "snow", "fish", "enchant", "exp", "anvil", "torch", "seeds", "leather", "reeds", "skull", "record", "snowball", "piston"};
    private static final String[] serverItems = {"selector", "tracking compass", "(right click)", "tienda ", "perfil", "salir", "shop", "collectibles", "game", "profil", "lobby", "show all", "hub", "friends only", "cofre", "(click", "teleport", "play", "exit", "hide all", "jeux", "gadget", " (activ", "emote", "amis", "bountique", "choisir", "choose ", "recipe book", "click derecho", "todos", "teletransportador", "configuraci", "jugar de nuevo"};
    private static final List<Integer> badPotionIDs = new ArrayList<>(Arrays.asList(Potion.moveSlowdown.getId(), Potion.weakness.getId(), Potion.poison.getId(), Potion.harm.getId()));

    private final TimerUtil timer = new TimerUtil();
    private boolean isInvOpen;

    public InvManager() {
        super(Category.Player);
    }

    @Override
    public String getSuffix() {
        return String.valueOf(delay.getValue().intValue());
    }

    @EventTarget
    public void onMotionEvent(EventMotion e) {
        if (e.isPost() || canContinue()) return;
        if (!mc.thePlayer.isUsingItem() && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiIngameMenu)) {
            if (isReady()) {
                Slot slot = ItemType.WEAPON.getSlot();
                if (!slot.getHasStack() || !isBestWeapon(slot.getStack())) {
                    getBestWeapon();
                }
            }
            getBestPickaxe();
            getBestAxe();
            getBestShovel();
            dropItems();
            swapBlocks();
            getBestBow();
            moveArrows();
            moveFood();
        }
    }

    @EventTarget
    public void onPacketSendEvent(EventPacket e) {
        if (!e.isSend()) return;
        if (isInvOpen) {
            Packet<?> packet = e.getPacket();
            if ((packet instanceof C16PacketClientStatus && ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)
                    || packet instanceof C0DPacketCloseWindow) {
                e.setCancelled();
            } else if (packet instanceof C02PacketUseEntity) {
                fakeClose();
            }
        }
    }

    private boolean isReady() {
        return timer.hasReached(delay.getValue().intValue());
    }

    public static float getDamageScore(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return 0;

        float damage = 0;
        Item item = stack.getItem();

        if (item instanceof ItemSword) {
            damage += ((ItemSword) item).getDamageVsEntity();
        } else if (item instanceof ItemTool) {
            damage += item.getMaxDamage();
        }

        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25F +
                EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.1F;

        return damage;
    }

    public static float getProtScore(ItemStack stack) {
        float prot = 0;
        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) stack.getItem();
            prot += armor.damageReduceAmount + ((100 - armor.damageReduceAmount) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)) * 0.0075F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) / 100F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) / 100F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) / 100F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 25.F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, stack) / 100F;
        }
        return prot;
    }

    private void dropItems() {
        if (!isReady()) return;
        for (int i = 9; i < 45; i++) {
            if (canContinue()) return;
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            ItemStack is = slot.getStack();
            if (is != null && isBadItem(is, i, false)) {
                InventoryUtil.drop(i);
                timer.reset();
                break;
            }
        }
    }

    private static boolean isBestWeapon(ItemStack is) {
        if (is == null) return false;
        float damage = getDamageScore(is);
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is2 = slot.getStack();
                if (getDamageScore(is2) > damage && is2.getItem() instanceof ItemSword) {
                    return false;
                }
            }
        }
        return is.getItem() instanceof ItemSword;
    }

    private void getBestWeapon() {
        for (int i = 9; i < 45; i++) {
            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (is != null && is.getItem() instanceof ItemSword && isBestWeapon(is) && getDamageScore(is) > 0) {
                swap(i, ItemType.WEAPON.getDesiredSlot() - 36);
                break;
            }
        }
    }

    // stealing is true when called from the ChestStealer module because returning true = ignore, but in invmanager returning true = drop
    public static boolean isBadItem(ItemStack stack, int slot, boolean stealing) {
        Item item = stack.getItem();
        String stackName = stack.getDisplayName().toLowerCase(), ulName = item.getUnlocalizedName();
        if (Arrays.stream(serverItems).anyMatch(stackName::contains)) return stealing;

        if (item instanceof ItemBlock) {
            return !BlockUtil.isValidBlock(((ItemBlock) item).getBlock(), true);
        }

        if (stealing) {
            if (isBestWeapon(stack) || isBestAxe(stack) || isBestPickaxe(stack) || isBestBow(stack) || isBestShovel(stack)) {
                return false;
            }
            if (item instanceof ItemArmor) {
                for (int type = 1; type < 5; type++) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(type + 4).getStack();
                    if (is != null) {
                        String typeStr = "";
                        switch (type) {
                            case 1:
                                typeStr = "helmet";
                                break;
                            case 2:
                                typeStr = "chestplate";
                                break;
                            case 3:
                                typeStr = "leggings";
                                break;
                            case 4:
                                typeStr = "boots";
                                break;
                        }
                        if (stack.getUnlocalizedName().contains(typeStr) && getProtScore(is) > getProtScore(stack)) {
                            continue;
                        }
                    }
                    if (isBestArmor(stack, type)) {
                        return false;
                    }
                }
            }
        }

        int weaponSlot = ItemType.WEAPON.getDesiredSlot(), pickaxeSlot = ItemType.PICKAXE.getDesiredSlot(),
                axeSlot = ItemType.AXE.getDesiredSlot(), shovelSlot = ItemType.SHOVEL.getDesiredSlot();

        if (stealing || (slot != weaponSlot || !isBestWeapon(ItemType.WEAPON.getStackInSlot()))
                && (slot != pickaxeSlot || !isBestPickaxe(ItemType.PICKAXE.getStackInSlot()))
                && (slot != axeSlot || !isBestAxe(ItemType.AXE.getStackInSlot()))
                && (slot != shovelSlot || !isBestShovel(ItemType.SHOVEL.getStackInSlot()))) {
            if (!stealing && item instanceof ItemArmor) {
                for (int type = 1; type < 5; type++) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(type + 4).getStack();
                    if (is != null && isBestArmor(is, type)) {
                        continue;
                    }
                    if (isBestArmor(stack, type)) {
                        return false;
                    }
                }
            }

            if ((item == Items.wheat) || item == Items.spawn_egg
                    || (item instanceof ItemFood && dropFood.getValue() && !(item instanceof ItemAppleGold))
                    || (item instanceof ItemPotion && isBadPotion(stack))) {
                return true;
            } else if (!(item instanceof ItemSword) && !(item instanceof ItemTool) && !(item instanceof ItemHoe) && !(item instanceof ItemArmor)) {
                if (dropArchery.getValue() && (item instanceof ItemBow || item == Items.arrow)) {
                    return true;
                } else {
                    return (dropShears.getValue() && ulName.contains("shears")) || item instanceof ItemGlassBottle || Arrays.stream(blacklist).anyMatch(ulName::contains);
                }
            }
            return true;
        }

        return false;
    }

    private void getBestPickaxe() {
        if (!isReady()) return;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                if (isBestPickaxe(is) && !isBestWeapon(is)) {
                    int desiredSlot = ItemType.PICKAXE.getDesiredSlot();
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestPickaxe(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                    }
                }
            }
        }
    }

    private void getBestAxe() {
        if (!isReady()) return;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                if (isBestAxe(is) && !isBestWeapon(is)) {
                    int desiredSlot = ItemType.AXE.getDesiredSlot();
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestAxe(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                        timer.reset();
                    }
                }
            }
        }
    }

    private void getBestShovel() {
        if (!isReady()) return;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                if (isBestShovel(is) && !isBestWeapon(is)) {
                    int desiredSlot = ItemType.SHOVEL.getDesiredSlot();
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestShovel(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                        timer.reset();
                    }
                }
            }
        }
    }

    private void getBestBow() {
        if (!isReady()) return;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                String stackName = is.getDisplayName().toLowerCase();
                if (Arrays.stream(serverItems).anyMatch(stackName::contains) || !(is.getItem() instanceof ItemBow))
                    continue;
                if (isBestBow(is) && !isBestWeapon(is)) {
                    int desiredSlot = ItemType.BOW.getDesiredSlot();
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestBow(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                    }
                }
            }
        }
    }

    private void moveArrows() {
        if (dropArchery.getValue() || !moveArrows.getValue() || !isReady()) return;
        for (int i = 36; i < 45; i++) {
            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (is != null && is.getItem() == Items.arrow) {
                for (int j = 0; j < 36; j++) {
                    if (mc.thePlayer.inventoryContainer.getSlot(j).getStack() == null) {
                        fakeOpen();
                        InventoryUtil.click(i, 0, true);
                        fakeClose();
                        timer.reset();
                        break;
                    }
                }
            }
        }
    }

    private void moveFood() {
        if (dropFood.getValue() || !isReady()) return;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                if (hasMostGapples(is)) {
                    int desiredSlot = ItemType.GAPPLE.getDesiredSlot();
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !hasMostGapples(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                    }
                }
            }
        }
    }

    private boolean hasMostGapples(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemAppleGold)) {
            return false;
        } else {
            int value = stack.stackSize;
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (is.getItem() instanceof ItemAppleGold && is.stackSize > value) {
                        return false;
                    }
                }
            }
            return true;
        }

    }

    private int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            ItemStack is = slot.getStack();
            if (is != null && is.getItem() instanceof ItemBlock && is.stackSize > stack && Arrays.stream(serverItems).noneMatch(is.getDisplayName().toLowerCase()::contains)) {
                stack = is.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private void swapBlocks() {
        if (!swapBlocks.getValue() || !isReady()) return;
        int mostBlocksSlot = getMostBlocks();
        int desiredSlot = ItemType.BLOCK.getDesiredSlot();
        if (mostBlocksSlot != -1 && mostBlocksSlot != desiredSlot) {
            // only switch if the hotbar slot doesn't already have blocks of the same quantity to prevent an inf loop
            Slot dss = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
            ItemStack dsis = dss.getStack();
            if (!(dsis != null && dsis.getItem() instanceof ItemBlock && dsis.stackSize >= mc.thePlayer.inventoryContainer.getSlot(mostBlocksSlot).getStack().stackSize && Arrays.stream(serverItems).noneMatch(dsis.getDisplayName().toLowerCase()::contains))) {
                swap(mostBlocksSlot, desiredSlot - 36);
            }
        }
    }

    private static boolean isBestPickaxe(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        if (!(item instanceof ItemPickaxe)) {
            return false;
        } else {
            float value = getToolScore(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (is.getItem() instanceof ItemPickaxe && getToolScore(is) > value) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static boolean isBestShovel(ItemStack stack) {
        if (stack == null) return false;
        if (!(stack.getItem() instanceof ItemSpade)) {
            return false;
        } else {
            float score = getToolScore(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (is.getItem() instanceof ItemSpade && getToolScore(is) > score) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static boolean isBestAxe(ItemStack stack) {
        if (stack == null) return false;
        if (!(stack.getItem() instanceof ItemAxe)) {
            return false;
        } else {
            float value = getToolScore(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (getToolScore(is) > value && is.getItem() instanceof ItemAxe && !isBestWeapon(is)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static boolean isBestBow(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBow)) {
            return false;
        } else {
            float value = getBowScore(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (getBowScore(is) > value && is.getItem() instanceof ItemBow && !isBestWeapon(stack)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static float getBowScore(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemBow) {
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 0.5F;
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1F;
        }
        return score;
    }

    private static float getToolScore(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            String name = item.getUnlocalizedName().toLowerCase();
            if (item instanceof ItemPickaxe) {
                score = tool.getStrVsBlock(stack, Blocks.stone) - (name.contains("gold") ? 5 : 0);
            } else if (item instanceof ItemSpade) {
                score = tool.getStrVsBlock(stack, Blocks.dirt) - (name.contains("gold") ? 5 : 0);
            } else {
                if (!(item instanceof ItemAxe)) return 1;
                score = tool.getStrVsBlock(stack, Blocks.log) - (name.contains("gold") ? 5 : 0);
            }
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 0.0075F;
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 100F;
        }
        return score;
    }

    private static boolean isBadPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            List<PotionEffect> effects = ((ItemPotion) stack.getItem()).getEffects(stack);
            if (effects != null) {
                for (PotionEffect effect : effects) {
                    if (badPotionIDs.contains(effect.getPotionID())) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private static boolean isBestArmor(ItemStack stack, int type) {
        String typeStr = "";
        switch (type) {
            case 1:
                typeStr = "helmet";
                break;
            case 2:
                typeStr = "chestplate";
                break;
            case 3:
                typeStr = "leggings";
                break;
            case 4:
                typeStr = "boots";
                break;
        }
        if (stack.getUnlocalizedName().contains(typeStr)) {
            float prot = getProtScore(stack);
            for (int i = 5; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (is.getUnlocalizedName().contains(typeStr) && getProtScore(is) > prot) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void fakeOpen() {
        if (!isInvOpen) {
            timer.reset();
            if (!inventoryOnly.getValue() && inventoryPackets.getValue())
                PacketUtil.sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            isInvOpen = true;
        }
    }

    private void fakeClose() {
        if (isInvOpen) {
            if (!inventoryOnly.getValue() && inventoryPackets.getValue())
                PacketUtil.sendPacketNoEvent(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            isInvOpen = false;
        }
    }

    private void swap(int slot, int hSlot) {
        fakeOpen();
        InventoryUtil.swap(slot, hSlot);
        fakeClose();
        timer.reset();
    }

    private boolean canContinue() {
        return (inventoryOnly.getValue() && !(mc.currentScreen instanceof GuiInventory)) || (onlyWhileNotMoving.getValue() && MoveUtil.isMoving());
    }

    @Getter
    @AllArgsConstructor
    private enum ItemType {
        WEAPON(slotWeapon),
        PICKAXE(slotPick),
        AXE(slotAxe),
        SHOVEL(slotShovel),
        BLOCK(slotBlock),
        BOW(slotBow),
        GAPPLE(slotGapple);

        private final NumberValue setting;

        public int getDesiredSlot() {
            return setting.getValue().intValue() + 35;
        }

        public Slot getSlot() {
            return mc.thePlayer.inventoryContainer.getSlot(getDesiredSlot());
        }

        public ItemStack getStackInSlot() {
            return getSlot().getStack();
        }
    }
}
