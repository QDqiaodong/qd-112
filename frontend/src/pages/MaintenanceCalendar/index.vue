<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <h2 class="text-2xl font-bold text-gray-800">保养日程月历</h2>
        <el-tag v-if="overdueCount > 0" type="danger" effect="light">
          {{ overdueCount }} 项已逾期
        </el-tag>
        <el-tag v-if="dueTodayCount > 0" type="warning" effect="light">
          {{ dueTodayCount }} 项今日到期
        </el-tag>
      </div>
      <div class="flex items-center gap-3">
        <el-button-group>
          <el-button @click="changeMonth(-1)">
            <ChevronLeft :size="18" />
          </el-button>
          <el-button @click="goToToday">今天</el-button>
          <el-button @click="changeMonth(1)">
            <ChevronRight :size="18" />
          </el-button>
        </el-button-group>
        <el-date-picker
          v-model="currentMonth"
          type="month"
          format="YYYY年MM月"
          value-format="YYYY-MM"
          @change="handleMonthChange"
          class="w-40"
        />
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <div class="grid grid-cols-7 border-b border-gray-200">
        <div
          v-for="day in weekDays"
          :key="day"
          class="py-3 text-center text-sm font-medium text-gray-600 bg-gray-50"
        >
          {{ day }}
        </div>
      </div>

      <template v-for="(week, _weekIndex) in calendarDays" :key="_weekIndex">
        <div class="grid grid-cols-7">
          <div
            v-for="day in week"
            :key="day.date"
            class="min-h-32 border-b border-r border-gray-100 p-2"
            :class="{
              'bg-gray-50': !day.isCurrentMonth,
              'bg-blue-50': day.isToday,
              'bg-red-50': day.isWeekend && day.isCurrentMonth
            }"
          >
            <div class="flex items-center justify-between mb-1">
              <span
                class="text-sm font-medium"
                :class="{
                  'text-gray-400': !day.isCurrentMonth,
                  'text-blue-600': day.isToday,
                  'text-gray-700': day.isCurrentMonth && !day.isToday
                }"
              >
                {{ day.day }}
              </span>
              <el-badge
                v-if="day.tools.length > 0"
                :value="day.tools.length"
                :type="day.hasOverdue ? 'danger' : 'primary'"
                :hidden="day.tools.length === 0"
                class="ml-auto"
              />
            </div>

            <div class="space-y-1">
              <div
                v-for="tool in day.tools.slice(0, 2)"
                :key="tool.id"
                class="text-xs p-1.5 rounded cursor-pointer transition-colors"
                :class="getToolClass(tool)"
                @click="expandedDate = expandedDate === day.date ? null : day.date"
              >
                <div class="font-medium truncate">{{ tool.name }}</div>
                <div class="text-gray-500 truncate">{{ getCategoryName(tool.categoryId) }}</div>
              </div>
              <div
                v-if="day.tools.length > 2"
                class="text-xs text-gray-400 text-center"
              >
                还有 {{ day.tools.length - 2 }} 项更多
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <div v-if="expandedDate" class="mt-6 bg-white rounded-lg shadow-sm p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-gray-800">
          {{ formatExpandedDate }} 的保养任务
        </h3>
        <el-button link type="primary" @click="expandedDate = null">关闭</el-button>
      </div>
      <div class="space-y-3">
        <div
          v-for="tool in getToolsForDate(expandedDate)"
          :key="tool.id"
          class="border rounded-lg p-4"
          :class="getToolCardClass(tool)"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <h4 class="font-semibold text-gray-800">{{ tool.name }}</h4>
                <el-tag size="small" :type="getOverdueTagType(tool)">
                  {{ getOverdueText(tool) }}
                </el-tag>
              </div>
              <div class="grid grid-cols-2 gap-4 text-sm text-gray-600">
                <div class="flex items-center gap-2">
                  <Folder :size="16" class="text-gray-400" />
                  <span>分类：{{ getCategoryName(tool.categoryId) }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <Wrench :size="16" class="text-gray-400" />
                  <span>保养项：{{ getDefaultMaintenanceItems(tool.subCategoryId || tool.categoryId) }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <Calendar :size="16" class="text-gray-400" />
                  <span>下次保养：{{ tool.nextMaintenanceDate }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <Clock :size="16" class="text-gray-400" />
                  <span>周期：{{ tool.maintenanceCycleDays }} 天</span>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <el-button type="primary" size="small" @click="goToToolDetail(tool.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="toolStore.loading" class="fixed inset-0 bg-white/50 flex items-center justify-center z-50">
      <el-loading text="加载中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronLeft, ChevronRight, Folder, Wrench, Calendar, Clock } from 'lucide-vue-next'
import { useToolStore } from '@/stores/tool'
import { useCategoryStore } from '@/stores/category'
import type { Tool } from '@/types'

const router = useRouter()
const toolStore = useToolStore()
const categoryStore = useCategoryStore()

const currentMonth = ref<string>('')
const expandedDate = ref<string | null>(null)

const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const today = new Date()

onMounted(() => {
  const year = today.getFullYear()
  const month = today.getMonth() + 1
  currentMonth.value = `${year}-${String(month).padStart(2, '0')}`
  loadData()
  categoryStore.fetchCategoryTree()
})

watch(() => toolStore.monthlyMaintenanceTools, async (tools) => {
  if (!tools || tools.length === 0) return
  const categoryIds = new Set<number>()
  for (const tool of tools) {
    if (tool.subCategoryId) {
      categoryIds.add(tool.subCategoryId)
    } else if (tool.categoryId) {
      categoryIds.add(tool.categoryId)
    }
  }
  const promises = Array.from(categoryIds).map(id => categoryStore.fetchEffectiveMaintenanceItems(id))
  await Promise.all(promises)
}, { immediate: true })

watch(currentMonth, () => {
  loadData()
})

async function loadData() {
  if (!currentMonth.value) return
  const [year, month] = currentMonth.value.split('-').map(Number)
  await toolStore.fetchMonthlyMaintenance(year, month)
}

function changeMonth(delta: number) {
  if (!currentMonth.value) return
  const [year, month] = currentMonth.value.split('-').map(Number)
  const date = new Date(year, month - 1, 1)
  date.setMonth(date.getMonth() + delta)
  currentMonth.value = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
}

function goToToday() {
  const year = today.getFullYear()
  const month = today.getMonth() + 1
  currentMonth.value = `${year}-${String(month).padStart(2, '0')}`
}

function handleMonthChange() {
  loadData()
}

const calendarDays = computed(() => {
  if (!currentMonth.value) return []

  const [year, month] = currentMonth.value.split('-').map(Number)
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  const startDay = firstDay.getDay()
  const daysInMonth = lastDay.getDate()

  const prevMonthLastDay = new Date(year, month - 1, 0).getDate()

  const weeks: Array<Array<{
    date: string
    day: number
    isCurrentMonth: boolean
    isToday: boolean
    isWeekend: boolean
    tools: Tool[]
    hasOverdue: boolean
  }>> = []

  let currentWeek: Array<any> = []

  for (let i = 0; i < startDay; i++) {
    const day = prevMonthLastDay - startDay + i + 1
    const date = new Date(year, month - 2, day)
    const dateStr = formatDate(date)
    currentWeek.push({
      date: dateStr,
      day,
      isCurrentMonth: false,
      isToday: false,
      isWeekend: date.getDay() === 0 || date.getDay() === 6,
      tools: getToolsForDate(dateStr),
      hasOverdue: hasOverdueTools(getToolsForDate(dateStr))
    })
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month - 1, day)
    const dateStr = formatDate(date)
    const isToday = date.toDateString() === today.toDateString()
    currentWeek.push({
      date: dateStr,
      day,
      isCurrentMonth: true,
      isToday,
      isWeekend: date.getDay() === 0 || date.getDay() === 6,
      tools: getToolsForDate(dateStr),
      hasOverdue: hasOverdueTools(getToolsForDate(dateStr))
    })

    if (currentWeek.length === 7) {
      weeks.push(currentWeek)
      currentWeek = []
    }
  }

  if (currentWeek.length > 0) {
    const remainingDays = 7 - currentWeek.length
    for (let day = 1; day <= remainingDays; day++) {
      const date = new Date(year, month, day)
      const dateStr = formatDate(date)
      currentWeek.push({
        date: dateStr,
        day,
        isCurrentMonth: false,
        isToday: false,
        isWeekend: date.getDay() === 0 || date.getDay() === 6,
        tools: getToolsForDate(dateStr),
        hasOverdue: hasOverdueTools(getToolsForDate(dateStr))
      })
    }
    weeks.push(currentWeek)
  }

  return weeks
})

const overdueCount = computed(() => {
  return toolStore.monthlyMaintenanceTools.filter(tool => isOverdue(tool)).length
})

const dueTodayCount = computed(() => {
  const todayStr = formatDate(today)
  return toolStore.monthlyMaintenanceTools.filter(tool => tool.nextMaintenanceDate === todayStr).length
})

const formatExpandedDate = computed(() => {
  if (!expandedDate.value) return ''
  const date = new Date(expandedDate.value)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

function formatDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getToolsForDate(dateStr: string): Tool[] {
  return toolStore.monthlyMaintenanceTools.filter(tool => tool.nextMaintenanceDate === dateStr)
}

function hasOverdueTools(tools: Tool[]): boolean {
  return tools.some(tool => isOverdue(tool))
}

function isOverdue(tool: Tool): boolean {
  if (!tool.nextMaintenanceDate) return false
  return new Date(tool.nextMaintenanceDate) < new Date(today.toDateString())
}

function getOverdueDays(tool: Tool): number {
  if (!tool.nextMaintenanceDate) return 0
  const maintenanceDate = new Date(tool.nextMaintenanceDate)
  const diffTime = today.getTime() - maintenanceDate.getTime()
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

function getOverdueText(tool: Tool): string {
  const days = getOverdueDays(tool)
  if (days > 0) {
    return `逾期 ${days} 天`
  } else if (days === 0) {
    return '今日到期'
  } else {
    return `还有 ${-days} 天`
  }
}

function getOverdueTagType(tool: Tool): 'danger' | 'warning' | 'success' | 'info' | 'primary' {
  const days = getOverdueDays(tool)
  if (days > 0) return 'danger'
  if (days === 0) return 'warning'
  return 'success'
}

function getToolClass(tool: Tool): string {
  const days = getOverdueDays(tool)
  if (days > 0) {
    return 'bg-red-50 text-red-700 hover:bg-red-100'
  } else if (days === 0) {
    return 'bg-yellow-50 text-yellow-700 hover:bg-yellow-100'
  }
  return 'bg-blue-50 text-blue-700 hover:bg-blue-100'
}

function getToolCardClass(tool: Tool): string {
  const days = getOverdueDays(tool)
  if (days > 0) {
    return 'border-red-200 bg-red-50/50'
  } else if (days === 0) {
    return 'border-yellow-200 bg-yellow-50/50'
  }
  return 'border-gray-200'
}

function getCategoryName(categoryId: number): string {
  function findCategory(categories: any[], id: number): string {
    for (const cat of categories) {
      if (cat.id === id) {
        return cat.name
      }
      if (cat.children && cat.children.length > 0) {
        const found = findCategory(cat.children, id)
        if (found) return found
      }
    }
    return ''
  }
  return findCategory(categoryStore.categoryTree, categoryId) || '未分类'
}

function getDefaultMaintenanceItems(categoryId?: number): string {
  if (!categoryId) return '暂无'
  const items = categoryStore.getMaintenanceItemsByCategory(categoryId)
  if (!items || items.length === 0) return '暂无'
  return items
    .filter(item => item.enabled && item.item)
    .map(item => item.item.name)
    .join('、')
}

function goToToolDetail(id: number) {
  router.push(`/tools/${id}`)
}
</script>
