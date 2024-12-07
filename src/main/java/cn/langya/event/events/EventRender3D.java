package cn.langya.event.events;

import cn.langya.event.impl.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author LangYa
 * @since 2024/11/16 03:51
 */
@Getter
@AllArgsConstructor
public class EventRender3D implements Event {
    private final float partialTicks;
}
