<template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  option: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartRef = ref()
let chart
let resizeObserver

function render() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption(props.option, true)
  syncLoading()
}

function resize() {
  chart?.resize()
}

function syncLoading() {
  if (!chart) return
  if (props.loading) {
    chart.showLoading('default', {
      text: '',
      color: '#54d6df',
      maskColor: 'rgba(7, 16, 26, 0.6)',
      lineWidth: 2
    })
  } else {
    chart.hideLoading()
  }
}

onMounted(async () => {
  await nextTick()
  render()
  window.addEventListener('resize', resize)
  // 监听容器尺寸变化（粘性布局 / 弹性高度下窗口 resize 事件可能不够）
  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => resize())
    resizeObserver.observe(chartRef.value)
  }
})

watch(() => props.option, render, { deep: true })
watch(() => props.loading, syncLoading)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  resizeObserver?.disconnect()
  chart?.dispose()
})
</script>
