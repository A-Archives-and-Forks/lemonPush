import { createStore } from 'vuex'


export default createStore({
    state: {
        status: false,
        logs: [] as Array<{content: string, timestamp: number}>,
        config: {
            ip: '',
            port: 14756,
            auto_open_url: true,
            folder: './_lemon_/'
        }
    },
    mutations: {
        updateStatus(state, playload) {
            state.status = playload
        },
        updateLogs(state, playload) {
            state.logs = playload
        },
        addLog(state, logData) {
            // 如果传入的是字符串，转换为包含时间戳的对象
            const logItem = typeof logData === 'string' ? { content: logData, timestamp: Date.now() } : logData;
            state.logs.unshift(logItem);
        },
        updateConfig(state, playload) {
            state.config = playload
        }
    }
})