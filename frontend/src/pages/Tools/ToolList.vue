<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">工具管理</h2>
        <p class="text-sm text-gray-500 mt-1">可用性评分综合计算：状态(30%) + 保养(25%) + 使用(25%) + 盘点(20%)</p>
      </div>
      <el-button type="primary" @click="router.push('/tools/add')">
        <Plus :size="16" class="mr-1" /> 新增工具
      </el-button>
    </div>

    <div class="flex items-center gap-4 mb-6">
      <el-input
        v-model="keyword"
        placeholder="搜索工具名称/型号"
        clearable
        class="w-60"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <Search :size="16" />
        </template>
      </el-input>
      <CategoryCascade v-model="categoryFilter" />
      <el-select v-model="sortBy" placeholder="排序方式" class="w-48" @change="handleSearch">
        <el-option label="按创建时间" value="createTime" />
        <el-option label="按评分从高到低" value="scoreDesc" />
        <el-option label="按评分从低到高" value="scoreAsc" />
      </el-select>
      <el-button @click="handleSearch">搜索</el-button>
    </div>

    <div v-if="toolStore.loading" class="text-center py-20 text-gray-400">加载中...</div>

    <div v-else-if="sortedTools.length" class="grid grid-cols-3 gap-6">
      <div
        v-for="item in sortedTools"
        :key="item.tool.id"
        class="bg-white rounded-lg p-5 shadow-sm card-hover"
      >
        <div class="flex items-start justify-between mb-3">
          <h3 class="text-lg font-semibold text-gray-800">{{ item.tool.name }}</h3>
          <div class="flex items-center gap-2">
            <StatusBadge :status="item.tool.status" />
          </div>
        </div>

        <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div
              class="w-14 h-14 rounded-full flex items-center justify-center text-xl font-bold"
              :class="gradeClass(item.availabilityScore.grade)"
            >
              {{ item.availabilityScore.grade }}
            </div>
            <div>
              <div class="text-2xl font-bold" :class="scoreColorClass(item.availabilityScore.totalScore)">
                {{ item.availabilityScore.totalScore }}
                <span class="text-sm font-normal text-gray-400">/ 100</span>
              </div>
              <div class="text-xs text-gray-500">可用性评分</div>
            </div>
          </div>
          <el-button text type="primary" size="small" @click="showScoreDetail(item)">
            <BarChart3 :size="14" class="mr-1" /> 评分明细
          </el-button>
        </div>

        <div class="space-y-2 mb-4">
          <div v-for="dim in scoreDimensions(item)" :key="dim.label" class="flex items-center gap-2">
            <span class="text-xs text-gray-500 w-10">{{ dim.label }}</span>
            <el-progress
              :percentage="dim.percentage"
              :color="dim.color"
              :stroke-width="6"
              :show-text="false"
              class="flex-1"
            />
            <span class="text-xs font-medium" :class="dim.textColor">{{ dim.score }}/{{ dim.max }}</span>
          </div>
        </div>

        <div class="space-y-1 text-sm text-gray-500 mb-4">
          <p>型号：{{ item.tool.model || '-' }}</p>
          <p>品牌：{{ item.tool.brand || '-' }}</p>
          <p>位置：{{ normalizeLocationForDisplay(item.tool.location) }}</p>
          <p class="flex items-center gap-1">
            <component :is="getDimIcon(item.availabilityScore)" :size="12" />
            <span :class="getDimTextClass(item.availabilityScore)">
              {{ getTopIssue(item.availabilityScore) }}
            </span>
          </p>
        </div>

        <div class="flex items-center gap-2 pt-3 border-t border-gray-100">
          <el-button text size="small" @click="router.push(`/tools/${item.tool.id}`)">查看</el-button>
          <el-button text size="small" type="primary" @click="router.push(`/tools/${item.tool.id}/edit`)">编辑</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(item.tool.id)">删除</el-button>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20">
      <Hammer :size="48" class="mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400">暂无工具数据</p>
    </div>

    <div v-if="toolStore.total > pagination.size.value" class="flex justify-end mt-6">
      <el-pagination
        :current-page="pagination.page.value"
        :page-size="pagination.size.value"
        :total="toolStore.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="scoreDetailVisible" width="560px" :title="scoreDetailTitle">
      <div v-if="currentScoreItem" class="space-y-5">
        <div class="flex items-center gap-4 p-4 bg-gray-50 rounded-lg">
          <div
            class="w-20 h-20 rounded-full flex items-center justify-center text-3xl font-bold"
            :class="gradeClass(currentScoreItem.availabilityScore.grade)"
          >
            {{ currentScoreItem.availabilityScore.grade }}
          </div>
          <div class="flex-1">
            <div class="text-3xl font-bold mb-1" :class="scoreColorClass(currentScoreItem.availabilityScore.totalScore)">
              {{ currentScoreItem.availabilityScore.totalScore }}
              <span class="text-base font-normal text-gray-400">分 / 满分100</span>
            </div>
            <div class="text-sm text-gray-500">{{ currentScoreItem.tool.name }} - 可用性综合评分</div>
          </div>
        </div>

        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="状态评分 (30分)">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-semibold" :class="dimColorClass(currentScoreItem.availabilityScore.statusScore, 30)">
                  {{ currentScoreItem.availabilityScore.statusScore }}/30
                </span>
              </div>
              <el-progress
                :percentage="Math.round(currentScoreItem.availabilityScore.statusScore / 30 * 100)"
                :color="dimProgressColor(currentScoreItem.availabilityScore.statusScore, 30)"
                :stroke-width="8"
                :show-text="false"
                class="w-32"
              />
            </div>
            <div class="mt-1 text-xs text-gray-500">{{ currentScoreItem.availabilityScore.statusDetail }}</div>
          </el-descriptions-item>

          <el-descriptions-item label="保养评分 (25分)">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-semibold" :class="dimColorClass(currentScoreItem.availabilityScore.maintenanceScore, 25)">
                  {{ currentScoreItem.availabilityScore.maintenanceScore }}/25
                </span>
                <el-tag v-if="currentScoreItem.availabilityScore.overdueDays > 0" type="danger" size="small" effect="light">
                  逾期 {{ currentScoreItem.availabilityScore.overdueDays }} 天
                </el-tag>
              </div>
              <el-progress
                :percentage="Math.round(currentScoreItem.availabilityScore.maintenanceScore / 25 * 100)"
                :color="dimProgressColor(currentScoreItem.availabilityScore.maintenanceScore, 25)"
                :stroke-width="8"
                :show-text="false"
                class="w-32"
              />
            </div>
            <div class="mt-1 text-xs text-gray-500">{{ currentScoreItem.availabilityScore.maintenanceDetail }}</div>
            <div v-if="currentScoreItem.availabilityScore.nextMaintenanceDate" class="mt-1 text-xs text-gray-400">
              下次保养日期：{{ currentScoreItem.availabilityScore.nextMaintenanceDate }}
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="使用频率评分 (25分)">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-semibold" :class="dimColorClass(currentScoreItem.availabilityScore.usageScore, 25)">
                  {{ currentScoreItem.availabilityScore.usageScore }}/25
                </span>
              </div>
              <el-progress
                :percentage="Math.round(currentScoreItem.availabilityScore.usageScore / 25 * 100)"
                :color="dimProgressColor(currentScoreItem.availabilityScore.usageScore, 25)"
                :stroke-width="8"
                :show-text="false"
                class="w-32"
              />
            </div>
            <div class="mt-1 text-xs text-gray-500">{{ currentScoreItem.availabilityScore.usageDetail }}</div>
            <div class="mt-1 flex gap-4 text-xs text-gray-400">
              <span>近30天使用：{{ currentScoreItem.availabilityScore.usageCount30Days }} 次</span>
              <span>近90天使用：{{ currentScoreItem.availabilityScore.usageCount90Days }} 次</span>
              <span v-if="currentScoreItem.availabilityScore.lastUseDate">
                上次使用：{{ currentScoreItem.availabilityScore.lastUseDate }}
              </span>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="盘点评分 (20分)">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-semibold" :class="dimColorClass(currentScoreItem.availabilityScore.inventoryScore, 20)">
                  {{ currentScoreItem.availabilityScore.inventoryScore }}/20
                </span>
              </div>
              <el-progress
                :percentage="Math.round(currentScoreItem.availabilityScore.inventoryScore / 20 * 100)"
                :color="dimProgressColor(currentScoreItem.availabilityScore.inventoryScore, 20)"
                :stroke-width="8"
                :show-text="false"
                class="w-32"
              />
            </div>
            <div class="mt-1 text-xs text-gray-500">{{ currentScoreItem.availabilityScore.inventoryDetail }}</div>
            <div v-if="currentScoreItem.availabilityScore.lastInventoryDate" class="mt-1 text-xs text-gray-400">
              最近盘点日期：{{ currentScoreItem.availabilityScore.lastInventoryDate }}
              <span v-if="currentScoreItem.availabilityScore.lastInventoryChecked" class="ml-2 text-green-600">已核对</span>
              <span v-else class="ml-2 text-orange-600">未核对</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Search, Hammer, BarChart3, AlertTriangle, Wrench, Clock, ClipboardList } from 'lucide-vue-next'
import { useToolStore } from '@/stores/tool'
import { useCategoryStore } from '@/stores/category'
import CategoryCascade from '@/components/CategoryCascade.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { usePagination } from '@/composables/usePagination'
import type { ToolWithScore, ToolAvailabilityScore } from '@/types'
import { normalizeLocationForDisplay } from '@/utils/location'

const router = useRouter()
const toolStore = useToolStore()
const categoryStore = useCategoryStore()
const pagination = usePagination()

const keyword = ref('')
const sortBy = ref('createTime')
const categoryFilter = reactive({ categoryId: undefined as number | undefined, subCategoryId: undefined as number | undefined })

const scoreDetailVisible = ref(false)
const currentScoreItem = ref<ToolWithScore | null>(null)
const scoreDetailTitle = computed(() => currentScoreItem.value ? `${currentScoreItem.value.tool.name} - 评分明细` : '')

const sortedTools = computed(() => {
  const list = [...toolStore.toolsWithScore]
  if (sortBy.value === 'scoreDesc') {
    list.sort((a, b) => b.availabilityScore.totalScore - a.availabilityScore.totalScore)
  } else if (sortBy.value === 'scoreAsc') {
    list.sort((a, b) => a.availabilityScore.totalScore - b.availabilityScore.totalScore)
  }
  return list
})

onMounted(() => {
  categoryStore.fetchCategoryTreeWithStats()
  loadData()
})

function loadData() {
  toolStore.fetchToolsWithScore({
    page: pagination.page.value,
    size: pagination.size.value,
    keyword: keyword.value || undefined,
    categoryId: categoryFilter.categoryId,
    subCategoryId: categoryFilter.subCategoryId
  })
}

function handleSearch() {
  pagination.reset()
  loadData()
}

function handlePageChange(page: number) {
  pagination.handleChange(page)
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除此工具吗？', '提示', { type: 'warning' })
  await toolStore.deleteTool(id)
  ElMessage.success('删除成功')
  loadData()
}

function showScoreDetail(item: ToolWithScore) {
  currentScoreItem.value = item
  scoreDetailVisible.value = true
}

function gradeClass(grade: string) {
  switch (grade) {
    case 'A': return 'bg-green-100 text-green-700'
    case 'B': return 'bg-blue-100 text-blue-700'
    case 'C': return 'bg-yellow-100 text-yellow-700'
    case 'D': return 'bg-orange-100 text-orange-700'
    case 'E': return 'bg-red-100 text-red-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}

function scoreColorClass(score: number) {
  if (score >= 90) return 'text-green-600'
  if (score >= 80) return 'text-blue-600'
  if (score >= 70) return 'text-yellow-600'
  if (score >= 60) return 'text-orange-600'
  return 'text-red-600'
}

function dimColorClass(score: number, max: number) {
  const pct = score / max
  if (pct >= 0.9) return 'text-green-600'
  if (pct >= 0.7) return 'text-blue-600'
  if (pct >= 0.5) return 'text-yellow-600'
  if (pct >= 0.3) return 'text-orange-600'
  return 'text-red-600'
}

function dimProgressColor(score: number, max: number) {
  const pct = score / max
  if (pct >= 0.9) return '#22c55e'
  if (pct >= 0.7) return '#3b82f6'
  if (pct >= 0.5) return '#eab308'
  if (pct >= 0.3) return '#f97316'
  return '#ef4444'
}

function scoreDimensions(item: ToolWithScore) {
  return [
    {
      label: '状态',
      score: item.availabilityScore.statusScore,
      max: 30,
      percentage: Math.round(item.availabilityScore.statusScore / 30 * 100),
      color: dimProgressColor(item.availabilityScore.statusScore, 30),
      textColor: dimColorClass(item.availabilityScore.statusScore, 30)
    },
    {
      label: '保养',
      score: item.availabilityScore.maintenanceScore,
      max: 25,
      percentage: Math.round(item.availabilityScore.maintenanceScore / 25 * 100),
      color: dimProgressColor(item.availabilityScore.maintenanceScore, 25),
      textColor: dimColorClass(item.availabilityScore.maintenanceScore, 25)
    },
    {
      label: '使用',
      score: item.availabilityScore.usageScore,
      max: 25,
      percentage: Math.round(item.availabilityScore.usageScore / 25 * 100),
      color: dimProgressColor(item.availabilityScore.usageScore, 25),
      textColor: dimColorClass(item.availabilityScore.usageScore, 25)
    },
    {
      label: '盘点',
      score: item.availabilityScore.inventoryScore,
      max: 20,
      percentage: Math.round(item.availabilityScore.inventoryScore / 20 * 100),
      color: dimProgressColor(item.availabilityScore.inventoryScore, 20),
      textColor: dimColorClass(item.availabilityScore.inventoryScore, 20)
    }
  ]
}

function getDimIcon(score: ToolAvailabilityScore) {
  const dims = [
    { score: score.statusScore / 30, icon: AlertTriangle, name: 'status' },
    { score: score.maintenanceScore / 25, icon: Wrench, name: 'maintenance' },
    { score: score.usageScore / 25, icon: Clock, name: 'usage' },
    { score: score.inventoryScore / 20, icon: ClipboardList, name: 'inventory' }
  ]
  const min = dims.reduce((a, b) => a.score < b.score ? a : b)
  return min.icon
}

function getDimTextClass(score: ToolAvailabilityScore) {
  const minPct = Math.min(
    score.statusScore / 30,
    score.maintenanceScore / 25,
    score.usageScore / 25,
    score.inventoryScore / 20
  )
  if (minPct >= 0.7) return 'text-green-600'
  if (minPct >= 0.5) return 'text-yellow-600'
  return 'text-red-600'
}

function getTopIssue(score: ToolAvailabilityScore) {
  const dims = [
    { pct: score.statusScore / 30, detail: score.statusDetail },
    { pct: score.maintenanceScore / 25, detail: score.maintenanceDetail },
    { pct: score.usageScore / 25, detail: score.usageDetail },
    { pct: score.inventoryScore / 20, detail: score.inventoryDetail }
  ]
  const min = dims.reduce((a, b) => a.pct < b.pct ? a : b)
  if (min.pct >= 0.9) return '各维度表现优秀'
  if (min.pct >= 0.7) return '整体状态良好'
  return min.detail
}
</script>
