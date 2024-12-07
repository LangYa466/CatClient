package cn.langya.module.impl.player;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;

public class FastPlace extends Module {
    private final NumberValue ticks = new NumberValue("Ticks", 0, 4, 0, 1);
    private final BooleanValue blocks = new BooleanValue("Blocks", true);
    private final BooleanValue projectiles = new BooleanValue("Projectiles", true);

    public FastPlace() {
        super(Category.Player);
    }

    @EventTarget
    public void onMotionEvent(EventMotion event) {
        if (canFastPlace()) mc.rightClickDelayTimer = Math.min(mc.rightClickDelayTimer, ticks.getValue().intValue());
    }

    @Override
    public String getSuffix() {
        return ticks.getValue().toString();
    }

    @Override
    public void onDisable() {
        mc.rightClickDelayTimer = 4;
        super.onDisable();
    }

    private boolean canFastPlace() {
        if (mc.thePlayer == null || mc.thePlayer.getCurrentEquippedItem() == null || mc.thePlayer.getCurrentEquippedItem().getItem() == null) return false;
        Item heldItem = mc.thePlayer.getCurrentEquippedItem().getItem();
        return (blocks.getValue() && heldItem instanceof ItemBlock) || (projectiles.getValue() && (heldItem instanceof ItemSnowball || heldItem instanceof ItemEgg));
    }
}
