package cn.langya.command.impl;

import cn.langya.Client;
import cn.langya.command.Command;
import cn.langya.utils.ChatUtil;

/**
 * @author LangYa
 * @since 2024/12/13 02:01
 */
public class CustomUIListCommand extends Command {

    public CustomUIListCommand() {
        super("cuis");
    }

    @Override
    public void run(String[] args) {
        Client.getInstance().getCustomUIManager().getCustomUIMap().values().forEach(customUI -> ChatUtil.log(customUI.getName()));
        super.run(args);
    }
}
