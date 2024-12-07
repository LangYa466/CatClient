package cn.langya.module.impl.move;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventPacket;
import cn.langya.event.events.EventSlowDown;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.MoveUtil;
import cn.langya.utils.PacketUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class NoSlow extends Module {

    private final ModeValue modeValue = new ModeValue("Mode", "Watchdog", "Vanilla", "Watchdog");
    private final BooleanValue watchDogSwordNoSprintValue = new BooleanValue("Sword No Sprint",false) {
        @Override
        public boolean isHide() {
            return !modeValue.isMode("Watchdog");
        }
    };
    private boolean send;

    public NoSlow() {
        super(Category.Player);
    }

    @Override
    public String getSuffix() {
        return modeValue.getValue();
    }

    @EventTarget
    public void onSlowDownEvent(EventSlowDown event) {
        event.setCancelled();
    }

    @EventTarget
    public void onMotionEvent(EventMotion event) {
        switch (modeValue.getValue()) {
            case "Watchdog":
                if (mc.thePlayer.onGround && mc.thePlayer.isUsingItem() && MoveUtil.isMoving()) {
                    if (event.isPre()) {
                        ItemStack item = mc.thePlayer.getHeldItem();
                        if (MoveUtil.offGroundTicks == 4 && send) {
                            send = false;
                            PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(
                                    new BlockPos(-1, -1, -1),
                                    255, item,
                                    0, 0, 0
                            ));

                        } else if (item != null && mc.thePlayer.isUsingItem() && !(item.getItem() instanceof ItemSword)) {
                            event.setY(event.getY() + 1E-14);
                        } else if (item != null && watchDogSwordNoSprintValue.getValue() && item.getItem() instanceof ItemSword) {
                            mc.thePlayer.setSprinting(false);
                        }
                    }
                }
                break;
        }
    }

    @EventTarget
    public void onSendPacket(EventPacket event) {
        if (!event.isSend()) return;
        switch (modeValue.getValue()) {
            case "Watchdog": {
                if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && !mc.thePlayer.isUsingItem()) {
                    C08PacketPlayerBlockPlacement blockPlacement = (C08PacketPlayerBlockPlacement) event.getPacket();
                    if (mc.thePlayer.getHeldItem() != null && blockPlacement.getPlacedBlockDirection() == 255
                            && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || mc.thePlayer.getHeldItem().getItem() instanceof ItemBow || mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && MoveUtil.offGroundTicks < 2) {
                        if (mc.thePlayer.onGround && !MoveUtil.jumpDown()) {
                            mc.thePlayer.jump();
                        }
                        send = true;
                        event.setCancelled();
                    }
                } else if (event.getPacket() instanceof C07PacketPlayerDigging) {
                    C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.getPacket();
                    if (packet.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                        if (send) {
                            // or get bad packet flag
                            event.setCancelled();
                        }
                        send = false;
                    }
                }
            }
        }
    }
}
