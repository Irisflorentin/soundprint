import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { setupGuards } from './router/guards';
import { pinia } from './stores';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { registerSoundprintTheme } from '@/utils/echarts-theme';

import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import './styles/index.scss';

registerSoundprintTheme();

const app = createApp(App);
app.use(pinia);
app.use(router);
app.use(ElementPlus, { locale: zhCn });

// 路由守卫需在 pinia 之后
setupGuards(router);

// 启用 Element-Plus 暗色主题
document.documentElement.classList.add('dark');

app.mount('#app');
