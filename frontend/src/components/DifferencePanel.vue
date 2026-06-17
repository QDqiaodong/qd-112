<template>
  <div class="bg-white rounded-lg shadow-sm overflow-hidden">
    <div class="flex items-center justify-between p-4 border-b border-gray-100">
      <div class="flex items-center gap-2">
        <AlertTriangle :size="18" class="text-amber-500" />
        <h4 class="font-semibold text-gray-800">盘点差异面板</h4>
        <span class="text-xs px-2 py-0.5 rounded-full bg-amber-100 text-amber-700">
          {{ totalDiffCount }} 项
        </span>
      </div>
      <div class="flex items-center gap-2">
        <el-radio-group v-model="groupBy" size="small" @change="handleGroupChange">
          <el-radio-button value="category">按分类</el-radio-button>
          <el-radio-button value="location">按位置</el-radio-button>
        </el-radio-group>
        <el-button size="small" @click="handleRefresh">
          <RefreshCw :size="14" />
        </el-button>
      </div>
    </div>

    <div class="p-4 flex gap-4 mb-2">
      <div class="flex items-center gap-1.5">
        <span class="w-2.5 h-2.5 rounded-full bg-red-500"></span>
        <span class="text-xs text-gray-600">状态不一致</span>
      </div>
      <div class="flex items-center gap-1.5">
        <span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
        <span class="text-xs text-gray-600">未核对</span>
      </div>
      <div class="flex items-center gap-1.5">
        <span class="w-2.5 h-2.5 rounded-full bg-gray-500"></span>
        <span class="text-xs text-gray-600">遗失</span>
      </div>
      <div class="flex items-center gap-1.5">
        <span class="w-2.5 h-2.5 rounded-full bg-orange-500"></span>
        <span class="text-xs text-gray-600">保养中</span>
      </div>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-400">加载中...</div>
    <div v-else-if="!differenceGroups.length" class="text-center py-10 text-gray-400">
      暂无差异项
    </div>
    <div v-else class="max-h-[520px] overflow-y-auto">
      <div
        v-for="group in differenceGroups"
        :key="group.groupKey"
        class="border-b border-gray-50 last:border-b-0"
      >
        <div class="px-4 py-2.5 bg-gray-50 sticky top-0 z-10">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <component :is="groupBy === 'category' ? FolderOpen : MapPin" :size="14" class="text-gray-500" />
              <span class="text-sm font-medium text-gray-700">{{ group.groupKey }}</span>
              <span class="text-xs text-gray-400">{{ group.totalCount || group.items.length }} 项</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs font-medium" :class="getCompletionClass(group.completionPercent || 0)">
                完成 {{ group.completionPercent || 0 }}%
              </span>
              <el-progress
                :percentage="group.completionPercent || 0"
                :stroke-width="4"
                :color="getProgressColor(group.completionPercent || 0)"
                class="w-20"
              />
            </div>
          </div>
          <div v-if="group.totalCount" class="flex gap-3 text-xs">
            <span class="flex items-center gap-1">
              <span class="w-2 h-2 rounded-full bg-green-500"></span>
              <span class="text-gray-500">已核对 {{ group.checkedCount || 0 }}</span>
            </span>
            <span class="flex items-center gap-1">
              <span class="w-2 h-2 rounded-full bg-amber-500"></span>
              <span class="text-gray-500">未核对 {{ group.uncheckedCount || 0 }}</span>
            </span>
            <span class="flex items-center gap-1">
              <span class="w-2 h-2 rounded-full bg-red-500"></span>
              <span class="text-gray-500">不符 {{ group.mismatchedCount || 0 }}</span>
            </span>
            <span class="flex items-center gap-1">
              <span class="w-2 h-2 rounded-full bg-gray-500"></span>
              <span class="text-gray-500">遗失 {{ group.lostCount || 0 }}</span>
            </span>
          </div>
        </div>
        <el-table :data="group.items" size="small" :show-header="true" class="diff-table">
          <el-table-column label="工具" min-width="160">
            <template #default="{ row }">
              <div>
                <span class="font-medium text-gray-800">{{ row.toolName }}</span>
                <span v-if="row.toolModel" class="text-xs text-gray-400 ml-1">{{ row.toolModel }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="预期" width="80" align="center">
            <template #default="{ row }">
              <StatusBadge :status="row.expectedStatus" />
            </template>
          </el-table-column>
          <el-table-column label="实际" width="80" align="center">
            <template #default="{ row }">
              <StatusBadge :status="row.actualStatus" />
            </template>
          </el-table-column>
          <el-table-column label="差异类型" width="150" align="center">
            <template #default="{ row }">
              <div class="flex flex-wrap gap-1 justify-center">
                <span
                  v-for="dt in parseDiffTypes(row.differenceType)"
                  :key="dt"
                  class="text-xs px-1.5 py-0.5 rounded"
                  :class="diffTypeClass(dt)"
                >
                  {{ diffTypeLabel(dt) }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="位置" width="120">
            <template #default="{ row }">
              <span class="text-xs text-gray-500">{{ normalizeLocationForDisplay(row.location) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="核对" width="60" align="center">
            <template #default="{ row }">
              <el-icon v-if="row.checked" class="text-green-500"><Check /></el-icon>
              <el-icon v-else class="text-gray-300"><X /></el-icon>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { AlertTriangle, RefreshCw, FolderOpen, MapPin, Check, X } from 'lucide-vue-next'
import { ElIcon } from 'element-plus'
import StatusBadge from '@/components/StatusBadge.vue'
import { useInventoryStore } from '@/stores/inventory'
import type { DifferenceGroup } from '@/types'
import { normalizeLocationForDisplay } from '@/utils/location'

const props = defineProps<{
  inventoryId: number
}>()

const inventoryStore = useInventoryStore()

const groupBy = defineModel<string>('groupBy', { default: 'category' })

const loading = computed(() => inventoryStore.loading)
const differenceGroups = computed<DifferenceGroup[]>(() => inventoryStore.differenceGroups)
const totalDiffCount = computed(() => differenceGroups.value.reduce((sum, g) => sum + g.items.length, 0))

function parseDiffTypes(types: string): string[] {
  return types ? types.split(',') : []
}

function diffTypeLabel(type: string): string {
  const map: Record<string, string> = {
    STATUS_MISMATCH: '状态不一致',
    UNCHECKED: '未核对',
    LOST: '遗失',
    MAINTENANCE: '保养中'
  }
  return map[type] || type
}

function diffTypeClass(type: string): string {
  const map: Record<string, string> = {
    STATUS_MISMATCH: 'bg-red-100 text-red-700',
    UNCHECKED: 'bg-amber-100 text-amber-700',
    LOST: 'bg-gray-200 text-gray-700',
    MAINTENANCE: 'bg-orange-100 text-orange-700'
  }
  return map[type] || 'bg-gray-100 text-gray-700'
}

function getCompletionClass(percent: number): string {
  if (percent >= 100) return 'text-green-600'
  if (percent >= 70) return 'text-blue-600'
  if (percent >= 40) return 'text-amber-600'
  return 'text-red-600'
}

function getProgressColor(percent: number): string {
  if (percent >= 100) return '#10b981'
  if (percent >= 70) return '#3b82f6'
  if (percent >= 40) return '#f59e0b'
  return '#ef4444'
}

async function handleGroupChange(val: string) {
  await inventoryStore.fetchDifferenceGroups(props.inventoryId, val)
}

async function handleRefresh() {
  await inventoryStore.fetchDifferenceGroups(props.inventoryId, groupBy.value)
}

watch(() => props.inventoryId, (id) => {
  if (id) {
    inventoryStore.fetchDifferenceGroups(id, groupBy.value)
  }
}, { immediate: true })
</script>

<style scoped>
.diff-table :deep(.el-table__header th) {
  background-color: #f9fafb;
  font-size: 12px;
  padding: 6px 0;
}
.diff-table :deep(.el-table__body td) {
  padding: 4px 0;
}
</style>
