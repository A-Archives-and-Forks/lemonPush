<template>
  <div class="home-container">
    <!-- 状态卡片 -->
    <n-card class="status-card" :class="{ 'status-success': status, 'status-error': !status }">
      <div class="status-header">
        <div class="status-indicator">
          <div class="status-dot" :class="{ 'active': status }"></div>
          <span class="status-text">{{ status ? '服务运行中' : '服务未启动' }}</span>
        </div>
        <n-tag :type="status ? 'success' : 'error'" size="small">
          端口: {{ config.port }}
        </n-tag>
      </div>
      <div class="status-description">
        {{ status ? '服务正常运行，可以接收推送内容' : '首次使用请生成二维码进行设备匹配' }}
      </div>
    </n-card>

    <!-- 主要操作区域 -->
    <n-card class="main-actions" title="快速操作">
      <div class="action-buttons">
        <n-button type="primary" size="large" @click="genQRCode" class="action-btn">
          <template #icon>
            <n-icon><qr-code-outline /></n-icon>
          </template>
          生成二维码
        </n-button>
        
        <n-popover trigger="click" placement="bottom">
          <template #trigger>
            <n-button size="large" class="action-btn">
              <template #icon>
                <n-icon><wifi-outline /></n-icon>
              </template>
              查看IP地址
            </n-button>
          </template>
          <div class="ip-list">
            <div v-for="option in options" :key="option.value" class="ip-item">
              <n-button text @click="copyText(option.value)" class="ip-copy-btn">
                {{ option.value }}
                <template #icon>
                  <n-icon><copy-outline /></n-icon>
                </template>
              </n-button>
            </div>
          </div>
        </n-popover>

        <n-button type="info" size="large" @click="showPort" class="action-btn">
          <template #icon>
            <n-icon><settings-outline /></n-icon>
          </template>
          修改端口
        </n-button>

        <n-button @click="hideApp" size="large" class="action-btn" v-if="!isDarwin">
          <template #icon>
            <n-icon><arrow-down-outline /></n-icon>
          </template>
          隐藏窗口
        </n-button>
      </div>
    </n-card>

    <!-- 设置面板 -->
    <n-card class="settings-panel" title="设置选项">
      <div class="settings-grid">
        <div class="setting-item">
          <div class="setting-info">
            <div class="setting-title">
              自动打开链接
              <n-popover trigger="hover" placement="top" :show-arrow="false">
                <template #trigger>
                  <n-icon class="help-icon" size="16">
                    <help-circle-outline />
                  </n-icon>
                </template>
                <div class="help-tooltip">
                  开启后，当接收到包含网页链接的文本时，会自动使用默认浏览器打开链接
                </div>
              </n-popover>
            </div>
          </div>
          <n-switch v-model:value="config.auto_open_url" @update:value="saveConfig(1)" />
        </div>
        

      </div>
    </n-card>

    <!-- 日志面板 -->
    <n-card class="logs-panel" title="接收日志">
      <template #header-extra>
        <n-button text @click="clearLogs" class="clear-btn">
          <template #icon>
            <n-icon><trash-outline /></n-icon>
          </template>
          清空日志
        </n-button>
      </template>
      
      <div class="logs-container" v-if="logs.length > 0">
        <div v-for="log in logs" :key="log.timestamp" class="log-item">
          <div class="log-time">{{ formatRelativeTime(log.timestamp || Date.now()) }}</div>
          <div class="log-content" v-html="convertLinksToHtml(log)" @click="handleLogClick"></div>
          <n-button text size="small" @click="copyText(log)" class="copy-btn">
            <template #icon>
              <n-icon><copy-outline /></n-icon>
            </template>
          </n-button>
        </div>
      </div>
      <div v-else class="empty-logs">
        <n-icon size="48" class="empty-icon">
          <document-text-outline />
        </n-icon>
        <div class="empty-text">暂无接收日志</div>
      </div>
    </n-card>

    <!-- 二维码模态框 -->
    <n-modal v-model:show="showModal" preset="card" title="设备匹配" :style="modalStyle" class="qr-modal">
      <div class="qr-content">
        <div class="qr-header">
          <div class="qr-title">扫描二维码匹配设备</div>
          <div class="qr-subtitle">请使用手机扫描下方二维码进行设备匹配</div>
        </div>
        
        <n-form-item label="选择IP地址：" label-placement="left">
          <n-select :options="options" @update:value="selectIP" placeholder="请选择电脑所在网络的IP地址" />
          <n-button text @click="reFreshIp" class="refresh-btn">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            刷新
          </n-button>
        </n-form-item>
        
        <div v-if="show_qrcode" class="qr-code-container">
          <qrcode-vue :value="qrcode_text" :size="280" level="H" class="qr-code" />
          <div class="qr-text">{{ qrcode_text }}</div>
        </div>
      </div>
    </n-modal>

    <!-- 端口设置模态框 -->
    <n-modal v-model:show="showPortModal" preset="card" title="端口设置" :style="modalStyle">
      <div class="port-content">
        <div class="port-info">
          <div class="port-title">修改监听端口</div>
          <div class="port-desc">端口范围：1024 - 65535</div>
        </div>
        
        <n-form-item label="新端口：" label-placement="left">
          <n-input-number 
            v-model:value="config.port" 
            :show-button="false" 
            :update-value-on-input="false" 
            :min="1024"
            :max="65535" 
            placeholder="请输入新端口"
            class="port-input"
          />
        </n-form-item>
        
        <div class="modal-actions">
          <n-button @click="showPortModal = false">取消</n-button>
          <n-button type="primary" @click="saveConfig(0)">保存设置</n-button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { SelectOption, useMessage } from 'naive-ui'
import { GetIPList, InitListener, LoadConfig, SaveConfig } from "../../wailsjs/go/main/App";
import { computed } from 'vue';
import * as runtime from '../../wailsjs/runtime/runtime';
import QrcodeVue from 'qrcode.vue'
import { useStore } from 'vuex';
import {
  QrCodeOutline,
  WifiOutline,
  SettingsOutline,
  ArrowDownOutline,
  CopyOutline,
  TrashOutline,
  DocumentTextOutline,
  RefreshOutline,
  HelpCircleOutline
} from '@vicons/ionicons5'

const showModal = ref(false)
const isDarwin = ref(false)
const showPortModal = ref(false)
const modalStyle = {
  width: '420px'
}
const store = useStore();
const logs = computed(() => store.state.logs);
const message = useMessage()

const config = computed(() => store.state.config)


const hideApp = () => {
  runtime.WindowHide()
}
runtime.Environment().then(res => {
  if (res.platform === 'darwin') {
    isDarwin.value = true
  }
})

const status = computed(() => store.state.status);
const options = ref<any[]>([]);
const qrcode_text = ref('柠檬Push，请先选择IP')
const show_qrcode = ref(false)
const timeUpdateKey = ref(0) // 用于强制更新时间显示
const loadConfig = () => {
  if (!status.value) {
    message.loading(
      '加载配置中'
    )
    LoadConfig().then((res) => {
      store.commit('updateConfig', res)
      message.loading(
        '启动服务中'
      )
      InitListener(res).then((res: any) => {
        console.log(res);
        message.destroyAll()
        if (res.code === 1) {
          store.commit('updateStatus', true)
        } else {
          store.commit('updateStatus', false)
          message.error('启动出错：' + res.msg)
        }
      }).catch(e => {
        console.log(e);
      })
    }).catch(e => {
      console.log(e);
    })
    runtime.EventsOn('showLogs', (log: any) => {
      store.commit('addLog', log);
    });
  }
}
const selectIP = (value: string, option: SelectOption) => {
  show_qrcode.value = true
  let config = store.state.config
  config.ip = value
  qrcode_text.value = config.ip + ':' + config.port
}

const closeQrCodeDialog = () => {
  show_qrcode.value = false
}
const reFreshIp = () => {
  GetIPList().then(res => {
    message.destroyAll()
    options.value = toOptions(res)
  }).catch(e => {
    message.error(
      '获取电脑IP失败' + e.toString()
    )
  })
}

const clearLogs = () => {
  //store.state.logs =[]
  store.commit('updateLogs', [])
}

const genQRCode = () => {
  reFreshIp()
  showModal.value = true
}

const copyText = (text: string | {content: string, timestamp: number}) => {
  // 如果传入的是对象，提取内容；如果是字符串，直接使用
  let content: string;
  if (typeof text === 'object' && text !== null) {
    content = text.content || '';
  } else {
    content = text as string;
  }
  navigator.clipboard.writeText(content).then(res => {
    message.success('复制成功')
  })
}

const convertLinksToHtml = (log: string | {content: string, timestamp: number}) => {
  // 提取文本内容
  let text: string;
  if (typeof log === 'object' && log !== null) {
    text = log.content || '';
  } else {
    text = log as string;
  }
  
  // 匹配URL的正则表达式
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  // 将URL替换为可点击的链接，使用data-url属性存储URL
  return text.replace(urlRegex, '<a href="#" class="log-link" data-url="$1" style="color: #1890ff; text-decoration: underline; cursor: pointer;">$1</a>');
}

const formatRelativeTime = (timestamp: number) => {
  // 使用timeUpdateKey来强制Vue重新计算
  const key = timeUpdateKey.value;
  const now = Date.now();
  const diff = now - timestamp;
  
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  
  if (seconds < 60) {
    return '刚刚';
  } else if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else {
    return `${days}天前`;
  }
}

// 处理日志点击事件
const handleLogClick = (event: Event) => {
  const target = event.target as HTMLElement;
  if (target.classList.contains('log-link')) {
    event.preventDefault();
    const url = target.getAttribute('data-url');
    if (url) {
      // 使用Wails运行时打开URL
      runtime.BrowserOpenURL(url);
    }
  }
}

const toOptions = (list: string[]) => {
  return list.map(item => {
    return {
      label: item,
      value: item
    }
  })
}

const showPort = () => {
  showPortModal.value = true
}

const saveConfig = (e: any) => {
  let config = store.state.config
  SaveConfig(config).then((result: any) => {
    if (result.code === 1) {
      if (e === 0) {
        message.success(
          '修改成功，重启后生效'
        )
      } else {
        message.success(
          '修改成功'
        )
      }

      showPortModal.value = false
    } else {
      message.error(result.msg)
    }
  })
}
loadConfig()
reFreshIp()

// 添加定时器，每30秒更新一次时间显示
let timeInterval: number | null = null;

onMounted(() => {
  // 每30秒更新一次时间显示
  timeInterval = window.setInterval(() => {
    timeUpdateKey.value++;
  }, 30000);
});

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval);
  }
});

</script>

<style scoped>
.home-container {
  padding: 16px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 状态卡片样式 */
.status-card {
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: white;
  border: none;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff4757;
  transition: all 0.3s ease;
}

.status-dot.active {
  background: #2ed573;
  box-shadow: 0 0 8px rgba(46, 213, 115, 0.4);
}

.status-text {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.status-description {
  color: #7f8c8d;
  font-size: 13px;
  line-height: 1.4;
}

/* 主要操作区域 */
.main-actions {
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: white;
  border: none;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  padding: 4px 0;
}

.action-btn {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 36px;
  font-size: 14px;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 设置面板 */
.settings-panel {
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: white;
  border: none;
}

.settings-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.setting-item:hover {
  background: #e9ecef;
}

.setting-info {
  flex: 1;
}

.setting-title {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 2px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.help-icon {
  color: #95a5a6;
  cursor: help;
  transition: color 0.3s ease;
}

.help-icon:hover {
  color: #3498db;
}

.help-tooltip {
  max-width: 280px;
  font-size: 13px;
  line-height: 1.5;
  color: #2c3e50;
}

.setting-desc {
  font-size: 12px;
  color: #7f8c8d;
}

/* 日志面板 */
.logs-panel {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: white;
  border: none;
}

.clear-btn {
  color: #e74c3c;
}

.clear-btn:hover {
  color: #c0392b;
}

.logs-container {
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid #ecf0f1;
  transition: all 0.3s ease;
}

.log-item:hover {
  background: #f8f9fa;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  font-size: 11px;
  color: #95a5a6;
  white-space: nowrap;
  min-width: 50px;
}

.log-content {
  flex: 1;
  font-size: 13px;
  line-height: 1.4;
  color: #2c3e50;
  word-break: break-word;
}

.copy-btn {
  color: #3498db;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.copy-btn:hover {
  opacity: 1;
  color: #2980b9;
}

.empty-logs {
  text-align: center;
  padding: 40px 16px;
  color: #95a5a6;
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-text {
  font-size: 14px;
  color: #7f8c8d;
}

/* IP地址列表 */
.ip-list {
  min-width: 180px;
}

.ip-item {
  margin-bottom: 6px;
}

.ip-copy-btn {
  width: 100%;
  justify-content: space-between;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.ip-copy-btn:hover {
  background: #f8f9fa;
}

/* 二维码模态框 */
.qr-modal {
  border-radius: 12px;
}

.qr-content {
  text-align: center;
}

.qr-header {
  margin-bottom: 16px;
}

.qr-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
}

.qr-subtitle {
  font-size: 13px;
  color: #7f8c8d;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}

.qr-code {
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  padding: 12px;
  background: white;
}

.qr-text {
  font-size: 13px;
  color: #7f8c8d;
  font-family: monospace;
}

.refresh-btn {
  margin-left: 8px;
  color: #3498db;
}

.refresh-btn:hover {
  color: #2980b9;
}

/* 端口设置模态框 */
.port-content {
  padding: 16px 0;
}

.port-info {
  margin-bottom: 16px;
}

.port-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
}

.port-desc {
  font-size: 13px;
  color: #7f8c8d;
}

.port-input {
  width: 100%;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

/* 卡片标题优化 - 更紧凑 */
.n-card-header {
  padding: 12px 16px !important;
  border-bottom: 1px solid #f0f0f0;
}

.n-card-header__main {
  font-size: 16px !important;
  font-weight: 600 !important;
}

.n-card__content {
  padding: 16px !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    padding: 12px;
  }
  
  .action-buttons {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .settings-grid {
    gap: 8px;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 10px;
  }
  
  .log-item {
    flex-direction: column;
    gap: 6px;
    padding: 10px;
  }
}

/* 自定义滚动条 */
.logs-container::-webkit-scrollbar {
  width: 6px;
}

.logs-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.logs-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.logs-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>