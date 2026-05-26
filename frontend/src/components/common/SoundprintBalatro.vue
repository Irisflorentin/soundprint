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
      color1="#F4F5F7"
      color2="#94A3B8"
      color3="#0A0A0B"
      :contrast="2.5"
      :lighting="0.45"
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
