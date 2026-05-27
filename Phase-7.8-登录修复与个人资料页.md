# Phase 7.8：登录绕过 bug 修复 + 轻量个人资料页

> Phase 7 系列的最后一个补丁阶段。
> 完成后正式收官，进入 Phase 8（Docker + 报告 + 答辩）。
> 估计工作量：**4 小时**（Codex 一个回合）。

---

## 🎯 阶段目标

1. **修复登录页绕过 bug**：当前 store 里默认有 admin 用户，路由 guard 放行，登录页变成装饰。修复后初次访问 / 刷新 / 清缓存必须经过登录页。
2. **新增轻量个人资料页**：替换设置页当前的"后续实装"占位，显示用户名、头像、个人统计、注销按钮。
3. **保持现有所有功能** + 视觉风格不变（黑白银 + 香槟金）。

---

## ⚠️ 关键约束

- **纯前端**，0 后端改动，0 数据库改动
- **不涉及多用户系统**——admin 单用户即可
- **不实现头像上传**——用字母 Avatar 占位（视觉一致性已够）
- **不破坏现有功能**：播放、转换、统计、上传 全部保持工作
- **Phase 7.5-7.7 视觉风格不动**

---

## 📋 任务清单

### 任务 1：修复登录绕过 bug

#### 1.1 修改 `frontend/src/stores/user.ts`

**当前问题**：store 初始化时**默认填了一个 admin 用户**，所以路由 guard 一开始就看到"已登录"状态，跳过 `/login`。

**修改逻辑**：

```ts
// 旧（大概是这样）
import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: {
      id: 1,
      username: 'admin',
      nickname: 'Kaidi',
      // ...
    },
    token: 'fake-token-admin',
  }),
  // ...
});

// 新：state 默认空，从 localStorage 恢复
export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as User | null,
    token: '' as string,
  }),

  getters: {
    isLoggedIn: (state) => !!state.user && !!state.token,
    nickname: (state) => state.user?.nickname || state.user?.username || '',
    avatar: (state) => state.user?.avatarUrl || '',
  },

  actions: {
    login(username: string, password: string) {
      // 演示账号：admin + 任意密码
      // 实际项目这里调后端 /api/auth/login，本项目作为大作业演示用硬编码
      this.user = {
        id: 1,
        username: 'admin',
        nickname: 'Kaidi',
        avatarUrl: '',
        registeredAt: '2026-05-26',
      };
      this.token = `demo-token-${Date.now()}`;

      // 持久化到 localStorage
      localStorage.setItem('soundprint_user', JSON.stringify(this.user));
      localStorage.setItem('soundprint_token', this.token);
    },

    logout() {
      this.user = null;
      this.token = '';
      localStorage.removeItem('soundprint_user');
      localStorage.removeItem('soundprint_token');
    },

    /**
     * 从 localStorage 恢复登录态
     * 在 main.ts / App.vue 初始化时调用一次
     */
    restoreFromStorage() {
      const userJson = localStorage.getItem('soundprint_user');
      const token = localStorage.getItem('soundprint_token');
      if (userJson && token) {
        try {
          this.user = JSON.parse(userJson);
          this.token = token;
        } catch {
          this.logout();
        }
      }
    },
  },
});
```

User 类型定义（在 types 目录或 store 文件里）：

```ts
export interface User {
  id: number;
  username: string;
  nickname: string;
  avatarUrl: string;
  registeredAt: string;
}
```

#### 1.2 修改 `frontend/src/router/index.ts`（或 router guard 所在文件）

```ts
import { useUserStore } from '@/stores/user';

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  // 公开页面（不需要登录）
  const publicPages = ['/login'];
  const requiresAuth = !publicPages.includes(to.path);

  // 未登录且访问需要登录的页面 → 跳登录页
  if (requiresAuth && !userStore.isLoggedIn) {
    next('/login');
    return;
  }

  // 已登录但访问登录页 → 跳首页
  if (to.path === '/login' && userStore.isLoggedIn) {
    next('/');
    return;
  }

  next();
});
```

#### 1.3 修改 `frontend/src/main.ts`

在 Pinia 挂载之后**立刻恢复登录态**：

```ts
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { useUserStore } from './stores/user';
// ... 其他 import ...

const app = createApp(App);
app.use(createPinia());

// 关键：在 router 挂载前恢复登录态，避免首次跳转判断错误
const userStore = useUserStore();
userStore.restoreFromStorage();

app.use(router);
// ... 其他 use ...

app.mount('#app');
```

#### 1.4 修改 `LoginView.vue`（如果之前 handleLogin 是硬编码跳转）

确认登录按钮调用 `userStore.login()` 而非直接修改 store 内部字段：

```ts
async function handleLogin() {
  loading.value = true;
  try {
    userStore.login(username.value, password.value);
    await new Promise(resolve => setTimeout(resolve, 400));
    router.push('/');
  } finally {
    loading.value = false;
  }
}
```

#### 🔍 检查点 1：测试登录绕过修复

**用户做的事**：

1. **F12 → Application → Local Storage → 删除所有 soundprint_ 开头的键**
2. **刷新浏览器**，访问 `http://localhost:5173/`
3. **预期**：自动跳转到 `/login`，不能直接进首页
4. 输入 admin + 任意密码 → 登录成功 → 跳首页
5. 刷新页面 → 仍在首页（不会再跳登录页）
6. 进设置页点"注销"（任务 2 实现） → 跳回登录页

---

### 任务 2：个人资料页

#### 2.1 创建 `frontend/src/views/profile/ProfileView.vue`

```vue
<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { dashboardApi } from '@/api/dashboard';
import { favoriteApi } from '@/api/favorite';

const router = useRouter();
const userStore = useUserStore();
const { user } = storeToRefs(userStore);

// 个人统计数据
const stats = ref({
  totalPlays: 0,
  favoriteCount: 0,
  topArtist: '—',
});

onMounted(async () => {
  try {
    const [dashboard, favorites] = await Promise.all([
      dashboardApi.get(),
      favoriteApi.list({ page: 1, size: 1 }),
    ]);

    // dashboard.totalPlays 或类似字段，Codex 看实际 API 字段
    stats.value.totalPlays = dashboard.totalPlayCount || dashboard.totalPlays || 0;
    stats.value.favoriteCount = favorites.total || 0;
    stats.value.topArtist =
      dashboard.topArtists?.[0]?.name || '—';
  } catch (e) {
    console.warn('加载个人统计失败', e);
  }
});

// 头像首字母
const initials = computed(() => {
  const name = user.value?.nickname || user.value?.username || '?';
  return name[0]?.toUpperCase() || '?';
});

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要注销当前账号吗？', '注销确认', {
      confirmButtonText: '注销',
      cancelButtonText: '取消',
      type: 'warning',
    });
    userStore.logout();
    router.push('/login');
  } catch {
    // 用户取消
  }
}
</script>

<template>
  <div v-if="user" class="profile-view">
    <header class="page-header">
      <h1>个人资料</h1>
      <p class="subtitle">账号信息、聆听统计与个人偏好</p>
    </header>

    <!-- 顶部：头像 + 名字 -->
    <section class="hero-card">
      <div class="avatar-large">
        <span>{{ initials }}</span>
      </div>
      <div class="identity">
        <h2 class="nickname">{{ user.nickname }}</h2>
        <p class="username">@{{ user.username }}</p>
        <p class="registered">注册于 {{ user.registeredAt }}</p>
      </div>
    </section>

    <!-- 中部：统计 -->
    <section class="stats-card">
      <h3 class="section-title">我的统计</h3>
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalPlays }}</div>
          <div class="stat-label">累计聆听</div>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <div class="stat-value">{{ stats.favoriteCount }}</div>
          <div class="stat-label">收藏曲目</div>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <div class="stat-value top-artist">{{ stats.topArtist }}</div>
          <div class="stat-label">最爱艺术家</div>
        </div>
      </div>
    </section>

    <!-- 底部：账户操作 -->
    <section class="account-card">
      <h3 class="section-title">账户</h3>
      <div class="account-info">
        <div class="info-row">
          <span class="info-label">用户名</span>
          <span class="info-value">{{ user.username }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">注册时间</span>
          <span class="info-value">{{ user.registeredAt }}</span>
        </div>
      </div>
      <div class="actions">
        <el-button @click="handleLogout">注销账号</el-button>
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.profile-view {
  max-width: 880px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  padding: var(--space-5);
}

.page-header {
  margin-bottom: var(--space-3);
  h1 {
    font-size: 32px;
    font-weight: 700;
    color: #E5E7EB;
    margin: 0 0 var(--space-2);
  }
  .subtitle {
    color: #94A3B8;
    font-size: 14px;
    margin: 0;
  }
}

/* ============== 通用卡片 ============== */
.hero-card,
.stats-card,
.account-card {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: var(--radius-card);
  padding: var(--space-6);
  backdrop-filter: blur(20px);
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #64748B;
  margin: 0 0 var(--space-4);
}

/* ============== Hero 卡片：头像 + 名字 ============== */
.hero-card {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.avatar-large {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #F4F5F7 0%, #94A3B8 50%, #C8A862 100%);
  color: #0A0A0B;
  font-size: 42px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.identity {
  flex: 1;
}

.nickname {
  font-size: 28px;
  font-weight: 700;
  color: #E5E7EB;
  margin: 0 0 var(--space-2);
}

.username {
  color: #94A3B8;
  font-size: 14px;
  margin: 0 0 var(--space-1);
}

.registered {
  color: #64748B;
  font-size: 13px;
  margin: 0;
}

/* ============== 统计卡片 ============== */
.stats-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: var(--space-3) 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #E5E7EB;
  margin-bottom: var(--space-2);
  line-height: 1;

  &.top-artist {
    font-size: 18px;
    background: linear-gradient(135deg, #F4F5F7 0%, #C8A862 100%);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }
}

.stat-label {
  font-size: 12px;
  color: #94A3B8;
  letter-spacing: 0.05em;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: rgba(255, 255, 255, 0.06);
}

/* ============== 账户卡片 ============== */
.account-info {
  margin-bottom: var(--space-5);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: var(--space-3) 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  color: #94A3B8;
  font-size: 14px;
}

.info-value {
  color: #E5E7EB;
  font-size: 14px;
  font-weight: 500;
}

.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
```

#### 2.2 注册路由

修改 `frontend/src/router/index.ts`，添加：

```ts
{
  path: '/profile',
  name: 'Profile',
  component: () => import('@/views/profile/ProfileView.vue'),
},
```

#### 2.3 替换设置页占位

修改 `frontend/src/views/settings/SettingsView.vue`（或者直接让 `/settings` 重定向到 `/profile`）。

**推荐方案**：直接让"设置"路由跳到个人资料页：

```ts
// router/index.ts
{
  path: '/settings',
  redirect: '/profile',
},
```

**或者**保留 SettingsView 但把内容改成"链接到个人资料"+ 其他设置项的占位。

#### 2.4 顶栏头像下拉菜单

如果顶栏右上角的圆形头像 `K` 当前没有交互，加点击下拉：

```vue
<el-dropdown @command="handleCommand">
  <div class="avatar-trigger">
    <span>K</span>
  </div>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item command="profile">个人资料</el-dropdown-item>
      <el-dropdown-item command="logout" divided>注销</el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
```

```ts
function handleCommand(cmd: string) {
  if (cmd === 'profile') router.push('/profile');
  if (cmd === 'logout') {
    userStore.logout();
    router.push('/login');
  }
}
```

#### 🔍 检查点 2：验证个人资料页

**用户做的事**：

1. 登录后从侧栏点"设置"，应该跳到个人资料页
2. 看到：
   - 大头像（银白→冷银→香槟金渐变，里面写大 K）
   - 名字 "Kaidi"、用户名 "@admin"、注册时间
   - 三栏统计：累计聆听、收藏数量、最爱艺术家（最爱艺术家有渐变文字效果）
   - 账户信息：用户名 / 注册时间
   - "注销账号" 按钮
3. 点"注销账号" → 弹出确认框 → 确认 → 跳回登录页
4. localStorage 应该被清空

---

### 任务 3：commit

```powershell
cd D:\Claude_Playground\Soundprint
git add frontend/
git status

@"
feat: Phase 7.8 修登录绕过 bug + 轻量个人资料页

登录流程修复:
- user store 默认状态改为空，不再硬编码 admin
- 加 restoreFromStorage 从 localStorage 恢复登录态
- main.ts 在 router 挂载前恢复登录态
- router guard：未登录访问任意非 /login 页 → 跳 /login
- router guard：已登录访问 /login → 跳 /
- LoginView.handleLogin 走 store.login() 流程

个人资料页（ProfileView.vue）:
- 大头像（银白→冷银→香槟金渐变，字母占位）
- 名字 + 用户名 + 注册时间
- 三栏统计：累计聆听、收藏曲目、最爱艺术家（金色渐变文字）
- 账户信息卡片
- 注销按钮（弹窗确认 → 清 localStorage → 跳 /login）

路由:
- /profile 新增
- /settings → 重定向到 /profile

顶栏:
- 右上角头像加下拉菜单：个人资料 / 注销

不动:
- Phase 7.5/7.6/7.7 视觉风格
- 任何后端代码
- 任何数据库表
- 现有功能：播放、转换、统计、上传
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM

git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
git push
```

---

## ✅ 完成检查清单

- [ ] 任务 1.1：user store 改为默认空 + 加 restoreFromStorage
- [ ] 任务 1.2：router guard 实现未登录跳 /login
- [ ] 任务 1.3：main.ts 在 router 前调用 restoreFromStorage
- [ ] 任务 1.4：LoginView.handleLogin 走 store.login()
- [ ] 检查点 1：清 localStorage 后访问 / 跳 /login
- [ ] 任务 2.1：ProfileView.vue 创建
- [ ] 任务 2.2：/profile 路由注册
- [ ] 任务 2.3：/settings 重定向到 /profile
- [ ] 任务 2.4：顶栏头像下拉菜单
- [ ] 检查点 2：个人资料页所有元素正常 + 注销流程正常
- [ ] 任务 3：commit + push

---

## 📩 反馈给架构师的内容

- **登录页绕过 bug 修复截图**：清空 localStorage 后访问 / 应跳 /login
- **个人资料页截图**：完整展示
- **注销流程截图/录屏**：点注销 → 弹窗确认 → 跳回登录页
- **顶栏头像下拉截图**：菜单弹出
- **commit hash**

---

## ⚠️ 注意事项

- **不要碰后端代码**——这是纯前端任务
- **不要加多用户系统**——admin 单用户演示就够
- **不要破坏 Phase 7.5/7.6/7.7 视觉**——配色、按钮、边框、侧栏激活态都保持
- **API 字段名以实际接口为准** —— `dashboard.totalPlays` 还是 `totalPlayCount` 等，按 Codex 看到的接口字段
- **dashboardApi / favoriteApi 应该已经存在**，不要新建，复用现有的
- **错误处理**：如果统计 API 报错，stats 用默认值 0/—，**不要让页面崩溃**

---

**End of Phase 7.8 Document.**
