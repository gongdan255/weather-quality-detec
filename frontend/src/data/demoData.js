export const mapPoints = [
  { province: '北京', region: '华北', city: '北京', aqi: 86, pm25: 51, pm10: 88, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '天津', region: '华北', city: '天津', aqi: 112, pm25: 71, pm10: 122, qualityLevel: '轻度污染', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '河北', region: '华北', city: '石家庄', aqi: 156, pm25: 96, pm10: 171, qualityLevel: '中度污染', warning: true, monitorTime: '2026-07-04T15:00:00' },
  { province: '上海', region: '华东', city: '上海', aqi: 42, pm25: 22, pm10: 48, qualityLevel: '优', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '江苏', region: '华东', city: '南京', aqi: 65, pm25: 38, pm10: 74, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '浙江', region: '华东', city: '杭州', aqi: 58, pm25: 31, pm10: 64, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '广东', region: '华南', city: '广州', aqi: 39, pm25: 19, pm10: 43, qualityLevel: '优', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '广西', region: '华南', city: '南宁', aqi: 55, pm25: 28, pm10: 62, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '湖北', region: '华中', city: '武汉', aqi: 94, pm25: 56, pm10: 99, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '湖南', region: '华中', city: '长沙', aqi: 78, pm25: 44, pm10: 85, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '四川', region: '西南', city: '成都', aqi: 121, pm25: 76, pm10: 137, qualityLevel: '轻度污染', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '重庆', region: '西南', city: '重庆', aqi: 107, pm25: 66, pm10: 118, qualityLevel: '轻度污染', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '陕西', region: '西北', city: '西安', aqi: 168, pm25: 104, pm10: 183, qualityLevel: '中度污染', warning: true, monitorTime: '2026-07-04T15:00:00' },
  { province: '甘肃', region: '西北', city: '兰州', aqi: 132, pm25: 82, pm10: 149, qualityLevel: '轻度污染', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '辽宁', region: '东北', city: '沈阳', aqi: 88, pm25: 52, pm10: 91, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' },
  { province: '黑龙江', region: '东北', city: '哈尔滨', aqi: 72, pm25: 41, pm10: 80, qualityLevel: '良', warning: false, monitorTime: '2026-07-04T15:00:00' }
]

export const regionComparison = [
  { region: '华北', avgAqi: 118, avgPm25: 73, avgPm10: 127, cityCount: 24, goodCityCount: 11, pollutedCityCount: 13, goodRate: 45.8, warningCount: 4 },
  { region: '华东', avgAqi: 57, avgPm25: 32, avgPm10: 65, cityCount: 36, goodCityCount: 31, pollutedCityCount: 5, goodRate: 86.1, warningCount: 0 },
  { region: '华南', avgAqi: 49, avgPm25: 24, avgPm10: 55, cityCount: 18, goodCityCount: 17, pollutedCityCount: 1, goodRate: 94.4, warningCount: 0 },
  { region: '华中', avgAqi: 86, avgPm25: 50, avgPm10: 92, cityCount: 16, goodCityCount: 11, pollutedCityCount: 5, goodRate: 68.8, warningCount: 1 },
  { region: '西南', avgAqi: 114, avgPm25: 71, avgPm10: 128, cityCount: 20, goodCityCount: 8, pollutedCityCount: 12, goodRate: 40, warningCount: 2 },
  { region: '西北', avgAqi: 145, avgPm25: 90, avgPm10: 162, cityCount: 17, goodCityCount: 5, pollutedCityCount: 12, goodRate: 29.4, warningCount: 5 },
  { region: '东北', avgAqi: 80, avgPm25: 47, avgPm10: 86, cityCount: 14, goodCityCount: 10, pollutedCityCount: 4, goodRate: 71.4, warningCount: 1 }
]
