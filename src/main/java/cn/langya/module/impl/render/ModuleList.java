package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.ui.Element;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.value.impl.NumberValue;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
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
    private final Element element = Client.getInstance().getElementManager().createElement(getName());

    @EventTarget
    public void onRender2D(EventRender2D event) {
        UFontRenderer fr = FontManager.hanYi(18);
        float width = 0;
        float height = ( fr.FONT_HEIGHT + spacingValue.getValue().intValue() ) * Client.getInstance().getModuleManager().getEnableModules().size();
        // 在循环里面获取浪费性能
        float posX = element.getX();
        float posY = element.getY();

        int index = 0;
        List<String> displayTexts = new ArrayList<>();
        for (Module module : Client.getInstance().getModuleManager().getEnableModules()) {
            String moduleText = module.getSuffix().isEmpty() ? module.getName() : String.format("%s %s%s", module.getName(),EnumChatFormatting.GRAY, module.getSuffix());
            displayTexts.add(moduleText);
        }

        displayTexts.sort(Comparator.comparingInt(fr::getStringWidth));

        for (String moduleText : displayTexts) {
            int moduleTextWidth = fr.getStringWidth(moduleText);
            if (width < moduleTextWidth) width = moduleTextWidth;
            fr.drawStringWithShadow(moduleText,posX,posY + (index * fr.FONT_HEIGHT + spacingValue.getValue().intValue()),-1);
            index++;
        }

        element.setWH(width,height);
    }
}
