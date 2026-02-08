package com.portmanager.web.scanner;

import com.portmanager.web.model.PortInfo;

import java.util.List;

/**
 * 端口扫描器接口
 */
public interface PortScanner {

    /**
     * 扫描所有端口
     *
     * @return 端口信息列表
     */
    List<PortInfo> scanPorts();

    /**
     * 扫描指定端口
     *
     * @param port 端口号
     * @return 端口信息，如果端口未占用则返回null
     */
    PortInfo scanPort(int port);
}