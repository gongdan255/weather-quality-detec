<template>
  <section class="chart-panel">
    <div class="panel-header">
      <div>
        <p class="panel-kicker">{{ kicker }}</p>
        <h2>{{ title }}</h2>
      </div>
      <slot name="extra" />
    </div>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  kicker: {
    type: String,
    default: 'Analysis'
  }
})
</script>
