<template>
  <div>
    <n-form label-placement="left">
      <div>启动{{ status ? '成功' : '失败' }}，首次使用请选择IP生成二维码匹配，电脑IP如无变动无需重新匹配</div>

      <n-form-item style="margin-top: 16px;">
        监听端口：<div style="font-size:20px;">{{ config.port }}</div>

        <n-button type="info" style="margin-left: 16px;" @click="showPort">
          修改端口
        </n-button>
        <n-button type="primary" style="margin-left: 16px;" @click="genQRCode">
          生成二维码
        </n-button>

        <div style="margin-left: 16px;">
          <n-popover trigger="click">
            <template #trigger>
              <n-button color="#8a2be2">查看电脑IP</n-button>
            </template>
            <div>
              <div style="display: block;margin-top: 8px;" v-for="option in options">
                <n-button strong secondary type="info" @click="copyText(option.value)">
                  {{ option.value }}</n-button>
              </div>
            </div>
          </n-popover>
        </div>

        <n-button type="warning" style="margin-left: 16px;" @click="hideApp" v-if="!isDarwin">
          隐藏到托盘图标
        </n-button>



      </n-form-item>
      <n-form-item label="自动打开文本中的链接：" label-align="left">
        <n-switch v-model:value="config.auto_open_url" @update:value="saveConfig(1)" />
      </n-form-item>
      <n-modal v-model:show="showPortModal" class="custom-card" preset="card" :style="bodyStyle" title="修改监听端口"
        size="huge" :bordered="false">
        <n-form-item label="监听端口：" label-placement="left">
          <n-input-number v-model:value="config.port" :show-button="false" :update-value-on-input="false" :min="1024"
            :max="65535" placeholder="端口范围1024到65535" />
          <n-button type="primary" style="margin-left: 8px;" @click="saveConfig(0)">
            保存
          </n-button>
        </n-form-item>
      </n-modal>
    </n-form>
    <div style="display: flex;width: 100%;flex-direction: column;">
      <div style="display: flex;justify-content: space-between;">接收日志： <n-button @click="clearLogs" strong secondary
          type="info">清空日志</n-button></div>
      <div style="margin-top: 12px;width: 80%;">
        <n-list bordered>
          <n-list-item v-for="log in logs">
            <n-thing>
              <template #description>
                <div style="font-size: 12px; color: #999; margin-bottom: 4px;">
                  {{ formatRelativeTime(log.timestamp || Date.now()) }}
                </div>
              </template>
              <div style="word-wrap: break-word;" v-html="convertLinksToHtml(log)" @click="handleLogClick"></div>
            </n-thing>
            <template #suffix>
              <n-button @click="copyText(log)">复制</n-button>
            </template>
          </n-list-item>
        </n-list>
      </div>
    </div>
    <n-modal v-model:show="showModal" class="custom-card" preset="card" :style="bodyStyle" title="二维码生成" size="huge"
      @close="closeQrCodeDialog" :bordered="false">
      <n-form-item label="IP地址：" label-placement="left">
        <n-select :options="options" @update:value="selectIP" placeholder="请选择电脑所在网络的IP地址" />
        <n-button type="primary" style="margin-left: 8px;" @click="reFreshIp">
          刷新
        </n-button>
      </n-form-item>
      <n-form-item v-if="show_qrcode">
        <qrcode-vue :value="qrcode_text" :size="300" level="H" />
      </n-form-item>
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
const showModal = ref(false)
const isDarwin = ref(false)
const showPortModal = ref(false)
const bodyStyle = {
  width: '600px'
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