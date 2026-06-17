<template>
  <div class="p-6">
    <div class="flex items-center gap-4 mb-6">
      <el-button @click="router.back()">
        <ArrowLeft :size="16" class="mr-1" /> 返回
      </el-button>
      <h2 class="text-2xl font-bold text-gray-800">{{ tool?.name }}</h2>
      <StatusBadge v-if="tool" :status="tool.status" />
    </div>

    <div v-if="toolStore.loading" class="text-center py-20 text-gray-400">加载中...</div>

    <template v-else-if="tool">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <div class="bg-white rounded-lg p-6 shadow-sm">
            <table class="w-full">
              <tbody>
                <tr v-for="row in infoRows" :key="row.label" class="border-b border-gray-100">
                  <td class="py-3 text-sm text-gray-500 w-32">{{ row.label }}</td>
                  <td class="py-3 text-sm text-gray-800">{{ row.value }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="使用记录" name="usage">
          <div class="bg-white rounded-lg p-6 shadow-sm">
            <div class="space-y-4">
              <div
                v-for="record in usageRecords"
                :key="record.id"
                class="flex items-start gap-3"
              >
                <div class="w-2 h-2 rounded-full bg-blue-400 mt-2 shrink-0"></div>
                <div>
                  <p class="text-sm text-gray-700">{{ record.scenario }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">
                    {{ record.useDate }} · 时长 {{ record.durationMinutes }}分钟 · 操作人：{{ record.operator }}
                  </p>
                </div>
              </div>
              <div v-if="!usageRecords.length" class="text-center text-gray-400 py-8">暂无使用记录</div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="保养记录" name="maintenance">
          <div class="bg-white rounded-lg p-6 shadow-sm">
            <div class="space-y-3">
              <div
                v-for="record in maintenanceRecords"
                :key="record.id"
                class="p-3 bg-gray-50 rounded-lg"
              >
                <div class="flex items-center justify-between">
                  <span class="text-sm font-medium text-gray-700">{{ maintenanceTypeMap[record.maintenanceType] || record.maintenanceType }}</span>
                  <span class="text-xs text-gray-400">{{ record.maintenanceDate }}</span>
                </div>
                <p class="text-sm text-gray-500 mt-1">{{ record.description }}</p>
                <div class="text-xs text-gray-400 mt-1">
                  <span>操作人：{{ record.operator }}</span>
                  <span class="mx-2">·</span>
                  <span>总费用：¥{{ record.cost || 0 }}</span>
                </div>
                <div class="text-xs text-gray-400 mt-0.5">
                  人工 ¥{{ record.laborCost || 0 }} · 配件 ¥{{ record.partsCost || 0 }} · 其他 ¥{{ record.otherCost || 0 }}
                </div>
              </div>
              <div v-if="!maintenanceRecords.length" class="text-center text-gray-400 py-8">暂无保养记录</div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="维护轨迹" name="track">
          <div class="bg-white rounded-lg p-6 shadow-sm">
            <div class="mb-4 flex items-center gap-2">
              <div v-for="filter in trackFilters" :key="filter.type" class="flex items-center gap-1">
                <el-checkbox v-model="filter.enabled" @change="applyTrackFilter">
                  <span class="text-xs flex items-center gap-1">
                    <span :class="['w-2 h-2 rounded-full', filter.color]"></span>
                    {{ filter.label }}
                  </span>
                </el-checkbox>
              </div>
            </div>
            <div class="relative">
              <div class="absolute left-[11px] top-0 bottom-0 w-0.5 bg-gray-200"></div>
              <div v-if="filteredTracks.length" class="space-y-6">
                <div
                  v-for="(track, index) in filteredTracks"
                  :key="`${track.type}-${track.recordId}-${index}`"
                  class="relative pl-8"
                >
                  <div :class="[
                    'absolute left-0 top-1 w-6 h-6 rounded-full flex items-center justify-center text-white text-xs',
                    getTrackIconBg(track.type)
                  ]">
                    <component :is="getTrackIcon(track.type)" :size="12" />
                  </div>
                  <div class="bg-gray-50 rounded-lg p-4">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-sm font-medium text-gray-800 flex items-center gap-2">
                        <span :class="['px-2 py-0.5 rounded text-xs font-medium', getTrackBadgeClass(track.type)]">
                          {{ track.actionName }}
                        </span>
                        <span v-if="track.type === 'STATUS_CHANGE'" class="text-xs text-gray-500">
                          {{ statusMap[track.oldStatus!] }} → {{ statusMap[track.newStatus!] }}
                        </span>
                      </span>
                      <span class="text-xs text-gray-400">{{ track.actionDate }}</span>
                    </div>
                    <p v-if="track.description" class="text-sm text-gray-600 mb-2">{{ track.description }}</p>
                    <div class="flex flex-wrap items-center gap-4 text-xs text-gray-500">
                      <span v-if="track.durationMinutes" class="flex items-center gap-1">
                        <Clock :size="12" />
                        时长 {{ track.durationMinutes }}分钟
                      </span>
                      <span v-if="track.cost > 0" class="flex items-center gap-1">
                        <DollarSign :size="12" />
                        费用 ¥{{ track.cost }}
                      </span>
                      <span v-if="track.type === 'INVENTORY'" class="flex items-center gap-1">
                        <CheckCircle :size="12" :class="track.inventoryChecked ? 'text-green-500' : 'text-gray-400'" />
                        {{ track.inventoryChecked ? '已盘点' : '未盘点' }}
                        <span v-if="track.inventoryActualStatus" class="ml-1">
                          状态：{{ statusMap[track.inventoryActualStatus as ToolStatus] || track.inventoryActualStatus }}
                        </span>
                      </span>
                      <span class="flex items-center gap-1">
                        <User :size="12" />
                        操作人：{{ track.operator || '-' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center text-gray-400 py-12">暂无维护轨迹记录</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Clock, DollarSign, User, Wrench, Play, ClipboardList, ArrowRightLeft, CheckCircle } from 'lucide-vue-next'
import { useToolStore } from '@/stores/tool'
import { getUsageByTool } from '@/api/usage'
import { getMaintenanceByTool } from '@/api/maintenance'
import { getMaintenanceTrack } from '@/api/tool'
import StatusBadge from '@/components/StatusBadge.vue'
import type { UsageRecord, MaintenanceRecord, MaintenanceTrack, TrackType, ToolStatus } from '@/types'
import { normalizeLocationForDisplay } from '@/utils/location'

const route = useRoute()
const router = useRouter()
const toolStore = useToolStore()
const tool = computed(() => toolStore.currentTool)
const activeTab = ref('info')
const usageRecords = ref<UsageRecord[]>([])
const maintenanceRecords = ref<MaintenanceRecord[]>([])
const maintenanceTracks = ref<MaintenanceTrack[]>([])

const maintenanceTypeMap: Record<string, string> = {
  CLEAN: '清洁',
  OIL: '润滑',
  TIGHTEN: '紧固',
  INSPECT: '检查',
  REPAIR: '修理',
  OTHER: '其他'
}

const statusMap: Record<string, string> = {
  AVAILABLE: '可用',
  IN_USE: '使用中',
  MAINTENANCE: '保养中',
  LOANED: '已借出',
  LOST: '已遗失'
}

const trackFilters = reactive<{ type: TrackType; label: string; color: string; enabled: boolean }[]>([
  { type: 'USAGE', label: '使用', color: 'bg-blue-500', enabled: true },
  { type: 'MAINTENANCE', label: '保养', color: 'bg-orange-500', enabled: true },
  { type: 'INVENTORY', label: '盘点', color: 'bg-green-500', enabled: true },
  { type: 'STATUS_CHANGE', label: '状态变更', color: 'bg-purple-500', enabled: true }
])

const filteredTracks = computed(() => {
  const enabledTypes = trackFilters.filter(f => f.enabled).map(f => f.type)
  return maintenanceTracks.value.filter(t => enabledTypes.includes(t.type))
})

function applyTrackFilter() {}

function getTrackIconBg(type: TrackType): string {
  const map: Record<TrackType, string> = {
    USAGE: 'bg-blue-500',
    MAINTENANCE: 'bg-orange-500',
    INVENTORY: 'bg-green-500',
    STATUS_CHANGE: 'bg-purple-500'
  }
  return map[type]
}

function getTrackIcon(type: TrackType) {
  const map: Record<TrackType, any> = {
    USAGE: Play,
    MAINTENANCE: Wrench,
    INVENTORY: ClipboardList,
    STATUS_CHANGE: ArrowRightLeft
  }
  return map[type]
}

function getTrackBadgeClass(type: TrackType): string {
  const map: Record<TrackType, string> = {
    USAGE: 'bg-blue-100 text-blue-700',
    MAINTENANCE: 'bg-orange-100 text-orange-700',
    INVENTORY: 'bg-green-100 text-green-700',
    STATUS_CHANGE: 'bg-purple-100 text-purple-700'
  }
  return map[type]
}

const latestMaintenance = computed(() => {
  if (!maintenanceRecords.value.length) return null
  return maintenanceRecords.value[0]
})

const infoRows = computed(() => {
  if (!tool.value) return []
  const rows = [
    { label: '名称', value: tool.value.name },
    { label: '型号', value: tool.value.model || '-' },
    { label: '品牌', value: tool.value.brand || '-' },
    { label: '分类ID', value: `${tool.value.categoryId} / ${tool.value.subCategoryId}` },
    { label: '位置', value: normalizeLocationForDisplay(tool.value.location) },
    { label: '购入日期', value: tool.value.purchaseDate || '-' },
    { label: '价格', value: tool.value.price ? `¥${tool.value.price}` : '-' },
    { label: '保养周期', value: tool.value.maintenanceCycleDays ? `${tool.value.maintenanceCycleDays}天` : '-' },
    { label: '上次保养', value: tool.value.lastMaintenanceDate || '-' },
    { label: '下次保养', value: tool.value.nextMaintenanceDate || '-' }
  ]
  if (latestMaintenance.value) {
    const m = latestMaintenance.value
    rows.push({
      label: '最近保养费用',
      value: `¥${m.cost || 0}（人工 ¥${m.laborCost || 0} + 配件 ¥${m.partsCost || 0} + 其他 ¥${m.otherCost || 0}）`
    })
  }
  return rows
})

onMounted(async () => {
  const id = Number(route.params.id)
  await toolStore.fetchTool(id)

  try {
    const usageRes = await getUsageByTool(id, { size: 20 })
    usageRecords.value = usageRes.data?.list || []
  } catch (e) {
    console.warn('获取使用记录失败', e)
  }

  try {
    const maintRes = await getMaintenanceByTool(id, { size: 20 })
    maintenanceRecords.value = maintRes.data?.list || []
  } catch (e) {
    console.warn('获取保养记录失败', e)
  }

  try {
    const trackRes = await getMaintenanceTrack(id)
    maintenanceTracks.value = trackRes.data || []
  } catch (e) {
    console.warn('获取维护轨迹失败', e)
  }
})
</script>
