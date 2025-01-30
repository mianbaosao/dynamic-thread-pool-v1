package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Description: 枚举类
 * @Author:bread
 * @Date: 2024-12-07 16:53
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum AlertEnum {
    DING_DING("DingDing", "钉钉"),
    FEI_SHU("FeiShu", "飞书"),
    QQ("QQ", "邮件");

    private String value;
    private String description;
}
