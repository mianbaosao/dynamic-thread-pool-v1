package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum AlarmStrategyEnum {
    DING_DING("DingDing", "钉钉"),
    FEI_SHU("FeiShu", "飞书"),
    QQ("QQ", "邮件");

    private String value;
    private String description;
}
