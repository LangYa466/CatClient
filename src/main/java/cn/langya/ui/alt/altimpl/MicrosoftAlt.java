package cn.langya.ui.alt.altimpl;

import lombok.Getter;
import cn.langya.ui.alt.AccountEnum;
import cn.langya.ui.alt.Alt;

@Getter
public class MicrosoftAlt extends Alt {
    private final String refreshToken;

    public MicrosoftAlt(String userName,String refreshToken) {
        super(userName, AccountEnum.MICROSOFT);
        this.refreshToken = refreshToken;
    }
}
