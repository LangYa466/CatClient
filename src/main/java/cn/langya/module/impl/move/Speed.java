package cn.langya.module.impl.move;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventUpdate;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.player.HypixelMotionDisabler;
import cn.langya.utils.MoveUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;

/**
 * @author LangYa, xia-mc
 * @since 2024/12/6 14:25
 */
public class Speed extends Module {
    public Speed() {
        super(Category.Move);
    }

    private final ModeValue modeValue = new ModeValue("Mode","Watchdog7Tick","WatchdogPredict","Watchdog7Tick");
    private final BooleanValue stopOnHurtValue = new BooleanValue("Stop on hurt", true);

    public boolean noLowHop() {
        if (!HypixelMotionDisabler.isDisabled()) return true;
        return stopOnHurtValue.getValue() && mc.thePlayer.hurtTime > 0;
    }

    @Override
    public String getSuffix() {
        return modeValue.getValue();
    }
    
    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!MoveUtil.isMoving()) return;

        switch (modeValue.getValue()) {
            case "WatchdogPredict": {
                if (MoveUtil.offGroundTicks == 0) {
                    if (!MoveUtil.jumpDown()) {
                        MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                        mc.thePlayer.jump();
                    }
                } else if (MoveUtil.getJumpEffect() != 0 || noLowHop()) {
                    return;
                }

                switch (MoveUtil.offGroundTicks) {
                    case 1:
                        mc.thePlayer.motionY = 0.39;
                        break;
                    case 3:
                        mc.thePlayer.motionY -= 0.13;
                        break;
                    case 4:
                        mc.thePlayer.motionY -= 0.2;
                        break;
                }
            }
            break;
            case "Watchdog7Tick": {
                if (MoveUtil.offGroundTicks == 0) {
                    if (!MoveUtil.jumpDown()) {
                        MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                        mc.thePlayer.jump();
                    }
                } else if (MoveUtil.offGroundTicks == 5 && !noLowHop()) {
                    mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
                }
            }
        }
    }
}