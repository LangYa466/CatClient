package cn.langya.module;

import cn.langya.Client;
import cn.langya.Wrapper;
import cn.langya.utils.ChatUtil;
import cn.langya.value.Value;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa
 * @since 2024/11/16 03:40
 */
@Getter
@Setter
public class Module implements Wrapper {
    private final String name;
    private final Category category;
    private String suffix = "";
    // private final String description;
    private boolean enabled;
    private List<Value<?>> values;
    private int keyCode = 114514;

    public Module(Category category) {
        this.name = this.getClass().getSimpleName();
        this.category = category;
        this.values = new ArrayList<>();
    }

    public void setEnabled(boolean enabled) {
        // 赋值需要.this
        this.enabled = enabled;
        // 获取值不需要.this
        if (enabled) {
            onEnable();
            ChatUtil.info(this.name + EnumChatFormatting.GREEN + " Enabled.");
        } else {
            onDisable();
            ChatUtil.info(this.name + EnumChatFormatting.RED + " Disable.");
        }

        Client.getInstance().getEventManager().registerModule(enabled,this);


        if (mc.thePlayer != null) mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.click", 0.5F, enabled ? 0.6F : 0.5F, false);
    }

    public void onEnable() { }
    public void onDisable() { }

    public void toggle() {
        setEnabled(!isEnabled());
    }
}
