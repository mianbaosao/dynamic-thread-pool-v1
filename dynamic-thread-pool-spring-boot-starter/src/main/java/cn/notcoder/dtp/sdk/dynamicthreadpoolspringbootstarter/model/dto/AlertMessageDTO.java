package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

/**
 * @Description: 告警通知类
 * @Author:bread
 * @Date: 2024-12-07 16:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessageDTO {
    private String message;
    // 存储参数的映射
    private LinkedHashMap<String, String> parameters;
    private String remarks;


    // 构造函数，用于创建AlarmMessageDTO对象
    public static AlertMessageDTO buildAlertMessageDTO(String message,LinkedHashMap<String,String> parameters,String remarks) {
        return new AlertMessageDTO(message,parameters,remarks);
    }

    // 构造函数，用于创建AlarmMessageDTO对象，remarks为空
    public static AlertMessageDTO buildAlarmMessageDTO(String message, String remarks) {
        return AlertMessageDTO.buildAlertMessageDTO(message, new LinkedHashMap<>(), remarks);
    }

    // 构造函数，用于创建AlarmMessageDTO对象，remarks和parameters为空
    public static AlertMessageDTO buildAlertMessageDTO(String message) {
        return AlertMessageDTO.buildAlertMessageDTO(message, new LinkedHashMap<>(), null
        );
    }

    // 添加参数
    public <T> AlertMessageDTO appendParameter(String k, T v) {
        parameters.put(k, v.toString());
        return this;
    }
}
