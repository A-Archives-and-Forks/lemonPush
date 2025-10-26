<template>
    <div>
        <n-h3>柠檬Push</n-h3>
        <n-collapse :default-expanded-names="['1']">
            <n-collapse-item title="软件简介" name="1">
                <div>同一WiFi环境下手机高效推送文本到电脑剪切板的工具，移动端支持Android、iOS，电脑端支持Windows、Mac、Linux平台</div>
            </n-collapse-item>
            <n-collapse-item title="项目地址" name="2">
                <n-a @click="openUrl('https://github.com/ishare20/lemonPush')">Github</n-a>
            </n-collapse-item>
            <n-collapse-item title="常见问题" name="3">
                <div>
                    电脑无法接收手机剪切板推送，需要配置允许应用通过防火墙
                </div>
            </n-collapse-item>
            <n-collapse-item title="版本更新" name="4">
                <div><n-a @click="openUrl('https://t.me/+ZVIwHSBOg1o5NzFl')">电报群</n-a>、微信订阅号发布更新版本</div>
                <n-image style="margin-top: 12px;" width="60%" src="https://img.lemontree.one/i/2025/10/25/gox6fu.jpg" />
            </n-collapse-item>

            <n-collapse-item title="开发者作品" name="6">
                <div style="display: flex;">
                    <n-card @click="viewProduct(product)" hoverable style="margin-left: 8px;cursor: pointer;" :title="product.name"
                        v-for="product in productList">
                        {{ product.desc }}
                        <template #footer>
                            <n-tag type="success" size="small">
                                {{ dictionary.get(product.type) }}
                            </n-tag>
                        </template>
                    </n-card>
                </div>
            </n-collapse-item>
        </n-collapse>
        <n-modal v-model:show="showModal" preset="card" :style="bodyStyle" title="扫码体验" size="huge" :bordered="false">
            <div style="display: flex;justify-content: center;">
                <n-image width="100%" :src="imageUrl" />
            </div>
        </n-modal>
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import * as runtime from '../../wailsjs/runtime/runtime';
const openUrl = (url: string) => {
    runtime.BrowserOpenURL(url)
}

const dictionary: Map<number, string> = new Map([
    [0, '小程序'],
    [1, '浏览器插件'],
    [2, '网页'],
    [3, 'PC软件'],
]);
const bodyStyle = {
    width: '600px'
}
const imageUrl = ref('https://lemontree.one/files/images/gh_d34d2687b0a1_258.jpg')
const showModal = ref(false)
const productList = [
    {
        name: '便捷搜索',
        desc: '无需重输关键词一键切换搜索引擎',
        type: 1,
        link: 'https://lemontree.one/docs/quicksearch/'
    },
    {
        name: '翻译命名',
        desc: '快速获取多种命名格式的翻译工具',
        type: 3,
        link: 'https://lemontree.one/tools/naming-tool.html'
    },
    {
        name: '今日愿景板',
        desc: '写下你的今日愿景，放置显而易见的位置',
        type: 2,
        link: 'https://lemontree.one/sibtools/vision_board'
    }
]
const viewProduct = (product: any) => {
    if (product.type === 0) {
        showModal.value = true
        imageUrl.value = product.link
    } else {
        runtime.BrowserOpenURL(product.link)
    }
}

</script>

<style></style>