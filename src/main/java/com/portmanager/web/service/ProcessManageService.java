package com.portmanager.web.service;

import com.portmanager.web.model.ProcessInfo;
import com.portmanager.web.scanner.PortScannerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 进程管理服务
 */
@Service
public class ProcessManageService {

    private static final Logger log = LoggerFactory.getLogger(ProcessManageService.class);

    @Autowired
    private PortScannerFactory portScannerFactory;

    /**
     * 永久停止进程（用于系统管理的服务）
     *
     * @param pid 进程ID
     * @return 是否成功
     */
    public boolean killProcessPermanently(Long pid) {
        if (pid == null || pid <= 0) {
            log.warn("Invalid PID: {}", pid);
            return false;
        }

        try {
            String osType = portScannerFactory.getOsType();

            if ("Windows".equals(osType)) {
                // Windows 系统使用标准方法
                log.info("Windows system, using standard kill method for permanent stop");
                return killProcess(pid);
            }

            // Mac/Linux: 强制使用服务管理方式
            String launchdService = checkLaunchdService(pid);
            if (launchdService != null) {
                log.info("Process {} is managed by launchd service: {}, attempting permanent stop", pid, launchdService);
                boolean stopped = stopLaunchdService(launchdService);

                if (stopped) {
                    log.info("Successfully stopped launchd service permanently: {}", launchdService);
                    return true;
                } else {
                    log.error("Failed to stop launchd service permanently: {}", launchdService);
                    return false;
                }
            } else {
                // 不是 launchd 管理的服务，使用普通 kill 方法
                log.info("Process {} is not managed by launchd, using standard kill", pid);
                return killProcess(pid);
            }
        } catch (Exception e) {
            log.error("Failed to permanently kill process {}: {}", pid, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 关闭指定PID的进程
     *
     * @param pid 进程ID
     * @return 是否成功
     */
    public boolean killProcess(Long pid) {
        if (pid == null || pid <= 0) {
            log.warn("Invalid PID: {}", pid);
            return false;
        }

        try {
            String osType = portScannerFactory.getOsType();

            // 对于 Mac/Linux 系统，先检查是否是 launchd 管理的服务
            if (!"Windows".equals(osType)) {
                String launchdService = checkLaunchdService(pid);
                if (launchdService != null) {
                    log.info("Process {} is managed by launchd service: {}", pid, launchdService);
                    boolean stopped = stopLaunchdService(launchdService);
                    if (stopped) {
                        log.info("Successfully stopped launchd service: {}", launchdService);
                        return true;
                    } else {
                        log.warn("Failed to stop launchd service: {}, will try kill command", launchdService);
                        // 如果停止服务失败，继续尝试 kill 命令
                    }
                }
            }

            // 普通进程的关闭逻辑
            Process process;

            if ("Windows".equals(osType)) {
                // Windows: taskkill /F /PID {pid}
                process = new ProcessBuilder("cmd", "/c",
                        String.format("taskkill /F /PID %d", pid)).start();
            } else {
                // Mac/Linux: kill -9 {pid}
                process = new ProcessBuilder("sh", "-c",
                        String.format("kill -9 %d", pid)).start();
            }

            int exitCode = process.waitFor();
            boolean success = exitCode == 0;

            if (success) {
                log.info("Successfully killed process: {}", pid);
            } else {
                log.warn("Failed to kill process: {}, exit code: {}", pid, exitCode);
                // 读取错误输出
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.warn("Error output: {}", line);
                    }
                }
            }

            return success;
        } catch (Exception e) {
            log.error("Failed to kill process {}: {}", pid, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取进程信息
     *
     * @param pid 进程ID
     * @return 进程信息
     */
    public ProcessInfo getProcessInfo(Long pid) {
        if (pid == null || pid <= 0) {
            return null;
        }

        try {
            String osType = portScannerFactory.getOsType();
            String processName = "";
            String commandLine = "";
            String user = "";

            if ("Windows".equals(osType)) {
                // Windows: tasklist + wmic
                processName = getWindowsProcessName(pid);
                commandLine = getWindowsCommandLine(pid);
            } else {
                // Mac/Linux: ps
                String[] psOutput = getMacProcessInfo(pid);
                if (psOutput != null && psOutput.length >= 2) {
                    user = psOutput[0];
                    commandLine = psOutput[1];
                    processName = extractProcessName(commandLine);
                }
            }

            return ProcessInfo.builder()
                    .pid(pid)
                    .processName(processName)
                    .commandLine(commandLine)
                    .user(user)
                    .build();
        } catch (Exception e) {
            log.error("Failed to get process info for PID {}: {}", pid, e.getMessage());
            return null;
        }
    }

    /**
     * 检查进程是否存在
     */
    public boolean isProcessAlive(Long pid) {
        if (pid == null || pid <= 0) {
            return false;
        }

        try {
            String osType = portScannerFactory.getOsType();
            Process process;

            if ("Windows".equals(osType)) {
                process = new ProcessBuilder("cmd", "/c",
                        String.format("tasklist /FI \"PID eq %d\"", pid)).start();
            } else {
                process = new ProcessBuilder("sh", "-c",
                        String.format("ps -p %d", pid)).start();
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.debug("Failed to check if process {} is alive: {}", pid, e.getMessage());
            return false;
        }
    }

    // ==================== Windows辅助方法 ====================

    private String getWindowsProcessName(Long pid) {
        try {
            Process process = new ProcessBuilder("cmd", "/c",
                    String.format("tasklist /FI \"PID eq %d\" /FO CSV /NH", pid)).start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        return parts[0].replace("\"", "");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get Windows process name for PID {}: {}", pid, e.getMessage());
        }
        return "Unknown";
    }

    private String getWindowsCommandLine(Long pid) {
        try {
            Process process = new ProcessBuilder("cmd", "/c",
                    String.format("wmic process where processid=%d get commandline /format:list", pid)).start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("CommandLine=")) {
                        return line.substring("CommandLine=".length()).trim();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get Windows command line for PID {}: {}", pid, e.getMessage());
        }
        return "";
    }

    // ==================== Mac/Linux辅助方法 ====================

    private String[] getMacProcessInfo(Long pid) {
        try {
            Process process = new ProcessBuilder("sh", "-c",
                    String.format("ps -p %d -o user=,command=", pid)).start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.trim().split("\\s+", 2);
                    if (parts.length >= 2) {
                        return parts; // [user, command]
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get Mac process info for PID {}: {}", pid, e.getMessage());
        }
        return null;
    }

    private String extractProcessName(String commandLine) {
        if (commandLine == null || commandLine.isEmpty()) {
            return "Unknown";
        }

        // 提取第一个空格前的部分作为进程名
        int spaceIndex = commandLine.indexOf(' ');
        String name = spaceIndex > 0 ? commandLine.substring(0, spaceIndex) : commandLine;

        // 如果是路径，提取文件名
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }

        return name;
    }

    // ==================== Launchd 服务管理 ====================

    /**
     * 检查进程是否由 launchd 管理
     *
     * @param pid 进程ID
     * @return launchd 服务名称，如果不是则返回 null
     */
    private String checkLaunchdService(Long pid) {
        try {
            // 首先获取进程信息，用于后续的名称匹配
            String[] processInfo = getMacProcessInfo(pid);
            String processName = null;
            if (processInfo != null && processInfo.length >= 2) {
                String commandLine = processInfo[1];
                processName = extractProcessName(commandLine);
            }

            // 方法1: 通过 PID 直接匹配
            Process process = new ProcessBuilder("sh", "-c",
                    "launchctl list | grep -v '^-'").start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    // 格式: PID StatusCode Label
                    if (parts.length >= 3) {
                        String pidStr = parts[0];
                        String label = parts[2];

                        // 检查 PID 是否匹配
                        if (pidStr.equals(pid.toString())) {
                            log.info("Found launchd service by PID: {}", label);
                            return label;
                        }
                    }
                }
            }

            // 方法2: 如果通过 PID 找不到，尝试通过进程名称匹配
            // 这对于 MySQL 等有多个进程的服务很有用（mysqld_safe 和 mysqld）
            if (processName != null) {
                String serviceName = matchServiceByProcessName(processName);
                if (serviceName != null) {
                    log.info("Found launchd service by process name matching: {} -> {}", processName, serviceName);
                    return serviceName;
                }
            }

        } catch (Exception e) {
            log.debug("Failed to check launchd service for PID {}: {}", pid, e.getMessage());
        }
        return null;
    }

    /**
     * 通过进程名称匹配 launchd 服务
     */
    private String matchServiceByProcessName(String processName) {
        try {
            String lowerProcessName = processName.toLowerCase();

            // 常见服务名称映射
            if (lowerProcessName.contains("mysql")) {
                return "homebrew.mxcl.mysql";
            } else if (lowerProcessName.contains("redis")) {
                return "homebrew.mxcl.redis";
            } else if (lowerProcessName.contains("postgres")) {
                return "homebrew.mxcl.postgresql";
            } else if (lowerProcessName.contains("nginx")) {
                return "homebrew.mxcl.nginx";
            } else if (lowerProcessName.contains("httpd") || lowerProcessName.contains("apache")) {
                return "homebrew.mxcl.httpd";
            } else if (lowerProcessName.contains("mongodb") || lowerProcessName.equals("mongod")) {
                return "homebrew.mxcl.mongodb-community";
            }

            // 如果是其他 Homebrew 服务，尝试构造服务名
            // 查询 launchctl list 看是否存在对应的服务
            Process process = new ProcessBuilder("sh", "-c",
                    String.format("launchctl list | grep -i '%s' | grep homebrew", lowerProcessName)).start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 3) {
                        return parts[2]; // 返回服务名称
                    }
                }
            }

        } catch (Exception e) {
            log.debug("Failed to match service by process name {}: {}", processName, e.getMessage());
        }
        return null;
    }

    /**
     * 停止 launchd 服务
     *
     * @param serviceName 服务名称
     * @return 是否成功
     */
    private boolean stopLaunchdService(String serviceName) {
        try {
            // 尝试使用 brew services stop（如果是 Homebrew 服务）
            if (serviceName.startsWith("homebrew.mxcl.")) {
                String brewServiceName = serviceName.substring("homebrew.mxcl.".length());
                log.info("Attempting to stop Homebrew service: {}", brewServiceName);

                Process brewProcess = new ProcessBuilder("sh", "-c",
                        String.format("brew services stop %s", brewServiceName)).start();

                int brewExitCode = brewProcess.waitFor();
                if (brewExitCode == 0) {
                    log.info("Successfully stopped Homebrew service: {}", brewServiceName);
                    return true;
                } else {
                    log.warn("Failed to stop with brew services, trying launchctl unload");
                }
            }

            // 尝试使用 launchctl stop
            Process stopProcess = new ProcessBuilder("sh", "-c",
                    String.format("launchctl stop %s", serviceName)).start();

            int stopExitCode = stopProcess.waitFor();
            if (stopExitCode == 0) {
                log.info("Successfully stopped service with launchctl stop: {}", serviceName);
                return true;
            }

            // 如果 stop 失败，尝试 unload（移除服务）
            Process unloadProcess = new ProcessBuilder("sh", "-c",
                    String.format("launchctl unload -w ~/Library/LaunchAgents/%s.plist", serviceName)).start();

            int unloadExitCode = unloadProcess.waitFor();
            if (unloadExitCode == 0) {
                log.info("Successfully unloaded service: {}", serviceName);
                return true;
            }

            log.warn("All attempts to stop service {} failed", serviceName);
            return false;

        } catch (Exception e) {
            log.error("Failed to stop launchd service {}: {}", serviceName, e.getMessage(), e);
            return false;
        }
    }
}