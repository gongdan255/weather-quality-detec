<template>
  <BaseChart :option="option" :loading="loading" />
</template>

<script setup>
import { computed } from 'vue'
import * as echarts from 'echarts'
import chinaJson from '../data/china.json'
import BaseChart from './BaseChart.vue'

echarts.registerMap('china', chinaJson)

const props = defineProps({
  points: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const provinceData = computed(() => {
  const grouped = new Map()
  props.points.forEach(item => {
    const province = normalizeProvinceName(item.province)
    if (!province) return
    if (!grouped.has(province)) {
      grouped.set(province, {
        name: province,
        value: 0,
        cityCount: 0,
        warningCount: 0,
        maxCity: '',
        maxAqi: 0,
        totalAqi: 0
      })
    }
    const row = grouped.get(province)
    const aqi = Number(item.aqi || 0)
    row.cityCount += 1
    row.totalAqi += aqi
    row.warningCount += item.warning || aqi >= 150 ? 1 : 0
    if (aqi > row.maxAqi) {
      row.maxAqi = aqi
      row.maxCity = item.city
    }
  })

  return Array.from(grouped.values()).map(item => ({
    ...item,
    value: item.cityCount ? Number((item.totalAqi / item.cityCount).toFixed(1)) : 0
  }))
})

const cityPoints = computed(() => props.points
  .map((item, index) => {
    const coord = resolveCoord(item, index)
    return coord ? {
      name: item.city,
      value: [coord[0], coord[1], Number(item.aqi || 0)],
      ...item
    } : null
  })
  .filter(Boolean))

const warningPoints = computed(() => cityPoints.value
  .filter(item => item.warning || Number(item.aqi) >= 150))

const option = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'item',
    borderWidth: 0,
    padding: [10, 14],
    backgroundColor: 'rgba(10, 20, 30, 0.94)',
    borderColor: 'rgba(84, 214, 223, 0.3)',
    textStyle: { color: '#f7fbff', fontSize: 13 },
    extraCssText: 'border-radius: 8px; box-shadow: 0 8px 24px rgba(0,0,0,0.4);',
    formatter(params) {
      const data = params.data
      if (!data) return `${params.name}<br/>暂无实时数据`
      if (params.seriesType === 'scatter' || params.seriesType === 'effectScatter') {
        return [
          `<b>${data.city}</b> / ${data.region || '-'}`,
          `AQI：${data.aqi}`,
          `PM2.5：${data.pm25 ?? '-'}`,
          `PM10：${data.pm10 ?? '-'}`,
          `等级：${data.qualityLevel || '-'}`
        ].join('<br/>')
      }
      return [
        `<b>${params.name}</b>`,
        `省均 AQI：${data.value}`,
        `监测城市：${data.cityCount}`,
        `预警城市：${data.warningCount}`,
        `最高城市：${data.maxCity || '-'} ${data.maxAqi ? `AQI ${data.maxAqi}` : ''}`
      ].join('<br/>')
    }
  },
  visualMap: {
    min: 0,
    max: 180,
    left: 14,
    bottom: 12,
    itemWidth: 10,
    itemHeight: 110,
    text: ['污染高', '空气优'],
    textStyle: { color: '#7fa0ad', fontSize: 11 },
    calculable: true,
    inRange: {
      color: ['#35d07f', '#b8d95b', '#f0c94b', '#ef8d32', '#d64d57', '#8d4ec8']
    }
  },
  geo: {
    map: 'china',
    roam: true,
    layoutCenter: ['50%', '48%'],
    layoutSize: '92%',
    center: [103.5, 36.2],
    zoom: 1.02,
    scaleLimit: { min: 0.8, max: 4 },
    label: {
      show: true,
      color: 'rgba(224, 241, 246, 0.72)',
      fontSize: 10
    },
    itemStyle: {
      areaColor: '#163a4e',
      borderColor: 'rgba(118, 205, 222, 0.72)',
      borderWidth: 0.8,
      shadowColor: 'rgba(0, 0, 0, 0.55)',
      shadowBlur: 22,
      shadowOffsetX: 6,
      shadowOffsetY: 12
    },
    emphasis: {
      label: { color: '#ffffff' },
      itemStyle: {
        areaColor: '#2c7378',
        shadowColor: 'rgba(89, 220, 208, 0.32)',
        shadowBlur: 22
      }
    }
  },
  series: [
    {
      name: '省均 AQI',
      type: 'map',
      map: 'china',
      geoIndex: 0,
      z: 2,
      data: provinceData.value
    },
    {
      name: '监测城市',
      type: 'scatter',
      coordinateSystem: 'geo',
      geoIndex: 0,
      z: 5,
      symbolSize: val => Math.max(5, Math.min(13, val[2] / 14)),
      itemStyle: {
        color: '#5eead4',
        borderColor: '#eaffff',
        borderWidth: 0.7,
        shadowBlur: 10,
        shadowColor: 'rgba(94, 234, 212, 0.5)'
      },
      data: cityPoints.value
    },
    {
      name: '高危污染城市',
      type: 'effectScatter',
      coordinateSystem: 'geo',
      geoIndex: 0,
      z: 6,
      symbolSize: val => Math.max(10, Math.min(24, val[2] / 8)),
      rippleEffect: { brushType: 'stroke', scale: 3.6 },
      itemStyle: {
        color: '#ff4d5d',
        shadowBlur: 18,
        shadowColor: '#ff4d5d'
      },
      data: warningPoints.value
    }
  ]
}))

function resolveCoord(item, index) {
  if (cityCoord[item.city]) return cityCoord[item.city]
  const center = provinceCenter[item.province] || provinceCenter[normalizeShortProvince(item.province)]
  if (!center) return null
  const seed = Math.abs(hashCode(`${item.city}-${index}`))
  const angle = (seed % 360) * Math.PI / 180
  const radius = 0.55 + (seed % 100) / 180
  return [
    Number((center[0] + Math.cos(angle) * radius).toFixed(4)),
    Number((center[1] + Math.sin(angle) * radius * 0.72).toFixed(4))
  ]
}

function normalizeProvinceName(name) {
  if (normalProvinceNameMap[name]) return normalProvinceNameMap[name]
  const mapName = provinceNameMap[name] || name
  if (!mapName) return ''
  if (['北京', '天津', '上海', '重庆'].includes(mapName)) return `${mapName}市`
  if (mapName === '内蒙古') return '内蒙古自治区'
  if (mapName === '广西') return '广西壮族自治区'
  if (mapName === '西藏') return '西藏自治区'
  if (mapName === '宁夏') return '宁夏回族自治区'
  if (mapName === '新疆') return '新疆维吾尔自治区'
  if (['香港', '澳门'].includes(mapName)) return `${mapName}特别行政区`
  if (mapName === '台湾') return '台湾省'
  return mapName.endsWith('省') || mapName.endsWith('市') || mapName.endsWith('自治区') ? mapName : `${mapName}省`
}

function normalizeShortProvince(name) {
  if (normalShortProvinceMap[name]) return normalShortProvinceMap[name]
  return provinceNameMap[name] || String(name || '')
    .replace('省', '')
    .replace('市', '')
    .replace('维吾尔自治区', '')
    .replace('回族自治区', '')
    .replace('壮族自治区', '')
    .replace('自治区', '')
}

function hashCode(text) {
  let hash = 0
  for (let i = 0; i < text.length; i++) {
    hash = ((hash << 5) - hash) + text.charCodeAt(i)
    hash |= 0
  }
  return hash
}

const provinceNameMap = {
  北京市: '北京', 天津市: '天津', 上海市: '上海', 重庆市: '重庆',
  河北省: '河北', 山西省: '山西', 辽宁省: '辽宁', 吉林省: '吉林', 黑龙江省: '黑龙江',
  江苏省: '江苏', 浙江省: '浙江', 安徽省: '安徽', 福建省: '福建', 江西省: '江西',
  山东省: '山东', 河南省: '河南', 湖北省: '湖北', 湖南省: '湖南', 广东省: '广东',
  海南省: '海南', 四川省: '四川', 贵州省: '贵州', 云南省: '云南', 陕西省: '陕西',
  甘肃省: '甘肃', 青海省: '青海', 新疆维吾尔自治区: '新疆', 宁夏回族自治区: '宁夏',
  广西壮族自治区: '广西', 内蒙古自治区: '内蒙古', 西藏自治区: '西藏'
}

const provinceCenter = {
  北京: [116.4, 40.1], 天津: [117.2, 39.2], 河北: [115.2, 38.3], 山西: [112.4, 37.8],
  内蒙古: [111.8, 42.2], 辽宁: [123.3, 41.6], 吉林: [126.2, 43.7], 黑龙江: [127.8, 47.2],
  上海: [121.4, 31.2], 江苏: [119.2, 32.9], 浙江: [120.1, 29.2], 安徽: [117.2, 31.8],
  福建: [118.1, 26.1], 江西: [115.7, 27.6], 山东: [118.1, 36.4], 河南: [113.5, 33.9],
  湖北: [112.4, 30.9], 湖南: [112.7, 27.7], 广东: [113.4, 23.4], 广西: [108.7, 23.8],
  海南: [110.3, 19.2], 重庆: [107.8, 30.0], 四川: [103.6, 30.7], 贵州: [106.7, 26.8],
  云南: [101.7, 24.8], 西藏: [88.8, 31.5], 陕西: [108.9, 34.4], 甘肃: [103.8, 37.8],
  青海: [96.0, 35.8], 宁夏: [106.2, 37.3], 新疆: [85.6, 41.8]
}

const cityCoord = {
  北京: [116.4074, 39.9042], 天津: [117.2009, 39.0842], 上海: [121.4737, 31.2304], 重庆: [106.5516, 29.563],
  哈尔滨: [126.6424, 45.7567], 齐齐哈尔: [123.9182, 47.3543], 牡丹江: [129.6332, 44.5517], 佳木斯: [130.3189, 46.7999],
  大庆: [125.1031, 46.5893], 鸡西: [130.9693, 45.2951], 双鸭山: [131.1416, 46.6762], 伊春: [128.8409, 47.7283],
  七台河: [131.0031, 45.7717], 鹤岗: [130.2979, 47.3499], 黑河: [127.5285, 50.2452], 绥化: [126.9689, 46.6538],
  大兴安岭: [124.7111, 52.3352],
  石家庄: [114.5149, 38.0428], 唐山: [118.1802, 39.6309], 太原: [112.5489, 37.8706], 呼和浩特: [111.7492, 40.8426],
  南京: [118.7969, 32.0603], 杭州: [120.1551, 30.2741], 合肥: [117.2272, 31.8206], 福州: [119.2965, 26.0745],
  南昌: [115.8582, 28.6829], 济南: [117.1201, 36.6512], 青岛: [120.3826, 36.0671], 郑州: [113.6254, 34.7466],
  武汉: [114.3055, 30.5928], 长沙: [112.9388, 28.2282], 广州: [113.2644, 23.1291], 深圳: [114.0579, 22.5431],
  南宁: [108.3669, 22.817], 海口: [110.1983, 20.0442], 成都: [104.0665, 30.5728], 贵阳: [106.6302, 26.647],
  昆明: [102.8329, 24.8801], 拉萨: [91.1322, 29.6604], 西安: [108.9398, 34.3416], 兰州: [103.8343, 36.0611],
  西宁: [101.7782, 36.6171], 银川: [106.2309, 38.4872], 乌鲁木齐: [87.6168, 43.8256], 沈阳: [123.4315, 41.8057],
  大连: [121.6147, 38.914], 长春: [125.3235, 43.8171]
}

const normalProvinceNameMap = {
  北京: '北京市', 天津: '天津市', 上海: '上海市', 重庆: '重庆市',
  河北: '河北省', 山西: '山西省', 辽宁: '辽宁省', 吉林: '吉林省', 黑龙江: '黑龙江省',
  江苏: '江苏省', 浙江: '浙江省', 安徽: '安徽省', 福建: '福建省', 江西: '江西省', 山东: '山东省',
  河南: '河南省', 湖北: '湖北省', 湖南: '湖南省', 广东: '广东省', 海南: '海南省',
  四川: '四川省', 贵州: '贵州省', 云南: '云南省', 陕西: '陕西省', 甘肃: '甘肃省', 青海: '青海省',
  内蒙古: '内蒙古自治区', 广西: '广西壮族自治区', 西藏: '西藏自治区', 宁夏: '宁夏回族自治区', 新疆: '新疆维吾尔自治区'
}

const normalShortProvinceMap = {
  北京市: '北京', 天津市: '天津', 上海市: '上海', 重庆市: '重庆',
  河北省: '河北', 山西省: '山西', 辽宁省: '辽宁', 吉林省: '吉林', 黑龙江省: '黑龙江',
  江苏省: '江苏', 浙江省: '浙江', 安徽省: '安徽', 福建省: '福建', 江西省: '江西', 山东省: '山东',
  河南省: '河南', 湖北省: '湖北', 湖南省: '湖南', 广东省: '广东', 海南省: '海南',
  四川省: '四川', 贵州省: '贵州', 云南省: '云南', 陕西省: '陕西', 甘肃省: '甘肃', 青海省: '青海',
  内蒙古自治区: '内蒙古', 广西壮族自治区: '广西', 西藏自治区: '西藏', 宁夏回族自治区: '宁夏', 新疆维吾尔自治区: '新疆',
  ...Object.fromEntries(Object.keys(normalProvinceNameMap).map(name => [name, name]))
}
</script>
