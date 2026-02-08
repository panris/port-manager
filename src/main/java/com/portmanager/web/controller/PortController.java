package com.portmanager.web.controller;

import com.portmanager.web.model.PortInfo;
import com.portmanager.web.scanner.PortScannerFactory;
import com.portmanager.web.service.PortScanService;
import com.portmanager.web.service.ProcessManageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 端口管理REST API控制器
 */
@RestController
@RequestMapping("/api")
public class PortController {

    private static final Logger log = LoggerFactory.getLogger(PortController.class);

    @Autowired
    private PortScanService portScanService;

    @Autowired
    private ProcessManageService processManageService;

    @Autowired
    private PortScannerFactory portScannerFactory;

    /**
     * 获取所有端口信息
     */
    @GetMapping("/ports")
    public ResponseEntity<Map<String, Object>> getAllPorts() {
        List<PortInfo> ports = portScanService.getAllPorts();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", ports);
        response.put("count", ports.size());
        response.put("lastScanTime", portScanService.getLastScanTime());
        response.put("osType", portScannerFactory.getOsType());
        return ResponseEntity.ok(response);
    }

    /**
     * 查询指定端口
     */
    @GetMapping("/ports/{port}")
    public ResponseEntity<Map<String, Object>> getPort(@PathVariable Integer port) {
        PortInfo portInfo = portScanService.getPort(port);
        Map<String, Object> response = new HashMap<>();

        if (portInfo != null) {
            response.put("success", true);
            response.put("data", portInfo);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Port not found or not in use");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 搜索端口 (支持端口号/进程名/PID)
     */
    @GetMapping("/ports/search")
    public ResponseEntity<Map<String, Object>> searchPorts(@RequestParam String q) {
        List<PortInfo> ports = portScanService.searchPorts(q);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", ports);
        response.put("count", ports.size());
        response.put("keyword", q);
        return ResponseEntity.ok(response);
    }

    /**
     * 关闭进程
     */
    @DeleteMapping("/process/{pid}")
    public ResponseEntity<Map<String, Object>> killProcess(
            @PathVariable("pid") Long pid,
            @RequestParam(value = "permanent", required = false, defaultValue = "false") Boolean permanent) {
        Map<String, Object> response = new HashMap<>();

        // 检查进程是否存在
        if (!processManageService.isProcessAlive(pid)) {
            response.put("success", false);
            response.put("message", "Process not found or already terminated");
            return ResponseEntity.ok(response);
        }

        // 关闭进程
        boolean success;
        if (permanent) {
            // 永久停止服务
            success = processManageService.killProcessPermanently(pid);
        } else {
            // 临时关闭进程
            success = processManageService.killProcess(pid);
        }

        if (success) {
            response.put("success", true);
            response.put("message", permanent ? "Service stopped permanently" : "Process killed successfully");
            response.put("pid", pid);
            response.put("permanent", permanent);

            // 立即触发一次扫描以更新端口列表
            portScanService.scanAllPorts();
        } else {
            response.put("success", false);
            response.put("message", permanent ?
                    "Failed to stop service. Please check permissions or use command line." :
                    "Failed to kill process. Please check permissions.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 批量关闭进程
     */
    @DeleteMapping("/process/batch")
    public ResponseEntity<Map<String, Object>> batchKillProcesses(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Integer> pids = (List<Integer>) request.get("pids");
            Boolean permanent = (Boolean) request.getOrDefault("permanent", false);

            if (pids == null || pids.isEmpty()) {
                response.put("success", false);
                response.put("message", "No PIDs provided");
                return ResponseEntity.ok(response);
            }

            log.info("Batch killing {} processes, permanent: {}", pids.size(), permanent);

            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Integer pidInt : pids) {
                Long pid = pidInt.longValue();
                Map<String, Object> result = new HashMap<>();
                result.put("pid", pid);

                // 检查进程是否存在
                if (!processManageService.isProcessAlive(pid)) {
                    result.put("success", false);
                    result.put("message", "Process not found or already terminated");
                    failCount++;
                } else {
                    // 关闭进程
                    boolean success;
                    if (permanent) {
                        success = processManageService.killProcessPermanently(pid);
                    } else {
                        success = processManageService.killProcess(pid);
                    }

                    result.put("success", success);
                    result.put("message", success ?
                            (permanent ? "Service stopped permanently" : "Process killed successfully") :
                            "Failed to kill process");

                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                }

                results.add(result);
            }

            response.put("success", true);
            response.put("results", results);
            response.put("total", pids.size());
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            response.put("permanent", permanent);

            log.info("Batch kill completed: {} success, {} failed", successCount, failCount);

            // 立即触发一次扫描以更新端口列表
            portScanService.scanAllPorts();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Batch kill failed", e);
            response.put("success", false);
            response.put("message", "Batch operation failed: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 手动触发扫描
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> triggerScan() {
        List<PortInfo> ports = portScanService.scanAllPorts();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Scan completed");
        response.put("count", ports.size());
        response.put("data", ports);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Integer> stats = portScanService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("osArch", System.getProperty("os.arch"));
        systemInfo.put("osType", portScannerFactory.getOsType());
        systemInfo.put("javaVersion", System.getProperty("java.version"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", systemInfo);
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}