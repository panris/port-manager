package com.portmanager.web.scanner;

/**
 * 端口类型识别工具
 */
public class PortTypeIdentifier {

    /**
     * 识别进程类型
     * @param processName 进程名称
     * @param commandLine 命令行
     * @return 进程类型: JAVA, NODE, PYTHON, WEB_SERVER, DATABASE, IDE, BROWSER, SYSTEM, OTHER
     */
    public static String identifyProcessType(String processName, String commandLine) {
        if (processName == null || processName.isEmpty()) {
            return "OTHER";
        }

        String lowerProcessName = processName.toLowerCase();
        String lowerCommandLine = commandLine != null ? commandLine.toLowerCase() : "";
        String combined = lowerProcessName + " " + lowerCommandLine;

        // Java应用
        if (isJavaProcess(lowerProcessName, combined)) {
            return "JAVA";
        }

        // Node.js应用
        if (isNodeProcess(lowerProcessName, combined)) {
            return "NODE";
        }

        // Python应用
        if (isPythonProcess(lowerProcessName, combined)) {
            return "PYTHON";
        }

        // Web服务器
        if (isWebServerProcess(lowerProcessName, combined)) {
            return "WEB_SERVER";
        }

        // 数据库
        if (isDatabaseProcess(lowerProcessName, combined)) {
            return "DATABASE";
        }

        // IDE
        if (isIDEProcess(lowerProcessName, combined)) {
            return "IDE";
        }

        // 浏览器
        if (isBrowserProcess(lowerProcessName, combined)) {
            return "BROWSER";
        }

        // 系统进程
        if (isSystemProcess(lowerProcessName)) {
            return "SYSTEM";
        }

        return "OTHER";
    }

    /**
     * 判断是否为Java进程
     */
    private static boolean isJavaProcess(String processName, String combined) {
        return processName.equals("java") ||
               processName.contains("java") ||
               combined.contains("spring") ||
               combined.contains("tomcat") ||
               combined.contains("jetty") ||
               combined.contains(".jar");
    }

    /**
     * 判断是否为Node.js进程
     */
    private static boolean isNodeProcess(String processName, String combined) {
        return processName.equals("node") ||
               processName.contains("npm") ||
               processName.contains("yarn") ||
               processName.contains("pnpm") ||
               combined.contains("webpack") ||
               combined.contains("vite") ||
               combined.contains("next") ||
               combined.contains("nuxt");
    }

    /**
     * 判断是否为Python进程
     */
    private static boolean isPythonProcess(String processName, String combined) {
        return processName.equals("python") ||
               processName.equals("python3") ||
               processName.equals("python2") ||
               combined.contains("django") ||
               combined.contains("flask") ||
               combined.contains("fastapi") ||
               combined.contains("uvicorn") ||
               combined.contains("gunicorn");
    }

    /**
     * 判断是否为Web服务器进程
     */
    private static boolean isWebServerProcess(String processName, String combined) {
        return processName.equals("nginx") ||
               processName.equals("httpd") ||
               processName.equals("apache") ||
               processName.equals("apache2") ||
               processName.contains("caddy") ||
               processName.contains("lighttpd");
    }

    /**
     * 判断是否为数据库进程
     */
    private static boolean isDatabaseProcess(String processName, String combined) {
        return processName.contains("mysql") ||
               processName.contains("postgres") ||
               processName.contains("redis") ||
               processName.contains("mongo") ||
               processName.equals("mongod") ||
               processName.contains("oracle") ||
               processName.contains("sqlserver") ||
               processName.contains("mariadb") ||
               processName.contains("clickhouse") ||
               processName.contains("elastic") ||
               processName.contains("cassandra") ||
               processName.contains("influx");
    }

    /**
     * 判断是否为IDE进程
     */
    private static boolean isIDEProcess(String processName, String combined) {
        return processName.contains("idea") ||
               processName.contains("intellij") ||
               processName.contains("pycharm") ||
               processName.contains("webstorm") ||
               processName.contains("vscode") ||
               processName.equals("code") ||
               processName.contains("eclipse") ||
               processName.contains("netbeans") ||
               processName.contains("sublime") ||
               processName.contains("atom") ||
               processName.contains("android studio");
    }

    /**
     * 判断是否为浏览器进程
     */
    private static boolean isBrowserProcess(String processName, String combined) {
        return processName.contains("chrome") ||
               processName.contains("firefox") ||
               processName.contains("safari") ||
               processName.contains("edge") ||
               processName.contains("opera") ||
               processName.contains("brave");
    }

    /**
     * 判断是否为系统进程
     */
    private static boolean isSystemProcess(String processName) {
        return processName.equals("systemd") ||
               processName.equals("launchd") ||
               processName.equals("init") ||
               processName.equals("sshd") ||
               processName.equals("cupsd") ||
               processName.contains("kernel") ||
               processName.equals("cron") ||
               processName.equals("systemd-resolved");
    }

    /**
     * 识别端口类型
     * @param port 端口号
     * @param processName 进程名称
     * @param commandLine 命令行
     * @return 端口类型: FRONTEND, BACKEND, DATABASE, OTHER
     */
    public static String identifyPortType(int port, String processName, String commandLine) {
        String lowerProcessName = processName != null ? processName.toLowerCase() : "";
        String lowerCommandLine = commandLine != null ? commandLine.toLowerCase() : "";
        String combined = lowerProcessName + " " + lowerCommandLine;

        // 前端端口识别
        if (isFrontendPort(port, combined)) {
            return "FRONTEND";
        }

        // 后端端口识别
        if (isBackendPort(port, combined)) {
            return "BACKEND";
        }

        // 数据库端口识别
        if (isDatabasePort(port, combined)) {
            return "DATABASE";
        }

        return "OTHER";
    }

    /**
     * 判断是否为前端端口
     */
    private static boolean isFrontendPort(int port, String combined) {
        // 常见前端开发服务器
        if (combined.contains("node") || combined.contains("npm") || combined.contains("yarn") ||
            combined.contains("webpack") || combined.contains("vite") || combined.contains("react") ||
            combined.contains("vue") || combined.contains("angular") || combined.contains("next") ||
            combined.contains("nuxt") || combined.contains("gatsby")) {
            return true;
        }

        // 常见前端端口号
        if (port == 3000 || port == 3001 || port == 4200 || port == 5173 ||
            port == 8081 || port == 9000 || port == 9090) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为后端端口
     */
    private static boolean isBackendPort(int port, String combined) {
        // Java后端
        if (combined.contains("java") && (combined.contains("spring") || combined.contains("tomcat") ||
            combined.contains("jar") || combined.contains("jetty"))) {
            return true;
        }

        // Python后端
        if (combined.contains("python") && (combined.contains("django") || combined.contains("flask") ||
            combined.contains("fastapi") || combined.contains("uvicorn") || combined.contains("gunicorn"))) {
            return true;
        }

        // Go后端
        if (combined.contains("go") && (combined.contains("gin") || combined.contains("beego") ||
            combined.contains("echo"))) {
            return true;
        }

        // Node.js后端
        if (combined.contains("node") && (combined.contains("express") || combined.contains("koa") ||
            combined.contains("nest") || combined.contains("fastify"))) {
            return true;
        }

        // 常见后端端口号
        if (port == 8080 || port == 8000 || port == 8888 || port == 9527 ||
            port == 7001 || port == 7002 || port == 5000 || port == 80 || port == 443) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为数据库端口
     */
    private static boolean isDatabasePort(int port, String combined) {
        // 进程名称包含数据库关键字
        if (combined.contains("mysql") || combined.contains("postgres") || combined.contains("redis") ||
            combined.contains("mongodb") || combined.contains("oracle") || combined.contains("sqlserver") ||
            combined.contains("mariadb") || combined.contains("clickhouse") || combined.contains("elasticsearch")) {
            return true;
        }

        // 常见数据库端口
        switch (port) {
            case 3306:  // MySQL
            case 5432:  // PostgreSQL
            case 6379:  // Redis
            case 27017: // MongoDB
            case 1521:  // Oracle
            case 1433:  // SQL Server
            case 9200:  // Elasticsearch
            case 9300:  // Elasticsearch cluster
            case 8086:  // InfluxDB
            case 9042:  // Cassandra
            case 33060: // MySQL X Protocol
                return true;
        }

        return false;
    }
}