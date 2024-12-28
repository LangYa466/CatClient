package cn.langya.ui.notification;

import lombok.Getter;

import java.awt.*;

@Getter
public enum NotificationType {
    SUCCESS(new Color(0, 255, 42, 153)),
    FAILED(new Color(255, 0, 30, 153)),
    WARNING(new Color(255, 251, 0, 153)),
    INIT(new Color(0, 234, 255, 153)),
    INFO(new Color(255, 136, 0,153));

    NotificationType(Color color) {
        this.color = color;
    }

    private final Color color;
}