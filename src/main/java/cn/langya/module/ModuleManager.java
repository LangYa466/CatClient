package cn.langya.module;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventKeyInput;
import cn.langya.utils.InitializerUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LangYa
 * @since 2024/11/16 03:46
 */
@Getter
public class ModuleManager {
    private final Map<String,Module> moduleMap;

    public ModuleManager() {
        this.moduleMap = new HashMap<>();

        init();
    }

    public ArrayList<Module> getEnableModules() {
        // 第一次想试试看Stream 有更好的写法就是for moduleMap.values() 接下来流程照常弄
        /*
        Stream<Module> moduleStream = moduleMap.values().stream().filter(Module::isEnable);
        ArrayList<Module> enableModules = new ArrayList<>();
        for (Object module : moduleStream.toArray()) {
            enableModules.add((Module) module);
        }
        return enableModules;

         */
        // chatgpt
        return moduleMap.values().stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Module getModule(String moduleName) {
        // 简单写法
        /*
        for (Module module : moduleMap.values()) {
            if (module.getName() == moduleName) return module;
        }
        return null;

         */
        // stream才是真神
        return moduleMap.values().stream()
                .filter(module -> module.getName().equals(moduleName))
                .findFirst()
                .orElse(null);
    }

    public List<Module> getModulesWithCategory(Category category) {
        List<Module> modules = new ArrayList<>();
        for (Module module : moduleMap.values()) {
            if (module.getCategory() == category) modules.add(module);
        }
        return modules;
    }

    public void addModule(Class<? extends Module> module) throws InstantiationException, IllegalAccessException {
        this.moduleMap.put(module.getSimpleName(), module.newInstance());
    }

    public void init() {
        InitializerUtil.initialize(clazz -> {
            if (!InitializerUtil.check(Module.class,clazz)) return;
            try {
                addModule((Class<? extends Module>) clazz);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }, this.getClass());

        Client.getInstance().getEventManager().register(this);
    }

    @EventTarget
    public void onKeyInput(EventKeyInput event) {
        for (Module module : moduleMap.values()) {
            if (module.getKeyCode() == event.getKeyCode()) module.toggle();
        }
    }
}
