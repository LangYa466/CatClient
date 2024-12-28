package cn.langya;

import cn.langya.command.CommandManager;
import cn.langya.config.ConfigManager;
import cn.langya.ui.ElementManager;
import cn.langya.event.EventManager;
import cn.langya.module.ModuleManager;
import cn.langya.ui.notification.NotificationManager;
import cn.langya.value.ValueManager;
import lombok.Getter;
import org.lwjgl.opengl.Display;

/**
 * @author LangYa
 * @since 2024/11/16 03:35
 */
@Getter
public class Client {
    @Getter
    private static final Client instance = new Client();

    public static final String name = "CatClient";
    public static final String version = "0.8";

    private final EventManager eventManager = new EventManager();
    private ElementManager elementManager;
    private NotificationManager notificationManager;
    private ModuleManager moduleManager;
    private ValueManager valueManager;
    private ConfigManager configManager;
    private CommandManager commandManager;

    public void initClient() {
        Display.setTitle(String.format("%s - %s",name,version));

        this.elementManager = new ElementManager();
        this.notificationManager = new NotificationManager();
        this.moduleManager = new ModuleManager();
        this.valueManager = new ValueManager();
        this.configManager = new ConfigManager();
        this.commandManager = new CommandManager();
    }

    public void stopClient() {
        this.configManager.saveAllConfig();
    }
}

