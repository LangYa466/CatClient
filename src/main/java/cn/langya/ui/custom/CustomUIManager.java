package cn.langya.ui.custom;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.utils.ChatUtil;
import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa
 * @since 2024/12/13 01:46
 */
@Getter
public class CustomUIManager {
    public static final CustomUIManager INSTANCE = new CustomUIManager();

    private final Map<String,CustomUI> customUIMap;
    private final Map<String, Color> colorMap;

    public CustomUIManager() {
        this.customUIMap = new HashMap<>();
        this.colorMap = new HashMap<>();
    }

    public CustomUI addCustomUIWithString(String customUIName,Color color,String displayText) {
        CustomUI customUI = new CustomUI(customUIName, color, UIType.DrawString);
        customUI.setDisplayText(customUIName);
        customUIMap.put(customUI.getName(), customUI);
        return customUI;
    }

    public CustomUI addCustomUIWithRect(String customUIName, Color color, int[] wh) {
        if (wh.length != 2) {
            ChatUtil.log("sb 你没填高度 宽度");
            return null;
        }
        CustomUI customUI = new CustomUI(customUIName, color, UIType.DrawRect);
        customUI.getElement().setWH(wh[0], wh[1]);
        customUIMap.put(customUI.getName(), customUI);
        return customUI;
    }

    public CustomUI getCustomUI(String name) {
        return customUIMap.get(name);
    }

    private void putColors() {
        colorMap.put("red", Color.RED);
        colorMap.put("blue", Color.BLUE);
        colorMap.put("green", Color.GREEN);
        colorMap.put("yellow", Color.YELLOW);
        colorMap.put("black", Color.BLACK);
        colorMap.put("white", Color.WHITE);
        colorMap.put("gray", Color.GRAY);
        colorMap.put("pink", Color.PINK);
        colorMap.put("orange", Color.ORANGE);
        colorMap.put("cyan", Color.CYAN);
        colorMap.put("magenta", Color.MAGENTA);
    }

    public Color parseColor(String colorName) {
        if (colorName == null || colorName.trim().isEmpty()) {
            ChatUtil.log("输入为空，返回默认颜色 black");
            return Color.BLACK;
        }

        Color color1 = colorMap.get(colorName);
        if (color1 != null) {
            return color1;
        } else {
            ChatUtil.log("没找到这个颜色，返回默认颜色 black");
            return Color.BLACK;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        customUIMap.values().forEach(CustomUI::draw);
    }
}


