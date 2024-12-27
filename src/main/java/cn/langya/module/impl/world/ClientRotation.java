package cn.langya.module.impl.world;

import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.value.impl.NumberValue;

/**
 * @author LangYa
 * @since 2024/12/27 11:40
 */
public class ClientRotation extends Module {
    public static NumberValue smoothFactor = new NumberValue("Rotation Speed",0.3F,1F,0.1F,0.1F);

    public ClientRotation() {
        super(Category.World);
    }

    public static boolean isEnabled = false;

    @Override
    public void onEnable() {
        isEnabled = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        isEnabled = false;
        super.onDisable();
    }
}
