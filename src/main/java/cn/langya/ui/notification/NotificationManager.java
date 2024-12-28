package cn.langya.ui.notification;

import cn.langya.utils.animations.Easing;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author LangYa
 * @since 2024/12/29 03:57
 */
@Getter
@Setter
public class NotificationManager {
    public NotificationManager() {
        this.notifications = new CopyOnWriteArrayList<>();
    }

    private final List<Notification> notifications;

    public void post(String title,NotificationType notificationType) {
        this.notifications.add(new Notification(title, Easing.EASE_IN_OUT_QUAD,
                Easing.EASE_IN_OUT_QUAD, 2500, notificationType));
    }
}
