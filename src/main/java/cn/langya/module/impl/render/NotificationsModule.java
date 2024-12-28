package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.notification.Notification;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.util.List;

/**
 * @author LangYa
 * @since 2024/12/29 03:52
 */
public class NotificationsModule extends Module {
    public NotificationsModule() {
        super("Notifications",Category.Render);
    }

    private static final BooleanValue cFontValue = new BooleanValue("ClientFont",true);
    public static final FontRenderer fr = cFontValue.getValue() ? FontManager.hanYi(18) : mc.fontRendererObj;

    @EventTarget
    public void onRender2D(EventRender2D event) {
        ScaledResolution sr = event.getScaledresolution();
        List<Notification> notificationArrayList = Client.getInstance().getNotificationManager().getNotifications();
        int pre_size = notificationArrayList.size();
        for (int j = 0; j < pre_size; j++) {
            for (int i = 0; i < notificationArrayList.size(); i++) {
                if (notificationArrayList.get(i) != null && notificationArrayList.get(i).isDone()) {
                    notificationArrayList.remove(notificationArrayList.get(i));
                    i--;
                }
            }
        }

        for (int i = 0; i < notificationArrayList.size(); i++) notificationArrayList.get(i).render(sr, i);
    }
}
