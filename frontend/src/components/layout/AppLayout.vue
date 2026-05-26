<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import SidebarNav from './SidebarNav.vue';
import TopBar from './TopBar.vue';
import PlayerBar from './PlayerBar.vue';

const route = useRoute();
const hideChrome = computed(() => route.meta.hideChrome === true);
</script>

<template>
  <div v-if="hideChrome" class="full-page">
    <router-view />
  </div>

  <div v-else class="app-layout">
    <SidebarNav class="sidebar" />
    <div class="main-area">
      <TopBar class="top-bar" />
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    <PlayerBar class="player-bar" />
  </div>
</template>

<style lang="scss" scoped>
.app-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  grid-template-rows: 1fr 88px;
  grid-template-areas:
    "sidebar main"
    "player  player";
  height: 100vh;
  background: var(--color-bg-base);
}

.sidebar    { grid-area: sidebar; border-right: 1px solid rgba(255,255,255,0.05); }
.main-area  { grid-area: main; display: flex; flex-direction: column; overflow: hidden; }
.player-bar { grid-area: player; border-top: 1px solid rgba(200,168,98,0.28); }

.top-bar    { flex-shrink: 0; height: 64px; }
.content    { flex: 1; overflow-y: auto; padding: var(--space-6); }

.full-page  { height: 100vh; background: var(--color-bg-base); }
</style>
