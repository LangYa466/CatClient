package cn.langya.event.events;

import cn.langya.Wrapper;
import cn.langya.event.impl.Event;
import cn.langya.module.impl.world.ClientRotation;
import lombok.Getter;
import lombok.Setter;

/**
 * @author LangYa
 * @since 2024/11/16 04:17
 */
@Getter
@Setter
public class EventMotion implements Event, Wrapper {
    private double x,y,z;
    private float yaw,pitch;
    private boolean onGround;
    private boolean pre;

    public EventMotion(double x, double y, double z,float yaw,float pitch,boolean onGround,boolean isPre) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.pre = isPre;
    }

    public EventMotion(boolean isPre) {
        this.pre = isPre;
    }

    public boolean isPost() {
        return !pre;
    }

    public void setRotations(float[] targetRotations) {
        float targetYaw = targetRotations[0];
        float targetPitch = targetRotations[1];

        // 丝滑程度
        float smoothFactor = ClientRotation.smoothFactor.getValue();

        // 逐步过渡到目标值
        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;

        float newYaw = smoothTransition(currentYaw, targetYaw, smoothFactor);
        float newPitch = smoothTransition(currentPitch, targetPitch, smoothFactor);

        if (ClientRotation.isEnabled) {
            mc.thePlayer.rotationYaw = newYaw;
            mc.thePlayer.rotationPitch = newPitch;
        } else {
            this.setYaw(newYaw);
            this.setPitch(newPitch);
        }

        mc.thePlayer.renderYawOffset = newYaw;
        mc.thePlayer.rotationYawHead = newYaw;
    }

    // 平滑过渡
    private float smoothTransition(float current, float target, float factor) {
        return current + (target - current) * factor;
    }
}

