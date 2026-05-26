<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue';
import Galaxy from '@/components/vue-bits/backgrounds/Galaxy.vue';
import { releaseWebglContexts } from '@/utils/webgl';

withDefaults(defineProps<{
  hueShift?: number;
  transparent?: boolean;
  density?: number;
  glowIntensity?: number;
  twinkleIntensity?: number;
  rotationSpeed?: number;
  saturation?: number;
  mouseInteraction?: boolean;
  mouseRepulsion?: boolean;
}>(), {
  hueShift: 210,
  transparent: true,
  density: 0.9,
  glowIntensity: 0.35,
  twinkleIntensity: 0.45,
  rotationSpeed: 0.04,
  saturation: 0.15,
  mouseInteraction: true,
  mouseRepulsion: true,
});

const root = ref<HTMLElement | null>(null);

onBeforeUnmount(() => {
  releaseWebglContexts(root.value);
});
</script>

<template>
  <div ref="root" class="soundprint-galaxy">
    <Galaxy
      class="galaxy-canvas"
      :hue-shift="hueShift"
      :density="density"
      :glow-intensity="glowIntensity"
      :twinkle-intensity="twinkleIntensity"
      :rotation-speed="rotationSpeed"
      :saturation="saturation"
      :mouse-interaction="mouseInteraction"
      :mouse-repulsion="mouseRepulsion"
      :transparent="transparent"
    />
  </div>
</template>

<style lang="scss" scoped>
.soundprint-galaxy,
.galaxy-canvas {
  width: 100%;
  height: 100%;
}

.soundprint-galaxy {
  filter: saturate(0.92);
}
</style>
