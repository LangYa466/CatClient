package cn.langya.module.impl.render;

import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.clickgui.ClickGuiScreen;
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

    @Override
    public void onEnable() {
        mc.displayGuiScreen(new ClickGuiScreen());
        setEnable(false);
        super.onEnable();
    }
}
