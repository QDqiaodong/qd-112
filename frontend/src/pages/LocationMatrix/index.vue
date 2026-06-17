<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">
        <LayoutGrid :size="24" class="inline mr-2 -mt-1" />
        工具柜位置矩阵
      </h2>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div class="flex flex-wrap items-center gap-4">
        <div class="flex items-center gap-2">
          <MapPin :size="18" class="text-gray-500" />
          <span class="text-sm text-gray-600">房间：</span>
          <el-select
            v-model="selectedRoom"
            placeholder="选择房间"
            clearable
            class="w-40"
            @change="handleRoomChange"
          >
            <el-option
              v-for="room in roomList"
              :key="room"
              :label="room"
              :value="room"
            />
          </el-select>
        </div>
        <div class="flex items-center gap-2">
          <Box :size="18" class="text-gray-500" />
          <span class="text-sm text-gray-600">工具柜：</span>
          <el-select
            v-model="selectedCabinet"
            placeholder="选择工具柜"
            clearable
            class="w-40"
            :disabled="!selectedRoom"
            @change="handleCabinetChange"
          >
            <el-option
              v-for="cab in cabinetList"
              :key="cab"
              :label="cab"
              :value="cab"
            />
          </el-select>
        </div>
        <div class="flex items-center gap-4 ml-auto">
          <div class="flex items-center gap-1.5">
            <span class="inline-block w-3 h-3 rounded bg-green-500"></span>
            <span class="text-xs text-gray-600">可用</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="inline-block w-3 h-3 rounded bg-blue-500"></span>
            <span class="text-xs text-gray-600">使用中</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="inline-block w-3 h-3 rounded bg-orange-500"></span>
            <span class="text-xs text-gray-600">保养中</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="inline-block w-3 h-3 rounded bg-purple-500"></span>
            <span class="text-xs text-gray-600">借出</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="inline-block w-3 h-3 rounded bg-red-500"></span>
            <span class="text-xs text-gray-600">遗失</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="inline-block w-3 h-3 rounded bg-gray-300 border border-dashed border-gray-400"></span>
            <span class="text-xs text-gray-600">空位</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="toolStore.loading" class="text-center py-20 text-gray-400">
      <Loader2 :size="32" class="animate-spin mx-auto mb-2" />
      <p>加载中...</p>
    </div>

    <div v-else-if="!selectedRoom" class="bg-white rounded-lg shadow-sm p-16 text-center">
      <MapPin :size="64" class="mx-auto text-gray-300 mb-4" />
      <p class="text-gray-500 text-lg">请先选择房间查看工具柜分布</p>
    </div>

    <div v-else-if="!selectedCabinet && cabinetList.length === 0" class="bg-white rounded-lg shadow-sm p-16 text-center">
      <Box :size="64" class="mx-auto text-gray-300 mb-4" />
      <p class="text-gray-500 text-lg">该房间暂无工具柜数据</p>
    </div>

    <template v-else>
      <div v-for="cabinet in displayCabinets" :key="cabinet" class="mb-8">
        <div class="flex items-center gap-3 mb-4">
          <div class="h-8 w-1 bg-primary rounded"></div>
          <h3 class="text-lg font-semibold text-gray-800">工具柜 {{ cabinet }}</h3>
          <el-tag size="small" type="info" effect="plain">
            共 {{ cabinetStats[cabinet]?.total || 0 }} 个工具
          </el-tag>
          <div class="flex items-center gap-2 ml-auto">
            <el-tag size="small" type="success" effect="light">
              可用 {{ cabinetStats[cabinet]?.AVAILABLE || 0 }}
            </el-tag>
            <el-tag size="small" type="primary" effect="light">
              使用中 {{ cabinetStats[cabinet]?.IN_USE || 0 }}
            </el-tag>
            <el-tag size="small" type="warning" effect="light">
              保养 {{ cabinetStats[cabinet]?.MAINTENANCE || 0 }}
            </el-tag>
          </div>
        </div>

        <div v-for="shelf in getShelves(cabinet)" :key="`${cabinet}-${shelf}`" class="bg-white rounded-lg shadow-sm p-5 mb-4 border-l-4 border-gray-200">
          <div class="flex items-center gap-2 mb-4">
            <Layers :size="16" class="text-gray-500" />
            <span class="text-sm font-medium text-gray-700">第 {{ shelf }} 层</span>
            <span class="text-xs text-gray-400">（{{ cabinet }}-{{ shelf }}）</span>
          </div>

          <div class="grid grid-cols-6 gap-3">
            <div
              v-for="cell in getCells(cabinet, shelf)"
              :key="`${cabinet}-${shelf}-${cell}`"
              class="relative"
            >
              <div
                v-if="getToolInCell(cabinet, shelf, cell)"
                class="cell-card cursor-pointer"
                :class="statusCardClass(getToolInCell(cabinet, shelf, cell)!.status)"
                @click="openToolSummary(getToolInCell(cabinet, shelf, cell)!)"
              >
                <div class="flex items-center justify-between mb-1">
                  <span class="text-xs font-bold text-white/90">
                  {{ cell }}
                  </span>
                  <component
                    :is="statusIcon(getToolInCell(cabinet, shelf, cell)!.status)"
                    :size="12"
                    class="text-white/80"
                  />
                </div>
                <div class="text-sm font-semibold text-white truncate">
                  {{ getToolInCell(cabinet, shelf, cell)!.name }}
                </div>
                <div class="text-[11px] text-white/70 truncate">
                  {{ getToolInCell(cabinet, shelf, cell)!.model || '—' }}
                </div>
              </div>
              <div
                v-else
                class="cell-empty"
              >
                <span class="text-xs font-bold text-gray-400">{{ cell }}</span>
                <span class="text-[11px] text-gray-300">空</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <el-dialog
      v-model="summaryDialogVisible"
      :title="currentTool?.name || '工具摘要'"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="currentTool" class="space-y-4">
        <div class="flex items-start gap-4 pb-4 border-b border-gray-100">
          <div
            class="w-16 h-16 rounded-lg flex items-center justify-center"
            :class="statusBgClass(currentTool.status)"
          >
            <Wrench :size="32" class="text-white" />
          </div>
          <div class="flex-1">
            <h3 class="text-xl font-bold text-gray-800">{{ currentTool.name }}</h3>
            <div class="flex items-center gap-2 mt-1">
              <StatusBadge :status="currentTool.status" />
              <span class="text-sm text-gray-500">ID: #{{ currentTool.id }}</span>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="text-xs text-gray-500 mb-1">型号</p>
            <p class="text-sm font-medium text-gray-800">{{ currentTool.model || '—' }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">品牌</p>
            <p class="text-sm font-medium text-gray-800">{{ currentTool.brand || '—' }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">位置</p>
            <p class="text-sm font-medium text-gray-800">{{ normalizeLocationForDisplay(currentTool.location) }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">购入日期</p>
            <p class="text-sm font-medium text-gray-800">{{ currentTool.purchaseDate || '—' }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">价格</p>
            <p class="text-sm font-medium text-gray-800">
              {{ currentTool.price ? '¥' + currentTool.price.toFixed(2) : '—' }}
            </p>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">保养周期</p>
            <p class="text-sm font-medium text-gray-800">
              {{ currentTool.maintenanceCycleDays ? currentTool.maintenanceCycleDays + ' 天' : '—' }}
            </p>
          </div>
        </div>

        <div>
          <p class="text-xs text-gray-500 mb-1">用途</p>
          <p class="text-sm text-gray-700">{{ currentTool.purpose || '—' }}</p>
        </div>

        <div>
          <p class="text-xs text-gray-500 mb-1">规格参数</p>
          <p class="text-sm text-gray-700">{{ currentTool.specification || '—' }}</p>
        </div>

        <div class="flex items-center gap-4 pt-2 border-t border-gray-100">
          <div class="flex-1">
            <p class="text-xs text-gray-500 mb-1">上次保养</p>
            <p class="text-sm font-medium text-gray-800">{{ currentTool.lastMaintenanceDate || '—' }}</p>
          </div>
          <div class="flex-1">
            <p class="text-xs text-gray-500 mb-1">下次保养</p>
            <p class="text-sm font-medium" :class="isOverdue(currentTool.nextMaintenanceDate) ? 'text-red-600' : 'text-gray-800'">
              {{ currentTool.nextMaintenanceDate || '—' }}
              <el-tag v-if="isOverdue(currentTool.nextMaintenanceDate)" size="small" type="danger" effect="light" class="ml-1">
                已逾期
              </el-tag>
            </p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="summaryDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToDetail">查看详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { LayoutGrid, MapPin, Box, Layers, Wrench, Loader2, CheckCircle, Clock, Wrench as WrenchIcon, Handshake, AlertTriangle } from 'lucide-vue-next'
import { useToolStore } from '@/stores/tool'
import StatusBadge from '@/components/StatusBadge.vue'
import type { Tool, ToolStatus } from '@/types'
import { parseLocation as parseLocationUtil, normalizeLocation, normalizeLocationForDisplay } from '@/utils/location'
import type { ParsedLocation } from '@/utils/location'

const router = useRouter()
const toolStore = useToolStore()

const selectedRoom = ref<string>('')
const selectedCabinet = ref<string>('')
const summaryDialogVisible = ref(false)
const currentTool = ref<Tool | null>(null)

function parseLocation(location: string): ParsedLocation | null {
  return parseLocationUtil(normalizeLocation(location))
}

const allTools = computed(() => toolStore.tools)

const parsedTools = computed(() => {
  return allTools.value
    .map(tool => ({
      tool,
      loc: parseLocation(tool.location)
    }))
    .filter(item => item.loc !== null) as { tool: Tool; loc: ParsedLocation }[]
})

const roomList = computed(() => {
  const rooms = new Set<string>()
  parsedTools.value.forEach(item => rooms.add(item.loc.room))
  return Array.from(rooms).sort()
})

const cabinetList = computed(() => {
  const cabinets = new Set<string>()
  parsedTools.value
    .filter(item => !selectedRoom.value || item.loc.room === selectedRoom.value)
    .forEach(item => cabinets.add(item.loc.cabinet))
  return Array.from(cabinets).sort()
})

const displayCabinets = computed(() => {
  if (selectedCabinet.value) {
    return [selectedCabinet.value]
  }
  return cabinetList.value
})

const cabinetStats = computed(() => {
  const stats: Record<string, Record<string, number>> = {}
  parsedTools.value.forEach(item => {
    const cab = item.loc.cabinet
    if (!stats[cab]) {
      stats[cab] = { total: 0, AVAILABLE: 0, IN_USE: 0, MAINTENANCE: 0, LOANED: 0, LOST: 0 }
    }
    stats[cab].total++
    if (stats[cab][item.tool.status] !== undefined) {
      stats[cab][item.tool.status]++
    }
  })
  return stats
})

function getShelves(cabinet: string): string[] {
  const shelves = new Set<string>()
  parsedTools.value
    .filter(item => item.loc.cabinet === cabinet)
    .forEach(item => shelves.add(item.loc.shelf))
  const sorted = Array.from(shelves).sort((a, b) => {
    const na = parseInt(a) || 0
    const nb = parseInt(b) || 0
    return na - nb
  })
  return sorted.length ? sorted : ['1']
}

function getCells(cabinet: string, shelf: string): string[] {
  const toolsInShelf = parsedTools.value
    .filter(item => item.loc.cabinet === cabinet && item.loc.shelf === shelf)
  
  const cellNums = toolsInShelf.map(item => parseInt(item.loc.cell) || 1)
  const maxCell = Math.max(6, ...cellNums)
  const cells: string[] = []
  for (let i = 1; i <= maxCell; i++) {
    cells.push(String(i))
  }
  return cells
}

function getToolInCell(cabinet: string, shelf: string, cell: string): Tool | null {
  const found = parsedTools.value.find(
    item => item.loc.cabinet === cabinet && item.loc.shelf === shelf && item.loc.cell === cell
  )
  return found ? found.tool : null
}

function statusCardClass(status: ToolStatus): string {
  const map: Record<ToolStatus, string> = {
    AVAILABLE: 'status-available',
    IN_USE: 'status-in-use',
    MAINTENANCE: 'status-maintenance',
    LOANED: 'status-loaned',
    LOST: 'status-lost'
  }
  return map[status] || 'status-available'
}

function statusBgClass(status: ToolStatus): string {
  const map: Record<ToolStatus, string> = {
    AVAILABLE: 'bg-green-500',
    IN_USE: 'bg-blue-500',
    MAINTENANCE: 'bg-orange-500',
    LOANED: 'bg-purple-500',
    LOST: 'bg-red-500'
  }
  return map[status] || 'bg-gray-500'
}

function statusIcon(status: ToolStatus) {
  const map: Record<ToolStatus, any> = {
    AVAILABLE: CheckCircle,
    IN_USE: Clock,
    MAINTENANCE: WrenchIcon,
    LOANED: Handshake,
    LOST: AlertTriangle
  }
  return map[status] || CheckCircle
}

function openToolSummary(tool: Tool) {
  currentTool.value = tool
  summaryDialogVisible.value = true
}

function goToDetail() {
  if (currentTool.value) {
    summaryDialogVisible.value = false
    router.push(`/tools/${currentTool.value.id}`)
  }
}

function isOverdue(dateStr?: string): boolean {
  if (!dateStr) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return new Date(dateStr) < today
}

function handleRoomChange() {
  selectedCabinet.value = ''
}

function handleCabinetChange() {
}

onMounted(() => {
  loadAllTools()
})

async function loadAllTools() {
  await toolStore.fetchTools({ page: 1, size: 9999 })
  if (roomList.value.length > 0) {
    selectedRoom.value = roomList.value[0]
  }
}
</script>

<style scoped>
.cell-card {
  @apply rounded-lg p-3 min-h-[80px] transition-all duration-200 hover:scale-[1.02] hover:shadow-lg;
}

.cell-card:hover {
  transform: translateY(-2px);
}

.status-available {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

.status-in-use {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.status-maintenance {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.status-loaned {
  background: linear-gradient(135deg, #a855f7 0%, #9333ea 100%);
}

.status-lost {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.cell-empty {
  @apply rounded-lg p-3 min-h-[80px] border-2 border-dashed border-gray-200 flex flex-col items-center justify-center bg-gray-50;
}
</style>
