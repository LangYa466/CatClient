package cn.langya.event.events;

import cn.langya.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

/**
 * @author LangYa
 * @since 2024/12/6 14:45
 */
@Getter
@Setter
public class EventPacket extends CancellableEvent {
    private Packet<?> packet;
    private boolean isSend;

    public EventPacket(Packet<?> packet,boolean isSend) {
        this.packet = packet;
        this.isSend = isSend;
    }

    public boolean isRev() {
        return !isSend;
    }
}
