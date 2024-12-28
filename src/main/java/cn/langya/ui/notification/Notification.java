package cn.langya.ui.notification;

import cn.langya.Wrapper;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.utils.RenderUtil;
import cn.langya.utils.animations.Easing;
import cn.langya.utils.animations.EasingAnimation;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

/**
 * @author yuxiangll,LangYa
 */
@Getter
public class Notification implements Wrapper {
    private final String content;
    private final EasingAnimation animationX, animationY, animationProcess;
    private final NotificationType type;
    private final Color color;
    private final long begin_time, duration;
    private boolean initialized = false;
    private final float height = 20;
    private final UFontRenderer font = FontManager.hanYi(18);

    public Notification(String content, Easing easingX, Easing easingY, long duration, NotificationType type) {
        this.content = content;
        this.animationX = new EasingAnimation(easingX, (long) (duration * 0.2), 0);
        this.animationY = new EasingAnimation(easingY, (long) (duration * 0.2), 0);
        this.animationProcess = new EasingAnimation(Easing.EASE_OUT_QUART, (long) (duration * 0.8), 0);
        this.type = type;
        this.color = type.getColor();
        begin_time = System.currentTimeMillis();
        this.duration = duration;
    }

    public boolean isDone() {
        return System.currentTimeMillis() >= begin_time + duration;
    }

    public void render(ScaledResolution sr, int index) {
        float width = (float) (font.getStringWidth(content) + 5 * 2);
        float targetY = sr.getScaledHeight() - (height + 2) * (index + 3);
        if (!initialized) {
            animationX.setStartValue(sr.getScaledWidth());
            animationY.setStartValue(targetY);
            initialized = true;
        }
        float targetX = sr.getScaledWidth() - width - 2;
        if (System.currentTimeMillis() >= begin_time + duration * 0.8) {
            targetX = sr.getScaledWidth() + 2;
            animationX.setDuration((long) (duration * 0.2));
        }

        float x = (float) animationX.getValue(targetX), y = (float) animationY.getValue(targetY);
        RenderUtil.drawRect((int) x, y, width, height, new Color(0, 0, 0, 70).getRGB());
        RenderUtil.drawRect(x, y, (float) (width * animationProcess.getValue(1)), height, color.getRGB());
        font.drawStringWithShadow(content, (x + 5), (y + (height - font.FONT_HEIGHT) / 2f) - 1, type == null ? 0 : -1);
    }
}