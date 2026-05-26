<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue';
import Balatro from '@/components/vue-bits/animations/Balatro.vue';
import { releaseWebglContexts } from '@/utils/webgl';

const root = ref<HTMLElement | null>(null);

onBeforeUnmount(() => {
  releaseWebglContexts(root.value);
});
</script>

<template>
  <div ref="root" class="soundprint-balatro">
    <Balatro
      class="balatro-canvas"
      :spin-rotation="-1.5"
      :spin-speed="5"
      color1="#7C3AED"
      color2="#06B6D4"
      color3="#0F0F1E"
      :contrast="2.8"
      :lighting="0.5"
      :spin-amount="0.3"
      :pixel-filter="800"
      :spin-ease="1"
      :is-rotate="true"
      :mouse-interaction="false"
    />
  </div>
</template>

<style lang="scss" scoped>
.soundprint-balatro,
.balatro-canvas {
  width: 100%;
  height: 100%;
}

.soundprint-balatro :deep(canvas) {
  z-index: 0 !important;
}
</style>
