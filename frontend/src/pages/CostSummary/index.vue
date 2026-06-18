<template>
  <div class="p-6">
    <div class="mb-6">
      <h2 class="text-2xl font-bold text-gray-800">费用汇总</h2>
      <p class="text-sm text-gray-500 mt-1">按分类、位置和月份分析家庭工具投入成本</p>
    </div>

    <div class="grid grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">采购总金额</p>
            <p class="text-3xl font-bold text-primary mt-1">¥ {{ formatMoney(totalPurchaseCost) }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 rounded-lg flex items-center justify-center">
            <ShoppingBag :size="24" class="text-primary" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">保养总成本</p>
            <p class="text-3xl font-bold text-accent mt-1">¥ {{ formatMoney(totalMaintenanceCost) }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-50 rounded-lg flex items-center justify-center">
            <Shield :size="24" class="text-accent" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">零件更换成本</p>
            <p class="text-3xl font-bold text-purple-600 mt-1">¥ {{ formatMoney(totalPartCost) }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-50 rounded-lg flex items-center justify-center">
            <Replace :size="24" class="text-purple-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg p-5 shadow-sm card-hover">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">累计总投入</p>
            <p class="text-3xl font-bold text-green-600 mt-1">¥ {{ formatMoney(totalCost) }}</p>
          </div>
          <div class="w-12 h-12 bg-green-50 rounded-lg flex items-center justify-center">
            <Wallet :size="24" class="text-green-600" />
          </div>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="mb-6">
      <el-tab-pane label="按分类汇总" name="category" />
      <el-tab-pane label="按位置汇总" name="location" />
      <el-tab-pane label="按月份汇总" name="month" />
    </el-tabs>

    <div v-if="costStore.loading" class="text-center py-20 text-gray-400">加载中...</div>

    <div v-else class="bg-white rounded-lg p-6 shadow-sm">
      <div v-if="currentData.length">
        <el-table :data="currentData" stripe>
          <el-table-column label="分组" prop="groupKey" min-width="180" />
          <el-table-column label="工具数量" prop="toolCount" width="120" align="center">
            <template #default="{ row }">
              <span class="font-medium">{{ row.toolCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="采购价格" width="150" align="right">
            <template #default="{ row }">
              <span class="text-primary font-medium">¥ {{ formatMoney(row.purchaseCost) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="保养成本" width="150" align="right">
            <template #default="{ row }">
              <span class="text-accent font-medium">¥ {{ formatMoney(row.maintenanceCost) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="零件更换" width="150" align="right">
            <template #default="{ row }">
              <span class="text-purple-600 font-medium">¥ {{ formatMoney(row.partReplacementCost) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="总计" width="160" align="right">
            <template #default="{ row }">
              <span class="text-green-600 font-bold">¥ {{ formatMoney(row.totalCost) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" width="180">
            <template #default="{ row }">
              <el-progress
                :percentage="getPercentage(row.totalCost)"
                :color="getProgressColor(row.totalCost)"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="text-center py-20">
        <PieChart :size="48" class="mx-auto text-gray-300 mb-4" />
        <p class="text-gray-400">暂无费用数据</p>
      </div>
    </div>

    <div v-if="currentData.length" class="bg-white rounded-lg p-6 shadow-sm mt-6">
      <h3 class="text-lg font-semibold text-gray-700 mb-4">成本构成</h3>
      <div ref="chartRef" class="h-80"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ShoppingBag, Shield, Replace, Wallet, PieChart } from 'lucide-vue-next'
import { useCostSummaryStore } from '@/stores/costSummary'
import type { CostSummary } from '@/types'

const costStore = useCostSummaryStore()
const activeTab = ref<'category' | 'location' | 'month'>('category')
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const currentData = computed<CostSummary[]>(() => {
  switch (activeTab.value) {
    case 'category':
      return costStore.categorySummary
    case 'location':
      return costStore.locationSummary
    case 'month':
      return costStore.monthSummary
    default:
      return []
  }
})

const totalPurchaseCost = computed(() =>
  costStore.categorySummary.reduce((sum, item) => sum + (item.purchaseCost || 0), 0)
)

const totalMaintenanceCost = computed(() =>
  costStore.categorySummary.reduce((sum, item) => sum + (item.maintenanceCost || 0), 0)
)

const totalPartCost = computed(() =>
  costStore.categorySummary.reduce((sum, item) => sum + (item.partReplacementCost || 0), 0)
)

const totalCost = computed(() => totalPurchaseCost.value + totalMaintenanceCost.value + totalPartCost.value)

function formatMoney(value: number | null | undefined): string {
  const num = value || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getPercentage(value: number): number {
  const groupTotal = currentData.value.reduce((sum, item) => sum + (item.totalCost || 0), 0)
  if (!groupTotal) return 0
  return Math.round(((value || 0) / groupTotal) * 100)
}

function getProgressColor(value: number): string {
  const pct = getPercentage(value)
  if (pct >= 40) return '#22c55e'
  if (pct >= 20) return '#3b82f6'
  if (pct >= 10) return '#eab308'
  return '#f97316'
}

async function loadData() {
  await costStore.fetchByCategory()
  await costStore.fetchByLocation()
  await costStore.fetchByMonth()
}

onMounted(async () => {
  await loadData()
  await nextTick()
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

watch(activeTab, async () => {
  await nextTick()
  if (!chartInstance && chartRef.value) {
    initChart()
  } else {
    updateChart()
  }
})

watch(currentData, () => {
  nextTick(() => {
    if (chartInstance) {
      updateChart()
    }
  })
}, { deep: true })

function handleResize() {
  if (chartInstance) {
    chartInstance.resize()
  }
}

function initChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  updateChart()
}

function updateChart() {
  if (!chartInstance) return
  const data = currentData.value.slice(0, 15)
  chartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['采购价格', '保养成本', '零件更换'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.groupKey),
      axisLabel: { rotate: 30, interval: 0 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '¥ {value}' }
    },
    series: [
      {
        name: '采购价格',
        type: 'bar',
        stack: 'total',
        data: data.map(d => d.purchaseCost || 0),
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '保养成本',
        type: 'bar',
        stack: 'total',
        data: data.map(d => d.maintenanceCost || 0),
        itemStyle: { color: '#f97316' }
      },
      {
        name: '零件更换',
        type: 'bar',
        stack: 'total',
        data: data.map(d => d.partReplacementCost || 0),
        itemStyle: { color: '#8b5cf6' }
      }
    ]
  }, true)
}
</script>
