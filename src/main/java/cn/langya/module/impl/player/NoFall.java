package cn.langya.module.impl.player;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.BlockUtil;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public class NoFall extends Module {
    private final ModeValue modeValue = new ModeValue("Mode","WatchdogBlink","WatchdogBlink");
    private final NumberValue minFallDistance = new NumberValue("Minimum fall distance", 3, 0, 8, 0.1F);

    private final Blink blink = new Blink();
    private boolean blinked = false;
    private boolean prevOnGround = false;
    private double fallDistance = 0;

    public NoFall() {
        super(Category.Player);
    }

    @Override
    public void onDisable() {
        blink.setEnabled(false);
        blinked = false;
    }

    @Override
    public String getSuffix() {
        return modeValue.getValue();
    }

    @EventTarget
    public void onPreMotion(EventMotion event) {
        if (modeValue.getValue().equals("WatchdogBlink")) {
            if (mc.thePlayer.onGround) {
                if (blinked) {
                    blink.setEnabled(false);
                    blinked = false;
                }
                this.prevOnGround = mc.thePlayer.onGround;
            } else if (this.prevOnGround) {
                if (shouldBlink()) {
                    blink.setEnabled(true);
                    blinked = true;
                }

                prevOnGround = false;
            } else if (BlockUtil.isBlockUnder() && blink.isEnabled() && (this.fallDistance - mc.thePlayer.motionY) >= minFallDistance.getValue()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                this.fallDistance = 0.0F;
            }
        }
    }

    private boolean shouldBlink() {
        return !mc.thePlayer.onGround && !BlockUtil.isBlockUnder((int) Math.floor(minFallDistance.getValue())) && BlockUtil.isBlockUnder() && !Client.getInstance().getModuleManager().getModule("Scaffold").isEnabled();
    }
}