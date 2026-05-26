<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';

const props = withDefaults(defineProps<{
  label: string;
  value: number;
  unit?: string;
  icon?: string;
  accent?: 'brand' | 'accent' | 'success' | 'warning';
}>(), {
  unit: '',
  icon: '',
  accent: 'brand',
});

const displayValue = ref(0);

const accentClass = computed(() => `accent-${props.accent}`);

onMounted(() => animateTo(props.value));
watch(() => props.value, animateTo);

function animateTo(nextValue: number) {
  const startValue = displayValue.value;
  const delta = nextValue - startValue;
  const duration = 650;
  const startedAt = performance.now();

  function tick(now: number) {
    const progress = Math.min((now - startedAt) / duration, 1);
    const eased = 1 - Math.pow(1 - progress, 3);
    displayValue.value = Math.round(startValue + delta * eased);
    if (progress < 1) requestAnimationFrame(tick);
  }

  requestAnimationFrame(tick);
}
</script>

<template>
  <article class="kpi-card" :class="accentClass">
    <span v-if="icon" class="icon">{{ icon }}</span>
    <div class="content">
      <span class="label">{{ label }}</span>
      <strong class="value">
        {{ displayValue.toLocaleString() }}
        <small v-if="unit">{{ unit }}</small>
      </strong>
    </div>
  </article>
</template>

<style lang="scss" scoped>
.kpi-card {
  position: relative;
  min-width: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.035);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, #F4F5F7, #94A3B8, #C8A862);
  }
}

.accent-success::before { background: linear-gradient(90deg, #F4F5F7, #A3D9B1); }
.accent-warning::before { background: linear-gradient(90deg, #F4F5F7, #C8A862); }
.accent-accent::before  { background: linear-gradient(90deg, #F4F5F7, #94A3B8, #C8A862); }

.icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 22px;
  background: color-mix(in srgb, var(--color-brand) 15%, transparent);
}

.content {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  color: var(--color-fg-secondary);
  font-size: 13px;
}

.value {
  color: var(--color-fg-primary);
  font-size: 28px;
  line-height: 1;
  letter-spacing: 0;

  small {
    margin-left: 4px;
    color: var(--color-fg-tertiary);
    font-size: 13px;
    font-weight: 500;
  }
}

.accent-accent .icon { background: color-mix(in srgb, var(--color-accent) 15%, transparent); }
.accent-success .icon { background: var(--color-success-bg); }
.accent-warning .icon { background: var(--color-warning-bg); }
</style>
