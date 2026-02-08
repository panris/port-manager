package com.portmanager.web.service;

import com.portmanager.web.model.PortInfo;
import com.portmanager.web.scanner.PortScannerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 端口扫描服务
 */
@Service
public class PortScanService {

    private static final Logger log = LoggerFactory.getLogger(PortScanService.class);

    @Autowired
    private PortScannerFactory portScannerFactory;

    /**
     * 端口信息缓存 (port -> PortInfo)
     */
    private final Map<Integer, PortInfo> portCache = new ConcurrentHashMap<>();

    /**
     * 上次扫描时间
     */
    private long lastScanTime = 0;

    @PostConstruct
    public void init() {
        log.info("Port scan service initialized");
        // 启动时立即扫描一次
        scanAllPorts();
    }

    /**
     * 定时扫描所有端口 (每5秒)
     */
    @Scheduled(fixedDelay = 5000)
    public void scheduledScan() {
        scanAllPorts();
    }

    /**
     * 扫描所有端口
     */
    public List<PortInfo> scanAllPorts() {
        try {
            long startTime = System.currentTimeMillis();
            List<PortInfo> portList = portScannerFactory.getScanner().scanPorts();

            // 更新缓存
            portCache.clear();
            for (PortInfo portInfo : portList) {
                portCache.put(portInfo.getPort(), portInfo);
            }

            lastScanTime = System.currentTimeMillis();
            long duration = lastScanTime - startTime;

            log.debug("Scanned {} ports in {}ms", portList.size(), duration);
            return portList;
        } catch (Exception e) {
            log.error("Failed to scan ports: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取所有端口信息
     */
    public List<PortInfo> getAllPorts() {
        return new ArrayList<>(portCache.values());
    }

    /**
     * 查询指定端口
     */
    public PortInfo getPort(int port) {
        return portCache.get(port);
    }

    /**
     * 搜索端口 (支持端口号、进程名、PID)
     */
    public List<PortInfo> searchPorts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPorts();
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        return portCache.values().stream()
                .filter(portInfo -> matchesKeyword(portInfo, lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * 判断端口信息是否匹配关键字
     */
    private boolean matchesKeyword(PortInfo portInfo, String keyword) {
        // 匹配端口号
        if (String.valueOf(portInfo.getPort()).contains(keyword)) {
            return true;
        }

        // 匹配PID
        if (portInfo.getPid() != null && String.valueOf(portInfo.getPid()).contains(keyword)) {
            return true;
        }

        // 匹配进程名
        if (portInfo.getProcessName() != null &&
                portInfo.getProcessName().toLowerCase().contains(keyword)) {
            return true;
        }

        // 匹配命令行
        if (portInfo.getCommandLine() != null &&
                portInfo.getCommandLine().toLowerCase().contains(keyword)) {
            return true;
        }

        return false;
    }

    /**
     * 获取上次扫描时间
     */
    public long getLastScanTime() {
        return lastScanTime;
    }

    /**
     * 获取端口数量统计
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("total", portCache.size());

        int devProcessCount = (int) portCache.values().stream()
                .filter(p -> p.getIsDevelopmentProcess() != null && p.getIsDevelopmentProcess())
                .count();
        stats.put("developmentProcesses", devProcessCount);

        long tcpCount = portCache.values().stream()
                .filter(p -> "TCP".equalsIgnoreCase(p.getProtocol()))
                .count();
        stats.put("tcp", (int) tcpCount);

        long udpCount = portCache.values().stream()
                .filter(p -> "UDP".equalsIgnoreCase(p.getProtocol()))
                .count();
        stats.put("udp", (int) udpCount);

        return stats;
    }
}