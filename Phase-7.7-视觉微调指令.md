# Phase 7.7 视觉微调指令（4 个 bug 一次性修复）

> Phase 7.6 之后的最后一波视觉精修。
> 完成后 Phase 7 大版本正式收官，进入 Phase 8。

---

## 给 Codex 的完整指令

请按下面 4 个 bug 修复，**所有改动都在样式层，不动业务逻辑**。

---

### Bug 1: SUCCESS 徽标改香槟金

**修改文件**：
- `frontend/src/styles/tokens.scss`
- `frontend/src/styles/element-overrides.scss`

**改动**：
```scss
/* tokens.scss */
/* 旧 */
--color-success:    #A3D9B1;
--color-success-bg: rgba(163, 217, 177, 0.12);
/* 新 */
--color-success:    #C8A862;
--color-success-bg: rgba(200, 168, 98, 0.12);
```

```scss
/* element-overrides.scss */
/* 旧 */
--el-color-success: #A3D9B1;
/* 新 */
--el-color-success: #C8A862;
```

**全局扫描硬编码绿色**：

```powershell
Select-String -Path "frontend\src\**\*.vue","frontend\src\**\*.ts","frontend\src\**\*.scss" `
    -Pattern "#A3D9B1|#86C594|#6CAB7C|#22C55E|163,\s*217,\s*177" -SimpleMatch
```

**替换映射**：

| 旧（哑光绿系） | 新（香槟金系） |
|---|---|
| `#A3D9B1` | `#C8A862` |
| `#86C594` | `#B89853` |
| `#6CAB7C` | `#A88D4E` |
| `#22C55E` | `#C8A862` |
| `rgba(163, 217, 177, X)` | `rgba(200, 168, 98, X)` |

**注意**：`danger` 红色 `#E89090` **保留**——用户需要区分"出错"和"成功"。

---

### Bug 2: 按钮 hover 不再变蓝

**根因**：Element-Plus 的 text/link 按钮 hover 走自己的色变量。

**修改文件**：`frontend/src/styles/element-overrides.scss`

**追加样式**（放在文件末尾）：

```scss
/* ========== 文本按钮 hover：不要默认天蓝 ========== */
.el-button.is-text:not(.is-disabled):hover,
.el-button.is-text:not(.is-disabled):focus {
  color: #F4F5F7;
  background-color: rgba(255, 255, 255, 0.06);
}

/* ========== 链接按钮 hover：用香槟金提示可交互 ========== */
.el-button.is-link:not(.is-disabled):hover,
.el-button.is-link:not(.is-disabled):focus {
  color: #C8A862;
}

/* ========== 默认按钮（无 type 或 type="default"）hover ========== */
.el-button:not(.el-button--primary):not(.el-button--success):not(.el-button--warning):not(.el-button--danger):not(.is-disabled):hover {
  color: #F4F5F7;
  border-color: rgba(255, 255, 255, 0.2);
  background-color: rgba(255, 255, 255, 0.04);
}

/* ========== "刷新"等带 type="info" 或 plain 按钮的 hover ========== */
.el-button--info:not(.is-disabled):hover {
  color: #F4F5F7;
  background-color: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.15);
}
```

---

### Bug 3: 纯白按钮改浅银 + 边框柔化

**根因**：`#FFFFFF` 在炭黑底反差过硬，违反"高级感"原则。

**修改文件**：`frontend/src/styles/element-overrides.scss`

**修改 `.el-button--primary` 块**：

```scss
/* 旧 */
.el-button--primary {
  --el-button-text-color: #0A0A0B;
  --el-button-bg-color: #F4F5F7;
  --el-button-border-color: #F4F5F7;
  --el-button-hover-text-color: #0A0A0B;
  --el-button-hover-bg-color: #FFFFFF;
  --el-button-hover-border-color: #FFFFFF;
  --el-button-active-bg-color: #E5E7EB;
  --el-button-active-border-color: #E5E7EB;
}

/* 新（浅银柔化） */
.el-button--primary {
  --el-button-text-color: #0A0A0B;
  --el-button-bg-color: #CBD5E1;          /* 浅银 */
  --el-button-border-color: #CBD5E1;
  --el-button-hover-text-color: #0A0A0B;
  --el-button-hover-bg-color: #E5E7EB;    /* hover 提亮 */
  --el-button-hover-border-color: #E5E7EB;
  --el-button-active-bg-color: #94A3B8;   /* active 压暗 */
  --el-button-active-border-color: #94A3B8;
}
```

**边框柔化**——找到 `:root` 块内的 `--el-border-color`：

```scss
/* 旧 */
--el-border-color:               rgba(255, 255, 255, 0.08);
--el-border-color-light:         rgba(255, 255, 255, 0.05);
--el-border-color-lighter:       rgba(255, 255, 255, 0.03);
--el-border-color-extra-light:   rgba(255, 255, 255, 0.02);

/* 新（一档更淡） */
--el-border-color:               rgba(255, 255, 255, 0.04);
--el-border-color-light:         rgba(255, 255, 255, 0.03);
--el-border-color-lighter:       rgba(255, 255, 255, 0.02);
--el-border-color-extra-light:   rgba(255, 255, 255, 0.015);
```

`tokens.scss` 里的 `--color-border` 同步：

```scss
/* 旧 */
--color-border:       rgba(255, 255, 255, 0.06);
--color-border-hover: rgba(255, 255, 255, 0.12);

/* 新 */
--color-border:       rgba(255, 255, 255, 0.04);
--color-border-hover: rgba(255, 255, 255, 0.08);
```

---

### Bug 4: 侧栏激活态去除整圈边框

**根因**：当前 active 状态可能用了 `border: 1px solid xxx` 整圈边框，违反"克制"原则。

**修改文件**：找到侧栏导航组件（`SidebarNav.vue` 或 `Sidebar.vue` 或 `MainLayout.vue` 内部）

**找到** `.nav-item.active` 或 `.menu-item.active` 或 `&.is-active` 等样式块。

**确保如下结构**：

```scss
.nav-item {
  /* ... 常规样式 ... */

  &.active,
  &.is-active,
  &.router-link-active {
    background: transparent;              /* 不要色块背景 */
    color: var(--color-fg-primary);
    font-weight: 600;
    position: relative;
    border: none;                          /* 关键：清除整圈描边 */
    box-shadow: none;                      /* 清除阴影描边 */

    /* 仅左侧 2px 香槟金竖线 */
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      bottom: 8px;
      width: 2px;
      background: #C8A862;
      border-radius: 2px;
    }

    /* 图标也变成银白 */
    .el-icon,
    svg {
      color: var(--color-fg-primary);
    }
  }
}
```

**如果发现 active 当前用了 `border: 1px solid ...`**，删除该行。

---

## 验证清单

完成后浏览器验证（不重启后端）：

- [ ] 转换工坊历史 SUCCESS 徽标是**香槟金**（不是绿）
- [ ] "上传音乐"/"查询"按钮是**浅银 #CBD5E1**（不是死白）
- [ ] 鼠标 hover "刷新"按钮 → 文字银白 + 微光背景（**不再变蓝**）
- [ ] 输入框/卡片边框比之前更柔（**几乎不可见但还在**）
- [ ] 侧栏选中项**没有整圈描边**，只有左侧香槟金竖线
- [ ] 其他位置视觉无意外退化

---

## commit

```powershell
cd D:\Claude_Playground\Soundprint
git add frontend/src/styles/
git status

@"
fix: Phase 7.7 视觉微调 - SUCCESS 改金 + 按钮 hover + 浅银主按钮 + 侧栏边框

- SUCCESS 徽标 #A3D9B1 → #C8A862 香槟金，全站状态色统一
- 按钮 text/link/default/info hover 不再用 Element-Plus 默认天蓝
- 主按钮（el-button--primary）背景 #F4F5F7 → #CBD5E1 浅银，柔和不刺眼
- hover 态 #E5E7EB 提亮、active 态 #94A3B8 压暗，三层立体
- 全局边框 rgba(255,255,255,0.08) → 0.04，更克制
- 侧栏激活态清除整圈 border，仅保留左侧 2px 香槟金竖线
- danger 红色保留 #E89090（功能性区分需要）
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM

git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
git push
```

---

## 注意事项

- **不动任何业务逻辑**——只改样式
- **不重启后端**——Vite HMR 自动重载
- **Phase 7.5/7.6 已有效果保持不变** —— 配色、布局、组件结构都不动
- **改完后告诉用户 commit hash + 浏览器验证状态**
