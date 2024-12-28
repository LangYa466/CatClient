package cn.langya.module.impl.render;

import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.clickgui.ClickGuiScreen;
import cn.langya.ui.font.FontManager;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

/**
 * @author LangYa
 * @since 2024/12/6 17:23
 */
public class ClickGUI extends Module {
    public ClickGUI() {
        super(Category.Render);
        setKeyCode(Keyboard.KEY_RSHIFT);
    }

    private static final BooleanValue cFontValue = new BooleanValue("ClientFont",true);
    public static final FontRenderer fr = cFontValue.getValue() ? FontManager.hanYi(17) : mc.fontRendererObj;

    @Override
    public void onEnable() {
        mc.displayGuiScreen(new ClickGuiScreen());
        setEnabled(false);
        super.onEnable();
    }
}
