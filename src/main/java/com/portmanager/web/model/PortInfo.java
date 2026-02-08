package com.portmanager.web.model;

/**
 * 端口信息
 */
public class PortInfo {

    private Integer port;
    private String protocol;
    private String status;
    private Long pid;
    private String processName;
    private String processPath;
    private String commandLine;
    private Boolean isDevelopmentProcess;
    private String startTime;
    private String user;
    private String localAddress;
    private String remoteAddress;
    private String portType; // "FRONTEND", "BACKEND", "DATABASE", "OTHER"
    private String processType; // "JAVA", "NODE", "PYTHON", "WEB_SERVER", "DATABASE", "IDE", "BROWSER", "SYSTEM", "OTHER"

    public PortInfo() {
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PortInfo portInfo = new PortInfo();

        public Builder port(Integer port) {
            portInfo.port = port;
            return this;
        }

        public Builder protocol(String protocol) {
            portInfo.protocol = protocol;
            return this;
        }

        public Builder status(String status) {
            portInfo.status = status;
            return this;
        }

        public Builder pid(Long pid) {
            portInfo.pid = pid;
            return this;
        }

        public Builder processName(String processName) {
            portInfo.processName = processName;
            return this;
        }

        public Builder processPath(String processPath) {
            portInfo.processPath = processPath;
            return this;
        }

        public Builder commandLine(String commandLine) {
            portInfo.commandLine = commandLine;
            return this;
        }

        public Builder isDevelopmentProcess(Boolean isDevelopmentProcess) {
            portInfo.isDevelopmentProcess = isDevelopmentProcess;
            return this;
        }

        public Builder startTime(String startTime) {
            portInfo.startTime = startTime;
            return this;
        }

        public Builder user(String user) {
            portInfo.user = user;
            return this;
        }

        public Builder localAddress(String localAddress) {
            portInfo.localAddress = localAddress;
            return this;
        }

        public Builder remoteAddress(String remoteAddress) {
            portInfo.remoteAddress = remoteAddress;
            return this;
        }

        public Builder portType(String portType) {
            portInfo.portType = portType;
            return this;
        }

        public Builder processType(String processType) {
            portInfo.processType = processType;
            return this;
        }

        public PortInfo build() {
            return portInfo;
        }
    }

    // Getters and Setters
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessPath() {
        return processPath;
    }

    public void setProcessPath(String processPath) {
        this.processPath = processPath;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public Boolean getIsDevelopmentProcess() {
        return isDevelopmentProcess;
    }

    public void setIsDevelopmentProcess(Boolean isDevelopmentProcess) {
        this.isDevelopmentProcess = isDevelopmentProcess;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getPortType() {
        return portType;
    }

    public void setPortType(String portType) {
        this.portType = portType;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }
}