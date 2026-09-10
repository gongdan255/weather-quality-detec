import axios from 'axios'
import { mapPoints, regionComparison } from '../data/demoData'

const client = axios.create({
  baseURL: '/api',
  timeout: 5000
})

export async function fetchChinaMapPoints() {
  try {
    const { data } = await client.get('/analysis/china-map')
    return Array.isArray(data) && data.length ? data : mapPoints
  } catch (error) {
    console.warn('使用前端演示地图数据', error)
    return mapPoints
  }
}

export async function fetchRegionComparison() {
  try {
    const { data } = await client.get('/analysis/region-comparison')
    return Array.isArray(data) && data.length ? data : regionComparison
  } catch (error) {
    console.warn('使用前端演示区域数据', error)
    return regionComparison
  }
}
