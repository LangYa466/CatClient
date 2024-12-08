package cn.langya.module.impl.player;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.combat.KillAura;
import cn.langya.utils.BlockUtil;
import cn.langya.utils.MoveUtil;
import cn.langya.utils.PacketUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public class NoFall extends Module {
    private final ModeValue modeValue = new ModeValue("Mode","WatchdogBlink","WatchdogBlink","WatchdogPacket");
    private final NumberValue minFallDistance = new NumberValue("Minimum fall distance", 3, 0, 8, 0.1F);

    private final ModeValue calcateMode = new ModeValue("Calcate mode", "Position", "Position", "Motion") {
        @Override
        public boolean isHide() {
            return !modeValue.isMode("WatchdogPacket");
        }
    };
    private final BooleanValue prediction = new BooleanValue("Prediction", false) {
        @Override
        public boolean isHide() {
            return !modeValue.isMode("WatchdogPacket");
        }
    };
    private final ModeValue packetMode = new ModeValue("Packet mode", "Extra", "Extra", "Edit") {
        @Override
        public boolean isHide() {
            return !modeValue.isMode("WatchdogPacket");
        }
    };
    private final BooleanValue notWhileKillAura = new BooleanValue("Not while killAura", true) {
        @Override
        public boolean isHide() {
            return !modeValue.isMode("WatchdogPacket");
        }
    };


    private final Blink blink = new Blink();
    private boolean blinked = false;
    private boolean prevOnGround = false;
    private double fallDistance = 0;
    private boolean timed = false;

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
        switch (modeValue.getValue()) {
            case "WatchdogBlink": {
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
                break;
            }
            case "WatchdogPacket": {
                if (mc.thePlayer.onGround)
                    fallDistance = 0;
                else {
                    switch (calcateMode.getValue()) {
                        case "Position":
                            fallDistance += (float) Math.max(mc.thePlayer.lastTickPosY - event.getY(), 0);
                            break;
                        case "Motion":
                            fallDistance += (float) Math.max(-mc.thePlayer.motionY, 0);
                            break;
                    }

                    if (prediction.getValue()) {
                        fallDistance -= MoveUtil.predictedMotion(mc.thePlayer.motionY, 1);  // motion should be nev on falling
                    }
                }

                if (fallDistance >= minFallDistance.getValue() && !(notWhileKillAura.getValue() && KillAura.target != null) && !Client.getInstance().getModuleManager().getModule("Scaffold").isEnabled()) {
                    switch (packetMode.getValue()) {
                        case "Extra":
                            mc.timer.timerSpeed = (float) 0.5;
                            timed = true;
                            PacketUtil.sendPacket(new C03PacketPlayer(true));
                            break;
                        case "Edit":
                            event.setOnGround(true);
                            break;
                    }
                    fallDistance = 0;
                } else if (timed) {
                    mc.timer.timerSpeed = 1;
                    timed = false;
                }
            }
        }
    }

    private boolean shouldBlink() {
        return !mc.thePlayer.onGround && !BlockUtil.isBlockUnder((int) Math.floor(minFallDistance.getValue())) && BlockUtil.isBlockUnder() && !Client.getInstance().getModuleManager().getModule("Scaffold").isEnabled();
    }
}