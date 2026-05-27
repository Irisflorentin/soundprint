import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { setupGuards } from './router/guards';
import { pinia } from './stores';
import { useUserStore } from './stores/user';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { registerSoundprintTheme } from '@/utils/echarts-theme';

import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import './styles/index.scss';

registerSoundprintTheme();

const app = createApp(App);
app.use(pinia);

// 必须在路由挂载前恢复登录态，否则首次访问会被守卫误判。
const userStore = useUserStore();
userStore.restoreFromStorage();
setupGuards(router);

app.use(router);
app.use(ElementPlus, { locale: zhCn });

// 启用 Element-Plus 暗色主题
document.documentElement.classList.add('dark');

app.mount('#app');
