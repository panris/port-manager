package com.portmanager.web.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 端口扫描器工厂类
 * 根据操作系统自动选择对应的扫描器
 */
@Component
public class PortScannerFactory {

    private static final Logger log = LoggerFactory.getLogger(PortScannerFactory.class);

    @Autowired
    private MacPortScanner macPortScanner;

    @Autowired
    private WindowsPortScanner windowsPortScanner;

    private PortScanner portScanner;

    @PostConstruct
    public void init() {
        String osName = System.getProperty("os.name").toLowerCase();
        log.info("Detected operating system: {}", osName);

        if (osName.contains("mac") || osName.contains("darwin")) {
            portScanner = macPortScanner;
            log.info("Using Mac port scanner");
        } else if (osName.contains("win")) {
            portScanner = windowsPortScanner;
            log.info("Using Windows port scanner");
        } else {
            // 默认尝试使用Mac扫描器 (Linux通常也支持lsof)
            portScanner = macPortScanner;
            log.warn("Unknown OS, using Mac port scanner as default");
        }
    }

    /**
     * 获取当前操作系统的端口扫描器
     */
    public PortScanner getScanner() {
        return portScanner;
    }

    /**
     * 获取操作系统类型
     */
    public String getOsType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac") || osName.contains("darwin")) {
            return "Mac";
        } else if (osName.contains("win")) {
            return "Windows";
        } else if (osName.contains("nix") || osName.contains("nux")) {
            return "Linux";
        }
        return "Unknown";
    }
}