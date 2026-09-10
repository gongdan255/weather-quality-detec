<template>
  <main class="dashboard">
    <header class="dashboard-header">
      <div class="header-title">
        <p>实时采集 · 空气质量监测驾驶舱</p>
        <h1>全国城市空气质量态势分析</h1>
      </div>
      <div class="header-actions">
        <span class="live-dot"></span>
        <span>实时数据</span>
        <strong>{{ nowText }}</strong>
      </div>
    </header>

    <section class="kpi-strip">
      <article class="kpi-card" :style="{ '--accent': 'var(--accent-cyan)' }">
        <span class="kpi-label">监测城市</span>
        <strong class="kpi-value">{{ points.length }}</strong>
        <em class="kpi-sub">覆盖 {{ provinceCount }} 个省级区域</em>
      </article>
      <article class="kpi-card" :style="{ '--accent': aqiLevelColor }">
        <span class="kpi-label">全国平均 AQI</span>
        <strong class="kpi-value">{{ avgAqi }}</strong>
        <em class="kpi-sub">{{ aqiSummary }}</em>
      </article>
      <article class="kpi-card" :style="{ '--accent': warningCities.length ? 'var(--accent-red)' : 'var(--accent-green)' }">
        <span class="kpi-label">预警城市</span>
        <strong class="kpi-value">{{ warningCities.length }}</strong>
        <em class="kpi-sub">AQI ≥ 150 自动标记</em>
      </article>
      <article class="kpi-card" :style="{ '--accent': 'var(--accent-green)' }">
        <span class="kpi-label">空气最佳区域</span>
        <strong class="kpi-value">{{ bestRegion }}</strong>
        <em class="kpi-sub">按区域平均 AQI 排序</em>
      </article>
    </section>

    <section class="content-grid">
      <aside class="side-stack">
        <ChartPanel title="高风险城市 TOP5" kicker="Warning Cities">
          <ul class="rank-list danger">
            <li v-for="(city, index) in topWarningCities" :key="city.city">
              <i>{{ index + 1 }}</i>
              <span>{{ city.city }}</span>
              <strong>AQI {{ city.aqi }}</strong>
            </li>
          </ul>
        </ChartPanel>

        <ChartPanel title="空气优良城市 TOP5" kicker="Clean Cities">
          <ul class="rank-list clean">
            <li v-for="(city, index) in cleanCities" :key="city.city">
              <i>{{ index + 1 }}</i>
              <span>{{ city.city }}</span>
              <strong>AQI {{ city.aqi }}</strong>
            </li>
          </ul>
        </ChartPanel>

        <ChartPanel title="PM2.5 / PM10 区域对比" kicker="Particle Matter">
          <BaseChart :option="pollutantOption" :loading="loading" />
        </ChartPanel>
      </aside>

      <ChartPanel title="全国空气质量空间分布" kicker="China AQI Map" class="map-panel">
        <ChinaAqiMap :points="points" :loading="loading" />
      </ChartPanel>

      <aside class="side-stack">
        <ChartPanel title="区域平均 AQI" kicker="Regional Rank">
          <BaseChart :option="regionBarOption" :loading="loading" />
        </ChartPanel>

        <ChartPanel title="区域预警占比" kicker="Risk Rate">
          <BaseChart :option="riskDonutOption" :loading="loading" />
        </ChartPanel>

        <ChartPanel title="优良率与预警统计" kicker="Quality Rate">
          <BaseChart :option="riskOption" :loading="loading" />
        </ChartPanel>
      </aside>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import BaseChart from '../components/BaseChart.vue'
import ChartPanel from '../components/ChartPanel.vue'
import ChinaAqiMap from '../components/ChinaAqiMap.vue'
import { fetchChinaMapPoints, fetchRegionComparison } from '../api/analysis'

const points = ref([])
const regions = ref([])
const loading = ref(true)
const nowText = new Intl.DateTimeFormat('zh-CN', {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
}).format(new Date())

onMounted(async () => {
  loading.value = true
  try {
    const [mapData, regionData] = await Promise.all([
      fetchChinaMapPoints(),
      fetchRegionComparison()
    ])
    points.value = mapData
    regions.value = regionData
  } finally {
    loading.value = false
  }
})

const avgAqi = computed(() => {
  if (!points.value.length) return 0
  const total = points.value.reduce((sum, item) => sum + Number(item.aqi || 0), 0)
  return Math.round(total / points.value.length)
})

const provinceCount = computed(() => new Set(points.value.map(item => item.province).filter(Boolean)).size)
const warningCities = computed(() => points.value.filter(item => item.warning || Number(item.aqi) >= 150))
const topWarningCities = computed(() => [...points.value].sort((a, b) => Number(b.aqi) - Number(a.aqi)).slice(0, 5))
const cleanCities = computed(() => [...points.value].sort((a, b) => Number(a.aqi) - Number(b.aqi)).slice(0, 5))

const bestRegion = computed(() => {
  const sorted = [...regions.value].sort((a, b) => Number(a.avgAqi) - Number(b.avgAqi))
  return sorted[0]?.region || '-'
})

const aqiSummary = computed(() => {
  if (avgAqi.value <= 50) return '整体空气质量优'
  if (avgAqi.value <= 100) return '整体处于良好水平'
  if (avgAqi.value <= 150) return '存在轻度污染风险'
  return '需重点关注污染扩散'
})

// 平均 AQI 对应的语义色，用于 KPI 卡片左侧色条
const aqiLevelColor = computed(() => {
  const v = avgAqi.value
  if (v <= 50) return 'var(--accent-green)'
  if (v <= 100) return 'var(--accent-lime)'
  if (v <= 150) return 'var(--accent-amber)'
  if (v <= 200) return 'var(--accent-orange)'
  if (v <= 300) return 'var(--accent-red)'
  return 'var(--accent-purple)'
})

const axisText = '#87a7b6'
const gridLine = 'rgba(139, 178, 193, 0.14)'
const chartColors = ['#36d399', '#63b3ed', '#f6c85f', '#f08c3a', '#d95f5f', '#9b6bd3', '#4fb6a8']

const regionBarOption = computed(() => ({
  color: chartColors,
  grid: { left: 36, right: 12, top: 24, bottom: 28 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: regions.value.map(item => item.region),
    axisLabel: { color: axisText, fontSize: 11 },
    axisLine: { lineStyle: { color: 'rgba(135, 167, 182, 0.28)' } }
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: axisText },
    splitLine: { lineStyle: { color: gridLine } }
  },
  series: [{
    type: 'bar',
    data: regions.value.map(item => Number(item.avgAqi)),
    barWidth: 18,
    itemStyle: {
      borderRadius: [3, 3, 0, 0],
      color: params => chartColors[params.dataIndex % chartColors.length]
    },
    label: { show: true, position: 'top', color: '#d9edf5', fontSize: 11 }
  }]
}))

const pollutantOption = computed(() => ({
  color: ['#4cc9f0', '#f6c85f'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, right: 8, textStyle: { color: axisText } },
  grid: { left: 42, right: 18, top: 42, bottom: 32 },
  xAxis: {
    type: 'category',
    data: regions.value.map(item => item.region),
    axisLabel: { color: axisText },
    axisLine: { lineStyle: { color: 'rgba(135, 167, 182, 0.28)' } }
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: axisText },
    splitLine: { lineStyle: { color: gridLine } }
  },
  series: [
    { name: 'PM2.5', type: 'line', smooth: true, symbolSize: 7, data: regions.value.map(item => Number(item.avgPm25)), areaStyle: { opacity: 0.12 } },
    { name: 'PM10', type: 'line', smooth: true, symbolSize: 7, data: regions.value.map(item => Number(item.avgPm10)), areaStyle: { opacity: 0.08 } }
  ]
}))

const riskOption = computed(() => ({
  color: ['#36d399', '#ff5d6c'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, right: 8, textStyle: { color: axisText } },
  grid: { left: 44, right: 36, top: 42, bottom: 32 },
  xAxis: {
    type: 'category',
    data: regions.value.map(item => item.region),
    axisLabel: { color: axisText },
    axisLine: { lineStyle: { color: 'rgba(135, 167, 182, 0.28)' } }
  },
  yAxis: [
    {
      type: 'value',
      axisLabel: { color: axisText, formatter: '{value}%' },
      splitLine: { lineStyle: { color: gridLine } }
    },
    { type: 'value', axisLabel: { color: axisText } }
  ],
  series: [
    { name: '优良率', type: 'bar', data: regions.value.map(item => Number(item.goodRate)), barWidth: 20, itemStyle: { borderRadius: [3, 3, 0, 0] } },
    { name: '预警数', type: 'line', yAxisIndex: 1, smooth: true, symbolSize: 7, data: regions.value.map(item => Number(item.warningCount)) }
  ]
}))

const riskDonutOption = computed(() => ({
  color: ['#ff5d6c', '#f6c85f', '#36d399'],
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['54%', '76%'],
    center: ['50%', '54%'],
    avoidLabelOverlap: true,
    label: { color: '#d9edf5', formatter: '{b}\n{d}%' },
    labelLine: { lineStyle: { color: 'rgba(217, 237, 245, 0.38)' } },
    data: [
      { name: '预警城市', value: warningCities.value.length },
      { name: '轻度风险', value: points.value.filter(item => Number(item.aqi) > 100 && Number(item.aqi) < 150).length },
      { name: '优良城市', value: points.value.filter(item => Number(item.aqi) <= 100).length }
    ]
  }]
}))
</script>
