package com.example.speechsystem.pojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "会议室评估参数")
public class RoomEvaluationDTO {
    @Schema(description = "开始时间")
    private Date startTime;
    
    @Schema(description = "结束时间")
    private Date endTime;
    
    @Schema(description = "所需人数")
    private Integer requiredCapacity;
    
    @Schema(description = "所需设备")
    private List<String> requiredEquipment;
} 