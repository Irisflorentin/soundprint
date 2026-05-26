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
      :count="350"
      :magnet-radius="14"
      :ring-radius="10"
      :particle-size="1.8"
      :lerp-speed="0.1"
      color="#CBD5E1"
      :auto-animate="true"
      :wave-speed="0.35"
      :wave-amplitude="1.2"
      :rotation-speed="0.08"
      :depth-factor="1.4"
      :pulse-speed="2"
      particle-shape="capsule"
      :field-strength="12"
      :particle-variance="1.2"
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
