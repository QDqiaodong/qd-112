<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">首页概览</h2>

    <div class="grid grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover fade-in stagger-1">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">工具总数</p>
            <p class="text-3xl font-bold text-primary mt-1">{{ totalToolsCount }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 rounded-lg flex items-center justify-center">
            <Hammer :size="24" class="text-primary" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover fade-in stagger-2">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待保养</p>
            <p class="text-3xl font-bold text-accent mt-1">{{ overview?.dueMaintenanceCount || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-50 rounded-lg flex items-center justify-center">
            <Shield :size="24" class="text-accent" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover fade-in stagger-3">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">在位</p>
            <p class="text-3xl font-bold text-primary mt-1">{{ overview?.statusCounts?.AVAILABLE || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 rounded-lg flex items-center justify-center">
            <CheckCircle :size="24" class="text-primary" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover fade-in stagger-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">在位率</p>
            <p class="text-3xl font-bold text-primary mt-1">{{ inPlaceRate }}%</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 rounded-lg flex items-center justify-center">
            <Clock :size="24" class="text-primary" />
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-2 gap-6 mb-8">
      <div class="bg-white rounded-lg p-6 shadow-sm">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">分类占比</h3>
        <div ref="chartRef" class="h-64"></div>
      </div>
      <div class="bg-white rounded-lg p-6 shadow-sm">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">最近活动</h3>
        <div class="space-y-4 max-h-64 overflow-auto">
          <div
            v-for="activity in overview?.recentActivities || []"
            :key="activity.id"
            class="flex items-start gap-3"
          >
            <div class="w-2 h-2 rounded-full bg-primary mt-2 shrink-0"></div>
            <div>
              <p class="text-sm text-gray-700">{{ activity.content }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ activity.time }}</p>
            </div>
          </div>
          <div v-if="!overview?.recentActivities?.length" class="text-center text-gray-400 py-8">
            暂无活动记录
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg p-6 shadow-sm">
      <h3 class="text-lg font-semibold text-gray-700 mb-4">待保养提醒</h3>
      <div class="space-y-3">
        <div
          v-for="item in overview?.dueMaintenanceTools || []"
          :key="item.id"
          class="flex items-center justify-between p-3 rounded-lg bg-gray-50"
        >
          <div class="flex items-center gap-3">
            <Clock :size="18" class="text-gray-400" />
            <span class="text-sm font-medium text-gray-700">{{ item.name }}</span>
          </div>
          <div class="text-sm text-gray-400">
            即将到期 · {{ item.nextMaintenanceDate }}
          </div>
        </div>
        <div v-if="!overview?.dueMaintenanceTools?.length" class="text-center text-gray-400 py-8">
          暂无待保养工具
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { Hammer, Shield, Clock, CheckCircle } from 'lucide-vue-next'
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()
const route = useRoute()
const overview = computed(() => dashboardStore.overview)
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const totalToolsCount = computed(() => {
  if (!overview.value?.statusCounts) return 0
  return Object.values(overview.value.statusCounts).reduce((sum, count) => sum + (count || 0), 0)
})

const inPlaceRate = computed(() => {
  const total = totalToolsCount.value
  if (!total) return '0.0'
  const available = overview.value?.statusCounts?.AVAILABLE || 0
  return ((available / total) * 100).toFixed(1)
})

async function loadOverview() {
  await dashboardStore.fetchOverview()
  await nextTick()
  initChart()
}

onMounted(() => {
  loadOverview()
})

watch(() => route.path, (newPath) => {
  if (newPath === '/') {
    loadOverview()
  }
})

watch(() => overview.value?.categoryStats, () => {
  nextTick(() => initChart())
}, { deep: true })

function initChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const stats = overview.value?.categoryStats || []
  chartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    color: ['#1e40af', '#3b82f6', '#f97316', '#fb923c', '#6366f1', '#8b5cf6', '#14b8a6', '#f59e0b'],
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: stats.length ? stats : [{ name: '暂无数据', value: 1 }],
        label: { show: true, formatter: '{b}\n{d}%' },
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.3)' } }
      }
    ]
  })
}
</script>
