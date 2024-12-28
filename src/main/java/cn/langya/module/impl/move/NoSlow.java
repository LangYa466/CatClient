package cn.langya.module.impl.move;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventPacket;
import cn.langya.event.events.EventSlowDown;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.MoveUtil;
import cn.langya.utils.PacketUtil;
import cn.langya.value.impl.ModeValue;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * @author LangYa, xia-mc
 * @since 2024/12/29 05:03
 */
public class NoSlow extends Module {
    public NoSlow() {
        super(Category.Move);
    }

    private final ModeValue modeValue = new ModeValue("Mode","Vanilla","Intave","Vanilla");
    private boolean lastUsingItem = false;

    @Override
    public String getSuffix() {
        return modeValue.getValue();
    }

    @EventTarget
    public void onPreMotion(EventMotion event) {
        switch (modeValue.getValue()) {
            case "Intave": {
                if (!mc.thePlayer.isUsingItem() || mc.thePlayer.getHeldItem() == null) {
                    lastUsingItem = false;
                    return;
                }

                Item item = mc.thePlayer.getHeldItem().getItem();

                if (!MoveUtil.isMoving()) return;
                if (isRest(item) || item instanceof ItemPotion) {
                    if (!lastUsingItem) {
                        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
                    }
                } else {
                    if (item instanceof ItemSword) {
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    }
                }

                lastUsingItem = true;
            }
        }
    }

    @Override
    public void onDisable() {
        lastUsingItem = false;
    }

    public static boolean isRest(Item item) {
        return item instanceof ItemFood || item instanceof ItemPotion;
    }

    @EventTarget
    public void onSlowDown(EventSlowDown event) {
        event.setCancelled(true);
    }
}
