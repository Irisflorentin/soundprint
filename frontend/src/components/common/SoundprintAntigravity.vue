<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue';
import Antigravity from '@/components/vue-bits/animations/Antigravity.vue';
import { releaseWebglContexts } from '@/utils/webgl';

const root = ref<HTMLElement | null>(null);

onBeforeUnmount(() => {
  releaseWebglContexts(root.value);
});
</script>

<template>
  <div ref="root" class="soundprint-antigravity">
    <Antigravity
      class="antigravity-canvas"
      :count="200"
      :magnet-radius="8"
      :ring-radius="6"
      :particle-size="1.5"
      color="#7C3AED"
      :auto-animate="true"
      :wave-speed="0.3"
      :wave-amplitude="0.8"
      :rotation-speed="0.05"
      :depth-factor="1.2"
      :pulse-speed="2"
      particle-shape="capsule"
    />
  </div>
</template>

<style lang="scss" scoped>
.soundprint-antigravity,
.antigravity-canvas {
  width: 100%;
  height: 100%;
}
</style>
