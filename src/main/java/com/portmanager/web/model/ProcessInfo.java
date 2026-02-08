package com.portmanager.web.model;

/**
 * 进程信息
 */
public class ProcessInfo {

    private Long pid;
    private String processName;
    private String processPath;
    private String commandLine;
    private Boolean isDevelopmentProcess;
    private String startTime;
    private String user;

    public ProcessInfo() {
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ProcessInfo processInfo = new ProcessInfo();

        public Builder pid(Long pid) {
            processInfo.pid = pid;
            return this;
        }

        public Builder processName(String processName) {
            processInfo.processName = processName;
            return this;
        }

        public Builder processPath(String processPath) {
            processInfo.processPath = processPath;
            return this;
        }

        public Builder commandLine(String commandLine) {
            processInfo.commandLine = commandLine;
            return this;
        }

        public Builder isDevelopmentProcess(Boolean isDevelopmentProcess) {
            processInfo.isDevelopmentProcess = isDevelopmentProcess;
            return this;
        }

        public Builder startTime(String startTime) {
            processInfo.startTime = startTime;
            return this;
        }

        public Builder user(String user) {
            processInfo.user = user;
            return this;
        }

        public ProcessInfo build() {
            return processInfo;
        }
    }

    // Getters and Setters
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
}