// Port Manager Web Application JavaScript

// 全局状态
const state = {
    ports: [],
    filteredPorts: [],
    selectedPorts: new Set(), // 选中的端口
    autoRefresh: true,
    autoRefreshInterval: null,
    currentTheme: 'light',
    lastScanTime: 0,
    filters: {
        portType: '',
        processType: '',
        protocol: '',
        devProcess: '',
        commonPort: ''
    }
};

// DOM元素
const elements = {
    searchInput: null,
    searchBtn: null,
    refreshBtn: null,
    autoRefreshToggle: null,
    themeToggle: null,
    portTableBody: null,
    osType: null,
    portCount: null,
    statTotal: null,
    statDev: null,
    statTcp: null,
    statUdp: null,
    lastScan: null,
    statusMessage: null,
    autoRefreshStatus: null,
    confirmDialog: null,
    confirmMessage: null,
    confirmYes: null,
    confirmNo: null,
    portTypeFilter: null,
    processTypeFilter: null,
    protocolFilter: null,
    devProcessFilter: null,
    commonPortFilter: null,
    clearFiltersBtn: null,
    systemServiceOptions: null,
    optionTemporary: null,
    optionPermanent: null,
    selectAll: null,
    batchKillBtn: null,
    batchKillText: null,
    batchConfirmDialog: null,
    batchConfirmMessage: null,
    batchConfirmYes: null,
    batchConfirmNo: null,
    batchPortList: null,
    batchSystemServiceOptions: null,
    batchOptionTemporary: null,
    batchOptionPermanent: null
};

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    initElements();
    initEventListeners();
    initTheme();
    loadPorts();
    startAutoRefresh();
});

// 初始化DOM元素
function initElements() {
    elements.searchInput = document.getElementById('searchInput');
    elements.searchBtn = document.getElementById('searchBtn');
    elements.refreshBtn = document.getElementById('refreshBtn');
    elements.autoRefreshToggle = document.getElementById('autoRefreshToggle');
    elements.themeToggle = document.getElementById('themeToggle');
    elements.portTableBody = document.getElementById('portTableBody');
    elements.osType = document.getElementById('osType');
    elements.portCount = document.getElementById('portCount');
    elements.statTotal = document.getElementById('statTotal');
    elements.statDev = document.getElementById('statDev');
    elements.statTcp = document.getElementById('statTcp');
    elements.statUdp = document.getElementById('statUdp');
    elements.lastScan = document.getElementById('lastScan');
    elements.statusMessage = document.getElementById('statusMessage');
    elements.autoRefreshStatus = document.getElementById('autoRefreshStatus');
    elements.confirmDialog = document.getElementById('confirmDialog');
    elements.confirmMessage = document.getElementById('confirmMessage');
    elements.confirmYes = document.getElementById('confirmYes');
    elements.confirmNo = document.getElementById('confirmNo');
    elements.portTypeFilter = document.getElementById('portTypeFilter');
    elements.processTypeFilter = document.getElementById('processTypeFilter');
    elements.protocolFilter = document.getElementById('protocolFilter');
    elements.devProcessFilter = document.getElementById('devProcessFilter');
    elements.commonPortFilter = document.getElementById('commonPortFilter');
    elements.clearFiltersBtn = document.getElementById('clearFiltersBtn');
    elements.systemServiceOptions = document.getElementById('systemServiceOptions');
    elements.optionTemporary = document.getElementById('optionTemporary');
    elements.optionPermanent = document.getElementById('optionPermanent');
    elements.selectAll = document.getElementById('selectAll');
    elements.batchKillBtn = document.getElementById('batchKillBtn');
    elements.batchKillText = document.getElementById('batchKillText');
    elements.batchConfirmDialog = document.getElementById('batchConfirmDialog');
    elements.batchConfirmMessage = document.getElementById('batchConfirmMessage');
    elements.batchConfirmYes = document.getElementById('batchConfirmYes');
    elements.batchConfirmNo = document.getElementById('batchConfirmNo');
    elements.batchPortList = document.getElementById('batchPortList');
    elements.batchSystemServiceOptions = document.getElementById('batchSystemServiceOptions');
    elements.batchOptionTemporary = document.getElementById('batchOptionTemporary');
    elements.batchOptionPermanent = document.getElementById('batchOptionPermanent');
}

// 初始化事件监听
function initEventListeners() {
    elements.searchBtn.addEventListener('click', handleSearch);
    elements.searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleSearch();
    });
    elements.refreshBtn.addEventListener('click', handleRefresh);
    elements.autoRefreshToggle.addEventListener('click', toggleAutoRefresh);
    elements.themeToggle.addEventListener('click', toggleTheme);
    elements.confirmNo.addEventListener('click', hideConfirmDialog);

    // 筛选器事件监听
    elements.portTypeFilter.addEventListener('change', handleFilterChange);
    elements.processTypeFilter.addEventListener('change', handleFilterChange);
    elements.protocolFilter.addEventListener('change', handleFilterChange);
    elements.devProcessFilter.addEventListener('change', handleFilterChange);
    elements.commonPortFilter.addEventListener('change', handleCommonPortSelect);
    elements.clearFiltersBtn.addEventListener('click', clearAllFilters);

    // 批量操作事件监听
    elements.selectAll.addEventListener('change', handleSelectAll);
    elements.batchKillBtn.addEventListener('click', showBatchKillConfirm);
    elements.batchConfirmNo.addEventListener('click', hideBatchConfirmDialog);
}

// 初始化主题
function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    state.currentTheme = savedTheme;
    document.body.setAttribute('data-theme', savedTheme);
}

// 切换主题
function toggleTheme() {
    state.currentTheme = state.currentTheme === 'light' ? 'dark' : 'light';
    document.body.setAttribute('data-theme', state.currentTheme);
    localStorage.setItem('theme', state.currentTheme);
}

// 加载端口信息
async function loadPorts() {
    try {
        showStatus('正在加载端口信息...', 'info');
        const response = await fetch('/api/ports');
        const result = await response.json();

        if (result.success) {
            state.ports = result.data || [];
            state.lastScanTime = result.lastScanTime;
            applyFilters();
            updateUI();
            showStatus(`加载成功: ${state.ports.length} 个端口`, 'success');

            // 更新操作系统类型
            if (result.osType) {
                elements.osType.textContent = result.osType;
            }
        } else {
            showStatus('加载失败: ' + (result.message || '未知错误'), 'error');
        }
    } catch (error) {
        console.error('Failed to load ports:', error);
        showStatus('加载失败: 网络错误', 'error');
    }
}

// 搜索端口
async function handleSearch() {
    const keyword = elements.searchInput.value.trim();

    if (!keyword) {
        // 如果搜索框为空，重新加载全部数据并应用筛选
        loadPorts();
        return;
    }

    try {
        showStatus('正在搜索...', 'info');
        const response = await fetch(`/api/ports/search?q=${encodeURIComponent(keyword)}`);
        const result = await response.json();

        if (result.success) {
            state.ports = result.data || [];
            applyFilters();
            updateUI();
            showStatus(`找到 ${state.filteredPorts.length} 个匹配结果`, 'success');
        } else {
            showStatus('搜索失败: ' + (result.message || '未知错误'), 'error');
        }
    } catch (error) {
        console.error('Failed to search:', error);
        showStatus('搜索失败: 网络错误', 'error');
    }
}

// 手动刷新
async function handleRefresh() {
    try {
        showStatus('正在刷新...', 'info');
        const response = await fetch('/api/scan', { method: 'POST' });
        const result = await response.json();

        if (result.success) {
            state.ports = result.data || [];
            applyFilters();
            updateUI();
            showStatus(`刷新成功: ${state.ports.length} 个端口`, 'success');
        } else {
            showStatus('刷新失败: ' + (result.message || '未知错误'), 'error');
        }
    } catch (error) {
        console.error('Failed to refresh:', error);
        showStatus('刷新失败: 网络错误', 'error');
    }
}

// 关闭进程
function showKillConfirm(pid, processName, port, processType) {
    let warningMessage = '';
    let isSystemManaged = false;

    // 根据进程类型添加警告信息并判断是否是系统管理的服务
    if (processType === 'DATABASE') {
        isSystemManaged = true;
        warningMessage = '\n\n⚠️ 警告：这是一个数据库服务，可能由系统服务管理器（如 launchd/Homebrew）自动启动。';
    } else if (processType === 'SYSTEM') {
        isSystemManaged = true;
        warningMessage = '\n\n⚠️ 警告：这是一个系统进程，关闭可能影响系统稳定性！';
    } else if (processType === 'WEB_SERVER') {
        isSystemManaged = true;
        warningMessage = '\n\n⚠️ 提示：这是一个 Web 服务器，可能由系统服务管理器管理。';
    }

    elements.confirmMessage.textContent =
        `确定要关闭进程 "${processName}" (PID: ${pid}, Port: ${port}) 吗？${warningMessage}`;

    // 根据是否是系统管理的服务显示选项
    if (isSystemManaged) {
        elements.systemServiceOptions.style.display = 'block';
        elements.optionTemporary.checked = false;
        elements.optionPermanent.checked = true; // 默认选择永久停止
    } else {
        elements.systemServiceOptions.style.display = 'none';
    }

    elements.confirmYes.onclick = () => {
        hideConfirmDialog();

        // 判断用户选择的关闭方式
        let permanent = false;
        if (isSystemManaged && elements.optionPermanent.checked) {
            permanent = true;
        }

        killProcess(pid, permanent);
    };

    elements.confirmDialog.style.display = 'flex';
}

function hideConfirmDialog() {
    elements.confirmDialog.style.display = 'none';
}

async function killProcess(pid, permanent = false) {
    try {
        const action = permanent ? '停止服务' : '关闭进程';
        showStatus(`正在${action} ${pid}...`, 'info');

        // 添加 permanent 参数到 URL
        const url = permanent ? `/api/process/${pid}?permanent=true` : `/api/process/${pid}`;
        const response = await fetch(url, { method: 'DELETE' });
        const result = await response.json();

        if (result.success) {
            showStatus(`${action}成功: ${pid}`, 'success');
            // 2秒后刷新列表（给系统一点时间完全停止服务）
            setTimeout(() => loadPorts(), 2000);
        } else {
            showStatus(`${action}失败: ` + (result.message || '未知错误'), 'error');
        }
    } catch (error) {
        console.error('Failed to kill process:', error);
        showStatus('操作失败: 网络错误', 'error');
    }
}

// 切换自动刷新
function toggleAutoRefresh() {
    state.autoRefresh = !state.autoRefresh;

    if (state.autoRefresh) {
        startAutoRefresh();
        elements.autoRefreshToggle.textContent = '⏸️ 暂停自动刷新';
        elements.autoRefreshStatus.textContent = '自动刷新: 开启 (5秒)';
    } else {
        stopAutoRefresh();
        elements.autoRefreshToggle.textContent = '▶️ 开启自动刷新';
        elements.autoRefreshStatus.textContent = '自动刷新: 关闭';
    }
}

// 启动自动刷新
function startAutoRefresh() {
    if (state.autoRefreshInterval) {
        clearInterval(state.autoRefreshInterval);
    }

    state.autoRefreshInterval = setInterval(() => {
        if (state.autoRefresh) {
            loadPorts();
        }
    }, 5000);
}

// 停止自动刷新
function stopAutoRefresh() {
    if (state.autoRefreshInterval) {
        clearInterval(state.autoRefreshInterval);
        state.autoRefreshInterval = null;
    }
}

// 更新UI
function updateUI() {
    updateTable();
    updateStatistics();
    updateLastScanTime();
}

// 更新表格
function updateTable() {
    if (state.filteredPorts.length === 0) {
        elements.portTableBody.innerHTML =
            '<tr><td colspan="11" class="loading">未找到端口信息</td></tr>';
        elements.portCount.textContent = '0 ports';
        elements.selectAll.checked = false;
        return;
    }

    // 按端口号排序
    const sortedPorts = [...state.filteredPorts].sort((a, b) => a.port - b.port);

    elements.portTableBody.innerHTML = sortedPorts.map(port => `
        <tr>
            <td>
                ${port.pid ? `<input type="checkbox" class="port-checkbox" data-pid="${port.pid}" data-port="${port.port}" data-process-name="${escapeHtml(port.processName || '')}" data-process-type="${port.processType || 'OTHER'}" ${state.selectedPorts.has(port.pid) ? 'checked' : ''}>` : '-'}
            </td>
            <td><strong>${port.port}</strong></td>
            <td>${getPortTypeBadge(port.portType)}</td>
            <td>${port.protocol || '-'}</td>
            <td><span class="status-badge status-${(port.status || 'listening').toLowerCase()}">${port.status || 'LISTENING'}</span></td>
            <td>${port.pid || '-'}</td>
            <td>${escapeHtml(port.processName || '-')}</td>
            <td title="${escapeHtml(port.commandLine || '')}">${truncate(escapeHtml(port.commandLine || '-'), 50)}</td>
            <td>${escapeHtml(port.user || '-')}</td>
            <td>${port.isDevelopmentProcess ? '<span class="dev-badge">开发</span>' : '-'}</td>
            <td>
                ${port.pid ? `<button class="kill-btn" onclick="showKillConfirm(${port.pid}, '${escapeHtml(port.processName || 'Unknown')}', ${port.port}, '${port.processType || 'OTHER'}')">关闭</button>` : '-'}
            </td>
        </tr>
    `).join('');

    elements.portCount.textContent = `${state.filteredPorts.length} ports`;

    // 为复选框添加事件监听
    document.querySelectorAll('.port-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', handlePortCheckboxChange);
    });

    // 更新全选状态
    updateSelectAllState();
}

// 获取端口类型标签
function getPortTypeBadge(portType) {
    const badges = {
        'FRONTEND': '<span class="type-badge type-frontend">前端</span>',
        'BACKEND': '<span class="type-badge type-backend">后端</span>',
        'DATABASE': '<span class="type-badge type-database">数据库</span>',
        'OTHER': '<span class="type-badge type-other">其他</span>'
    };
    return badges[portType] || badges['OTHER'];
}

// 更新统计信息
function updateStatistics() {
    elements.statTotal.textContent = state.ports.length;

    const devCount = state.ports.filter(p => p.isDevelopmentProcess).length;
    elements.statDev.textContent = devCount;

    const tcpCount = state.ports.filter(p => p.protocol === 'TCP').length;
    elements.statTcp.textContent = tcpCount;

    const udpCount = state.ports.filter(p => p.protocol === 'UDP').length;
    elements.statUdp.textContent = udpCount;
}

// 更新最后扫描时间
function updateLastScanTime() {
    if (state.lastScanTime) {
        const date = new Date(state.lastScanTime);
        elements.lastScan.textContent = formatTime(date);
    }
}

// 显示状态消息
function showStatus(message, type = 'info') {
    elements.statusMessage.textContent = message;
    elements.statusMessage.style.color =
        type === 'error' ? 'var(--color-danger)' :
        type === 'success' ? 'var(--color-free)' :
        'var(--text-secondary)';
}

// 工具函数
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function truncate(str, maxLength) {
    if (!str || str.length <= maxLength) return str;
    return str.substring(0, maxLength) + '...';
}

function formatTime(date) {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
}

// 应用筛选
function applyFilters() {
    let filtered = [...state.ports];

    // 端口类型筛选
    if (state.filters.portType) {
        filtered = filtered.filter(port => port.portType === state.filters.portType);
    }

    // 进程类型筛选
    if (state.filters.processType) {
        filtered = filtered.filter(port => port.processType === state.filters.processType);
    }

    // 协议筛选
    if (state.filters.protocol) {
        filtered = filtered.filter(port => port.protocol === state.filters.protocol);
    }

    // 开发进程筛选
    if (state.filters.devProcess) {
        const isDev = state.filters.devProcess === 'true';
        filtered = filtered.filter(port => port.isDevelopmentProcess === isDev);
    }

    // 常用端口筛选
    if (state.filters.commonPort) {
        const portNum = parseInt(state.filters.commonPort);
        filtered = filtered.filter(port => port.port === portNum);
    }

    state.filteredPorts = filtered;
}

// 处理筛选器变化
function handleFilterChange(event) {
    const filterId = event.target.id;
    const value = event.target.value;

    switch (filterId) {
        case 'portTypeFilter':
            state.filters.portType = value;
            break;
        case 'processTypeFilter':
            state.filters.processType = value;
            break;
        case 'protocolFilter':
            state.filters.protocol = value;
            break;
        case 'devProcessFilter':
            state.filters.devProcess = value;
            break;
    }

    applyFilters();
    updateUI();
    showFilterStatus();
}

// 处理常用端口选择
function handleCommonPortSelect(event) {
    const port = event.target.value;

    if (port) {
        state.filters.commonPort = port;
        // 清空其他筛选器
        state.filters.portType = '';
        state.filters.processType = '';
        state.filters.protocol = '';
        state.filters.devProcess = '';
        elements.portTypeFilter.value = '';
        elements.processTypeFilter.value = '';
        elements.protocolFilter.value = '';
        elements.devProcessFilter.value = '';

        // 清空搜索框
        elements.searchInput.value = '';

        applyFilters();
        updateUI();
        showStatus(`筛选端口: ${port}`, 'info');
    } else {
        state.filters.commonPort = '';
        applyFilters();
        updateUI();
    }
}

// 清除所有筛选
function clearAllFilters() {
    // 重置筛选状态
    state.filters = {
        portType: '',
        processType: '',
        protocol: '',
        devProcess: '',
        commonPort: ''
    };

    // 重置UI控件
    elements.portTypeFilter.value = '';
    elements.processTypeFilter.value = '';
    elements.protocolFilter.value = '';
    elements.devProcessFilter.value = '';
    elements.commonPortFilter.value = '';
    elements.searchInput.value = '';

    // 重新加载所有端口
    loadPorts();
    showStatus('已清除所有筛选', 'success');
}

// 显示筛选状态
function showFilterStatus() {
    const activeFilters = [];

    if (state.filters.portType) {
        const typeMap = {
            'FRONTEND': '前端',
            'BACKEND': '后端',
            'DATABASE': '数据库',
            'OTHER': '其他'
        };
        activeFilters.push(`端口类型: ${typeMap[state.filters.portType]}`);
    }

    if (state.filters.processType) {
        const processTypeMap = {
            'JAVA': 'Java',
            'NODE': 'Node.js',
            'PYTHON': 'Python',
            'WEB_SERVER': 'Web服务器',
            'DATABASE': '数据库',
            'IDE': 'IDE',
            'BROWSER': '浏览器',
            'SYSTEM': '系统',
            'OTHER': '其他'
        };
        activeFilters.push(`进程类型: ${processTypeMap[state.filters.processType]}`);
    }

    if (state.filters.protocol) {
        activeFilters.push(`协议: ${state.filters.protocol}`);
    }

    if (state.filters.devProcess) {
        activeFilters.push(`开发进程: ${state.filters.devProcess === 'true' ? '是' : '否'}`);
    }

    if (activeFilters.length > 0) {
        showStatus(`筛选: ${activeFilters.join(', ')} | ${state.filteredPorts.length} 个端口`, 'info');
    } else {
        showStatus(`显示所有端口: ${state.filteredPorts.length} 个`, 'success');
    }
}

// 暴露函数到全局作用域
window.showKillConfirm = showKillConfirm;

// ==================== 批量操作功能 ====================

// 处理全选/取消全选
function handleSelectAll(event) {
    const checked = event.target.checked;
    state.selectedPorts.clear();

    document.querySelectorAll('.port-checkbox').forEach(checkbox => {
        checkbox.checked = checked;
        if (checked) {
            const pid = parseInt(checkbox.dataset.pid);
            state.selectedPorts.add(pid);
        }
    });

    updateBatchKillButton();
}

// 处理单个复选框变化
function handlePortCheckboxChange(event) {
    const pid = parseInt(event.target.dataset.pid);

    if (event.target.checked) {
        state.selectedPorts.add(pid);
    } else {
        state.selectedPorts.delete(pid);
    }

    updateSelectAllState();
    updateBatchKillButton();
}

// 更新全选复选框状态
function updateSelectAllState() {
    const checkboxes = document.querySelectorAll('.port-checkbox');
    const checkedCount = document.querySelectorAll('.port-checkbox:checked').length;

    if (checkboxes.length === 0) {
        elements.selectAll.checked = false;
        elements.selectAll.indeterminate = false;
    } else if (checkedCount === 0) {
        elements.selectAll.checked = false;
        elements.selectAll.indeterminate = false;
    } else if (checkedCount === checkboxes.length) {
        elements.selectAll.checked = true;
        elements.selectAll.indeterminate = false;
    } else {
        elements.selectAll.checked = false;
        elements.selectAll.indeterminate = true;
    }
}

// 更新批量关闭按钮
function updateBatchKillButton() {
    const count = state.selectedPorts.size;

    if (count > 0) {
        elements.batchKillBtn.style.display = 'block';
        elements.batchKillText.textContent = `关闭选中 (${count})`;
    } else {
        elements.batchKillBtn.style.display = 'none';
    }
}

// 显示批量关闭确认对话框
function showBatchKillConfirm() {
    if (state.selectedPorts.size === 0) {
        showStatus('请先选择要关闭的端口', 'error');
        return;
    }

    // 收集选中的端口信息
    const selectedPortInfos = [];
    let hasSystemManaged = false;

    document.querySelectorAll('.port-checkbox:checked').forEach(checkbox => {
        const pid = parseInt(checkbox.dataset.pid);
        const port = parseInt(checkbox.dataset.port);
        const processName = checkbox.dataset.processName;
        const processType = checkbox.dataset.processType;

        selectedPortInfos.push({ pid, port, processName, processType });

        if (processType === 'DATABASE' || processType === 'SYSTEM' || processType === 'WEB_SERVER') {
            hasSystemManaged = true;
        }
    });

    // 显示端口列表
    elements.batchPortList.innerHTML = selectedPortInfos.map(info => `
        <div class="batch-port-item">
            <div class="batch-port-info">
                <span class="batch-port-number">端口 ${info.port}</span>
                <span class="batch-process-name">PID: ${info.pid} - ${info.processName}</span>
            </div>
        </div>
    `).join('');

    // 更新消息
    elements.batchConfirmMessage.textContent =
        `确定要关闭选中的 ${selectedPortInfos.length} 个进程吗？`;

    // 根据是否有系统管理的服务显示选项
    if (hasSystemManaged) {
        elements.batchSystemServiceOptions.style.display = 'block';
        elements.batchOptionTemporary.checked = false;
        elements.batchOptionPermanent.checked = true; // 默认选择永久停止
    } else {
        elements.batchSystemServiceOptions.style.display = 'none';
    }

    // 设置确认按钮事件
    elements.batchConfirmYes.onclick = () => {
        hideBatchConfirmDialog();

        // 判断用户选择的关闭方式
        let permanent = false;
        if (hasSystemManaged && elements.batchOptionPermanent.checked) {
            permanent = true;
        }

        batchKillProcesses(Array.from(state.selectedPorts), permanent);
    };

    elements.batchConfirmDialog.style.display = 'flex';
}

// 隐藏批量确认对话框
function hideBatchConfirmDialog() {
    elements.batchConfirmDialog.style.display = 'none';
}

// 批量关闭进程
async function batchKillProcesses(pids, permanent = false) {
    try {
        const action = permanent ? '停止服务' : '关闭进程';
        showStatus(`正在批量${action} (${pids.length}个)...`, 'info');

        const response = await fetch('/api/process/batch', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                pids: pids,
                permanent: permanent
            })
        });

        const result = await response.json();

        if (result.success) {
            const successCount = result.results.filter(r => r.success).length;
            const failCount = result.results.filter(r => !r.success).length;

            let message = `批量${action}完成: ${successCount} 成功`;
            if (failCount > 0) {
                message += `, ${failCount} 失败`;
            }

            showStatus(message, successCount > 0 ? 'success' : 'error');

            // 清空选择
            state.selectedPorts.clear();
            updateBatchKillButton();

            // 2秒后刷新列表
            setTimeout(() => loadPorts(), 2000);
        } else {
            showStatus(`批量${action}失败: ` + (result.message || '未知错误'), 'error');
        }
    } catch (error) {
        console.error('Failed to batch kill processes:', error);
        showStatus('批量操作失败: 网络错误', 'error');
    }
}