package cn.langya.ui.custom;

import cn.langya.Client;
import cn.langya.ui.Element;
import cn.langya.ui.font.FontManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

/**
 * @author LangYa
 * @since 2024/12/13 01:46
 */
@Getter
@Setter
public class CustomUI {
    private final String name;
    private final Color color;
    private final Element element;
    private final UIType uiType;
    private String displayText;

    public CustomUI(String name, Color color, UIType uiType) {
        this.name = name;
        this.color = color;
        this.uiType = uiType;
        this.element = Client.getInstance().getElementManager().createElement(name);
        this.element.setCustomUI(true);
    }

    public Element draw() {
        switch (uiType) {
            case DrawString: {
                FontManager.hanYi().drawStringWithShadow(displayText, element.getX(), element.getY(),color.getRGB());
                break;
            }
            case DrawRect: {
                Gui.drawRect(element.getX(), element.getY(), element.getX() + element.getWidth(), element.getY() + element.getHeight(), color.getRGB());
                break;
            }
        }
        return element;
    }
}
