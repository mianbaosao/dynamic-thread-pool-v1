package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmMessageDTO {
    private String message;
    // 存储参数的映射
    private LinkedHashMap<String, String> parameters;
    private String remarks;

    // 构造函数，用于创建AlarmMessageDTO对象
    public static AlarmMessageDTO buildAlarmMessageDTO(
            String message,
            LinkedHashMap<String, String > parameters,
            String remarks
    ) {
        return new AlarmMessageDTO(
                message,
                parameters,
                remarks
        );
    }

    // 构造函数，用于创建AlarmMessageDTO对象，remarks为空
    public static AlarmMessageDTO buildAlarmMessageDTO(
            String message,
            String remarks
    ) {
        return AlarmMessageDTO.buildAlarmMessageDTO(
                message,
                new LinkedHashMap<>(),
                remarks
        );
    }

    // 构造函数，用于创建AlarmMessageDTO对象，remarks和parameters为空
    public static AlarmMessageDTO buildAlarmMessageDTO(
            String message
    ) {
        return AlarmMessageDTO.buildAlarmMessageDTO(
                message,
                new LinkedHashMap<>(),
                null
        );
    }

    // 添加参数
    public <T> AlarmMessageDTO appendParameter(String k, T v) {
        parameters.put(
                k,
                v.toString()
        );
        return this;
    }
}
