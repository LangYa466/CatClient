package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.ui.Element;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.utils.RenderUtil;
import cn.langya.utils.animations.ContinualAnimation;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author LangYa
 * @since 2024/11/16 03:50
 */
public class ModuleList extends Module {

    public ModuleList() {
        super(Category.Render);
    }

    private final NumberValue spacingValue = new NumberValue("Spacing",2,5,0,1);
    private final BooleanValue rainbowColorValue = new BooleanValue("Rainbow Color",true);
    private final BooleanValue importantModules = new BooleanValue("Important", false);
    private final Element element = Client.getInstance().getElementManager().createElement(getName());
    private final ContinualAnimation animation = new ContinualAnimation();
    private final BooleanValue cFontValue = new BooleanValue("ClientFont",true);
    private final FontRenderer fr = cFontValue.getValue() ? FontManager.hanYi(18) : mc.fontRendererObj;

    @EventTarget
    public void onRender2D(EventRender2D event) {
        float width = 0;
        float height = fr.FONT_HEIGHT;
        // 在循环里面获取浪费性能
        float posX = element.getX();
        float posY = element.getY();

        int index = 0;
        List<Module> displayModules = new ArrayList<>(Client.getInstance().getModuleManager().getEnableModules());

        displayModules.sort(Comparator.comparingInt(module -> fr.getStringWidth(module.getDisplayText())));
        // 反转list
        Collections.reverse(displayModules);

        for (Module module : displayModules) {
            if (importantModules.getValue() && module.getCategory() == Category.Render) continue;

            String moduleText = module.getDisplayText();
            int moduleTextWidth = fr.getStringWidth(moduleText);
            if (width < moduleTextWidth) width = moduleTextWidth;
            int color = -1;
            if (rainbowColorValue.getValue()) color = RenderUtil.skyRainbow(index * 50, 0.6f, 1f).getRGB();
            animation.animate(1,25);
            float moduleY = Math.abs(animation.getOutput() * (posY + (index * (fr.FONT_HEIGHT + spacingValue.getValue().intValue()))));

            fr.drawStringWithShadow(moduleText, posX, moduleY, color);

            index++;
        }

        height += (fr.FONT_HEIGHT + spacingValue.getValue().intValue()) * (displayModules.size() - 1);

        element.setWH(width, height);
    }
}
