<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">使用场景时长分析</h2>
    </div>

    <div class="flex items-center gap-4 mb-6">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="handleSearch"
      />
      <el-button @click="handleSearch">筛选</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <div class="grid grid-cols-5 gap-6">
      <div class="col-span-2 bg-white rounded-lg p-6 shadow-sm">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">场景时长排行</h3>
        <div v-if="usageStore.scenarioLoading" class="text-center py-10 text-gray-400">加载中...</div>
        <div v-else-if="scenarioList.length" class="space-y-3">
          <div
            v-for="(item, index) in scenarioList"
            :key="item.scenario"
            class="p-4 rounded-lg cursor-pointer transition-all duration-200"
            :class="selectedScenario === item.scenario ? 'bg-primary/10 border border-primary/30' : 'bg-gray-50 hover:bg-gray-100 border border-transparent'"
            @click="selectScenario(item.scenario)"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-3">
                <span
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold text-white"
                  :class="getRankColor(index)"
                >
                  {{ index + 1 }}
                </span>
                <span class="font-medium text-gray-800">{{ item.scenario }}</span>
              </div>
              <span class="text-primary font-bold">{{ formatMinutes(item.totalMinutes) }}</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all duration-500"
                :class="getBarColor(index)"
                :style="{ width: getBarWidth(item.totalMinutes) + '%' }"
              ></div>
            </div>
            <div class="flex justify-between mt-2 text-xs text-gray-400">
              <span>使用 {{ item.usageCount }} 次</span>
              <span>最近：{{ item.lastUseDate || '暂无' }}</span>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-10 text-gray-400">暂无场景数据</div>
      </div>

      <div class="col-span-3 bg-white rounded-lg p-6 shadow-sm">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">
          {{ selectedScenario ? selectedScenario + ' - 高频工具' : '请选择场景查看详情' }}
        </h3>
        <div v-if="!selectedScenario" class="text-center py-16 text-gray-400">
          <BarChart3 :size="48" class="mx-auto mb-4 text-gray-300" />
          <p>点击左侧场景卡片查看详情</p>
        </div>
        <div v-else-if="currentScenarioDetail" class="space-y-4">
          <div class="grid grid-cols-3 gap-4 mb-6">
            <div class="bg-blue-50 rounded-lg p-4">
              <p class="text-sm text-gray-500">总使用时长</p>
              <p class="text-2xl font-bold text-primary mt-1">
                {{ formatMinutes(currentScenarioDetail.totalMinutes) }}
              </p>
            </div>
            <div class="bg-orange-50 rounded-lg p-4">
              <p class="text-sm text-gray-500">使用次数</p>
              <p class="text-2xl font-bold text-accent mt-1">
                {{ currentScenarioDetail.usageCount }} 次
              </p>
            </div>
            <div class="bg-green-50 rounded-lg p-4">
              <p class="text-sm text-gray-500">最近使用</p>
              <p class="text-lg font-bold text-green-600 mt-1">
                {{ currentScenarioDetail.lastUseDate || '暂无' }}
              </p>
            </div>
          </div>

          <div ref="chartRef" class="h-64 mb-6"></div>

          <h4 class="text-md font-semibold text-gray-700">工具明细</h4>
          <div class="space-y-2 max-h-80 overflow-auto">
            <div
              v-for="(tool, index) in currentScenarioDetail.topTools"
              :key="tool.toolId"
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <div class="flex items-center gap-3">
                <span class="w-6 h-6 rounded-full bg-gray-200 flex items-center justify-center text-xs font-bold text-gray-600">
                  {{ index + 1 }}
                </span>
                <div>
                  <p class="font-medium text-gray-800">{{ tool.toolName }}</p>
                  <p class="text-xs text-gray-400">使用 {{ tool.usageCount }} 次</p>
                </div>
              </div>
              <div class="text-right">
                <p class="font-bold text-primary">{{ formatMinutes(tool.totalMinutes) }}</p>
                <p class="text-xs text-gray-400">最近：{{ tool.lastUseDate || '暂无' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { BarChart3 } from 'lucide-vue-next'
import { useUsageStore } from '@/stores/usage'

const usageStore = useUsageStore()
const dateRange = ref<string[]>([])
const selectedScenario = ref<string>('')
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const scenarioList = computed(() => usageStore.scenarioAnalysis)

const currentScenarioDetail = computed(() => {
  if (!selectedScenario.value) return null
  return scenarioList.value.find((s) => s.scenario === selectedScenario.value)
})

const maxMinutes = computed(() => {
  if (!scenarioList.value.length) return 1
  return Math.max(...scenarioList.value.map((s) => s.totalMinutes))
})

function formatMinutes(minutes: number): string {
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (mins === 0) return `${hours}小时`
  return `${hours}小时${mins}分`
}

function getRankColor(index: number): string {
  const colors = ['bg-amber-500', 'bg-gray-400', 'bg-amber-700', 'bg-gray-500', 'bg-gray-500']
  return colors[index] || 'bg-gray-500'
}

function getBarColor(index: number): string {
  const colors = ['bg-primary', 'bg-blue-500', 'bg-blue-400', 'bg-blue-300', 'bg-blue-200']
  return colors[index] || 'bg-blue-200'
}

function getBarWidth(minutes: number): number {
  if (maxMinutes.value === 0) return 0
  return Math.round((minutes / maxMinutes.value) * 100)
}

function selectScenario(scenario: string) {
  selectedScenario.value = scenario
  nextTick(() => {
    initChart()
  })
}

function handleSearch() {
  const params: Record<string, any> = {}
  if (dateRange.value?.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  usageStore.fetchScenarioAnalysis(params)
  selectedScenario.value = ''
}

function resetFilter() {
  dateRange.value = []
  handleSearch()
}

function initChart() {
  if (!chartRef.value || !currentScenarioDetail.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const topTools = currentScenarioDetail.value.topTools.slice(0, 10)
  chartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { formatter: '{value} 分钟' }
    },
    yAxis: {
      type: 'category',
      data: topTools.map((t) => t.toolName).reverse(),
      axisLabel: { fontSize: 12 }
    },
    series: [
      {
        type: 'bar',
        data: topTools.map((t) => t.totalMinutes).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1e40af' },
            { offset: 1, color: '#3b82f6' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        barWidth: 20,
        label: {
          show: true,
          position: 'right',
          formatter: '{c}分钟',
          fontSize: 12
        }
      }
    ]
  })
}

onMounted(() => {
  handleSearch()
})

watch(() => currentScenarioDetail.value?.topTools, () => {
  nextTick(() => initChart())
}, { deep: true })
</script>
