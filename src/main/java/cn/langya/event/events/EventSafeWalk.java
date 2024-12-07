package cn.langya.event.events;

import cn.langya.event.impl.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventSafeWalk implements Event {
    private boolean safe;
}
