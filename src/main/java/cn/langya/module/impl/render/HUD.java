package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @author LangYa
 * @since 2024/12/6 15:10
 */
public class HUD extends Module {
    public HUD() {
        super(Category.Render);
    }

    public static void renderHUD(ScaledResolution scaledresolution,float partialTicks) {
        if (!Client.getInstance().getModuleManager().getModule("HUD").isEnabled()) return;
        EventRender2D eventRender2D = new EventRender2D(scaledresolution, partialTicks);
        Client.getInstance().getEventManager().call(eventRender2D);
    }
}
