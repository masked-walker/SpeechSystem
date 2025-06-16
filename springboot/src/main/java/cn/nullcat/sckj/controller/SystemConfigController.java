package cn.nullcat.sckj.controller;

import cn.nullcat.sckj.annotation.RequirePermission;
import cn.nullcat.sckj.pojo.DTO.BatchConfigDTO;
import cn.nullcat.sckj.pojo.Result;
import cn.nullcat.sckj.pojo.SystemConfig;
import cn.nullcat.sckj.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system")
@Tag(name = "系统配置")
public class SystemConfigController {
    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取系统配置
     * @return
     */
    @GetMapping("/config")
    @RequirePermission("system:config:view")
    @Operation(summary ="获取系统配置")

    public Result getConfig(@RequestParam String key) {
        SystemConfig systemConfig1 = systemConfigService.getByConfigKey(key);
        return Result.success(systemConfig1);
    }

    /**
     * 获取配置列表
     * @param configType
     * @return
     */
    @GetMapping("/config/list")
    @RequirePermission("system:config:view")
    @Operation(summary ="获取配置列表")
    public Result getConfigList(@RequestParam String configType) {
        List<SystemConfig> systemConfigs = systemConfigService.getConfigList(configType);
        return Result.success(systemConfigs);
    }

    /**
     * 修改系统配置
     * @param systemConfig
     * @return
     */
    @PutMapping("/config")
    @RequirePermission("system:config:edit")
    @Operation(summary ="修改系统配置")
    public Result putConfig(@RequestBody SystemConfig systemConfig) {
        systemConfigService.putConfig(systemConfig);
        return Result.success("修改成功");
    }

    /**
     * 批量获取配置
     * @param dto
     * @return
     */
    @PostMapping("/config/batch")
    @Operation(summary ="批量获取配置")
    @RequirePermission("system:config:view")
    public Result batchPutConfig(@RequestBody BatchConfigDTO dto) {
        List<SystemConfig> systemConfigs = systemConfigService.getByConfigKeys(dto.getKeys());
        return Result.success(systemConfigs);
    }
}
