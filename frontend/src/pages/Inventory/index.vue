<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">盘点管理</h2>
      <el-button type="primary" @click="handleCreate">
        <Plus :size="16" class="mr-1" /> 新建盘点
      </el-button>
    </div>

    <div v-if="!currentInventory" class="bg-white rounded-lg p-6 shadow-sm">
      <h3 class="text-lg font-semibold text-gray-700 mb-4">盘点历史</h3>
      <div v-if="inventoryStore.loading" class="text-center py-10 text-gray-400">加载中...</div>
      <div v-else-if="inventoryStore.inventories.length" class="space-y-3">
        <div
          v-for="inv in inventoryStore.inventories"
          :key="inv.id"
          class="flex items-center justify-between p-4 border border-gray-100 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors"
          @click="handleSelect(inv.id)"
        >
          <div>
            <p class="font-medium text-gray-800">盘点 #{{ inv.id }}</p>
            <p class="text-sm text-gray-400 mt-1">{{ inv.inventoryDate }} · 操作人：{{ inv.operator }}</p>
          </div>
          <div class="flex items-center gap-3">
            <span
              class="text-xs px-2 py-0.5 rounded"
              :class="inv.completed ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'"
            >
              {{ inv.completed ? '已完成' : '进行中' }}
            </span>
            <span class="text-sm text-gray-500">{{ inv.checkedTools }}/{{ inv.totalTools }}</span>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-10 text-gray-400">暂无盘点记录</div>
    </div>

    <div v-else>
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-3">
          <el-button @click="handleBack">返回列表</el-button>
          <h3 class="text-lg font-semibold text-gray-800">盘点 #{{ currentInventory.id }}</h3>
          <span class="text-xs text-gray-400">{{ currentInventory.inventoryDate }} · 快照已冻结</span>
          <span
            v-if="currentInventory.completed"
            class="text-xs px-2 py-0.5 rounded bg-green-100 text-green-700"
          >
            盘点已完成
          </span>
          <span v-else class="text-xs px-2 py-0.5 rounded bg-blue-100 text-blue-700">
            进行中
          </span>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-500">
            进度：{{ currentInventory.checkedTools }}/{{ currentInventory.totalTools }}
          </span>
          <el-progress
            :percentage="progressPercent"
            :stroke-width="8"
            class="w-40"
          />
          <el-button
            type="success"
            @click="handleCompleteInventory"
            :disabled="currentInventory.completed || completing"
            :loading="completing"
          >
            <CheckCircle :size="16" class="mr-1" />
            完成盘点
          </el-button>
        </div>
      </div>

      <div class="grid grid-cols-4 gap-4 mb-6">
        <div class="bg-white rounded-lg p-5 shadow-sm border border-green-100">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-medium text-gray-600">已核对</span>
            <div class="w-8 h-8 rounded-lg bg-green-100 flex items-center justify-center">
              <Check :size="16" class="text-green-600" />
            </div>
          </div>
          <div class="text-2xl font-bold text-gray-800 mb-1">
            {{ progress?.checkedCount || 0 }}
          </div>
          <div class="flex items-center justify-between">
            <span class="text-xs text-gray-400">占总数</span>
            <span class="text-sm font-semibold text-green-600">{{ progress?.checkedPercent || 0 }}%</span>
          </div>
          <el-progress
            :percentage="progress?.checkedPercent || 0"
            :stroke-width="4"
            color="#10b981"
            class="mt-3"
            :show-text="false"
          />
        </div>

        <div class="bg-white rounded-lg p-5 shadow-sm border border-amber-100">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-medium text-gray-600">未核对</span>
            <div class="w-8 h-8 rounded-lg bg-amber-100 flex items-center justify-center">
              <Clock :size="16" class="text-amber-600" />
            </div>
          </div>
          <div class="text-2xl font-bold text-gray-800 mb-1">
            {{ progress?.uncheckedCount || 0 }}
          </div>
          <div class="flex items-center justify-between">
            <span class="text-xs text-gray-400">占总数</span>
            <span class="text-sm font-semibold text-amber-600">{{ progress?.uncheckedPercent || 0 }}%</span>
          </div>
          <el-progress
            :percentage="progress?.uncheckedPercent || 0"
            :stroke-width="4"
            color="#f59e0b"
            class="mt-3"
            :show-text="false"
          />
        </div>

        <div class="bg-white rounded-lg p-5 shadow-sm border border-red-100">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-medium text-gray-600">状态不符</span>
            <div class="w-8 h-8 rounded-lg bg-red-100 flex items-center justify-center">
              <AlertTriangle :size="16" class="text-red-600" />
            </div>
          </div>
          <div class="text-2xl font-bold text-gray-800 mb-1">
            {{ progress?.mismatchedCount || 0 }}
          </div>
          <div class="flex items-center justify-between">
            <span class="text-xs text-gray-400">占总数</span>
            <span class="text-sm font-semibold text-red-600">{{ progress?.mismatchedPercent || 0 }}%</span>
          </div>
          <el-progress
            :percentage="progress?.mismatchedPercent || 0"
            :stroke-width="4"
            color="#ef4444"
            class="mt-3"
            :show-text="false"
          />
        </div>

        <div class="bg-white rounded-lg p-5 shadow-sm border border-gray-200">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-medium text-gray-600">遗失</span>
            <div class="w-8 h-8 rounded-lg bg-gray-200 flex items-center justify-center">
              <HelpCircle :size="16" class="text-gray-600" />
            </div>
          </div>
          <div class="text-2xl font-bold text-gray-800 mb-1">
            {{ progress?.lostCount || 0 }}
          </div>
          <div class="flex items-center justify-between">
            <span class="text-xs text-gray-400">占总数</span>
            <span class="text-sm font-semibold text-gray-600">{{ progress?.lostPercent || 0 }}%</span>
          </div>
          <el-progress
            :percentage="progress?.lostPercent || 0"
            :stroke-width="4"
            color="#6b7280"
            class="mt-3"
            :show-text="false"
          />
        </div>
      </div>

      <DifferencePanel :inventory-id="currentInventory.id" class="mb-6" />

      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="flex items-center justify-between px-4 py-3 border-b border-gray-100">
          <h4 class="font-semibold text-gray-700">盘点明细（快照）</h4>
          <div class="flex items-center gap-2 text-xs text-gray-400">
            <Camera :size="14" />
            <span>盘点创建时已冻结工具快照信息</span>
          </div>
        </div>
        <el-table :data="inventoryStore.inventoryItems" stripe>
          <el-table-column prop="toolId" label="工具ID" width="80" />
          <el-table-column label="工具名称" min-width="140">
            <template #default="{ row }">
              <div>
                <span class="font-medium">{{ row.toolName || `工具#${row.toolId}` }}</span>
                <span v-if="row.toolModel" class="text-xs text-gray-400 ml-1">{{ row.toolModel }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="分类" width="100">
            <template #default="{ row }">
              <span class="text-xs text-gray-500">{{ row.categoryName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="位置" width="120">
            <template #default="{ row }">
              <span class="text-xs text-gray-500">{{ row.location || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="快照状态" width="90" align="center">
            <template #default="{ row }">
              <StatusBadge :status="row.snapshotStatus || row.expectedStatus" />
            </template>
          </el-table-column>
          <el-table-column label="预期状态" width="90" align="center">
            <template #default="{ row }">
              <StatusBadge :status="row.expectedStatus" />
            </template>
          </el-table-column>
          <el-table-column label="实际状态" width="200">
            <template #default="{ row }">
              <el-radio-group
                :model-value="row.actualStatus"
                @update:model-value="(val: string) => handleUpdateItem(row, val)"
                :disabled="row.checked || currentInventory.completed"
              >
                <el-radio value="AVAILABLE">在位</el-radio>
                <el-radio value="LOANED">借用</el-radio>
                <el-radio value="MAINTENANCE">维修</el-radio>
                <el-radio value="LOST">丢失</el-radio>
              </el-radio-group>
            </template>
          </el-table-column>
          <el-table-column label="核对" width="80" align="center">
            <template #default="{ row }">
              <el-checkbox
                :model-value="row.checked"
                @update:model-value="(val: boolean) => handleCheckItem(row, val)"
                :disabled="currentInventory.completed"
              />
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remarks">
            <template #default="{ row }">
              <el-input
                v-model="row.remarks"
                size="small"
                placeholder="备注"
                @blur="handleUpdateRemark(row)"
                :disabled="currentInventory.completed"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Camera, CheckCircle, Check, Clock, AlertTriangle, HelpCircle } from 'lucide-vue-next'
import { useInventoryStore } from '@/stores/inventory'
import StatusBadge from '@/components/StatusBadge.vue'
import DifferencePanel from '@/components/DifferencePanel.vue'
import type { Inventory, InventoryItem, ToolStatus, InventoryProgress } from '@/types'

const inventoryStore = useInventoryStore()
const currentInventory = ref<Inventory | null>(null)
const progress = ref<InventoryProgress | null>(null)
const completing = ref(false)

const progressPercent = computed(() => {
  if (!currentInventory.value || !currentInventory.value.totalTools) return 0
  return Math.round((currentInventory.value.checkedTools / currentInventory.value.totalTools) * 100)
})

onMounted(() => inventoryStore.fetchInventories())

async function handleCreate() {
  await ElMessageBox.confirm('确定新建盘点？将自动生成所有工具的盘点清单并冻结快照。', '提示', { type: 'info' })
  const inv = await inventoryStore.createInventory()
  ElMessage.success('盘点创建成功，工具快照已冻结')
  await inventoryStore.fetchInventories()
  if (inv) handleSelect(inv.id)
}

function handleBack() {
  currentInventory.value = null
  progress.value = null
}

async function handleSelect(id: number) {
  await inventoryStore.fetchInventory(id)
  await inventoryStore.fetchInventoryItems(id)
  await inventoryStore.fetchInventoryProgress(id)
  currentInventory.value = inventoryStore.currentInventory
  progress.value = inventoryStore.inventoryProgress
}

async function refreshProgress() {
  if (currentInventory.value) {
    await inventoryStore.fetchInventoryProgress(currentInventory.value.id)
    progress.value = inventoryStore.inventoryProgress
    await inventoryStore.fetchInventory(currentInventory.value.id)
    currentInventory.value = inventoryStore.currentInventory
  }
}

async function handleUpdateItem(row: InventoryItem, val: string) {
  await inventoryStore.updateItem(currentInventory.value!.id, row.toolId, { actualStatus: val })
  row.actualStatus = val as ToolStatus
  await refreshProgress()
}

async function handleCheckItem(row: InventoryItem, val: boolean) {
  await inventoryStore.updateItem(currentInventory.value!.id, row.toolId, { checked: val })
  row.checked = val
  await refreshProgress()
}

async function handleUpdateRemark(row: InventoryItem) {
  await inventoryStore.updateItem(currentInventory.value!.id, row.toolId, { remarks: row.remarks })
}

async function handleCompleteInventory() {
  if (!currentInventory.value) return

  const uncheckedCount = progress.value?.uncheckedCount || 0
  let confirmMsg = '确定完成本次盘点？完成后将根据盘点结果批量更新工具实际状态，且不可撤销。'
  if (uncheckedCount > 0) {
    confirmMsg = `当前还有 ${uncheckedCount} 项未核对，确定完成盘点吗？完成后将根据盘点结果批量更新工具实际状态，且不可撤销。`
  }

  await ElMessageBox.confirm(confirmMsg, '完成盘点确认', {
    type: 'warning',
    confirmButtonText: '确认完成',
    cancelButtonText: '取消'
  })

  completing.value = true
  try {
    const result = await inventoryStore.completeInventory(currentInventory.value.id)
    if (result.success) {
      ElMessage.success(`${result.message}（共更新 ${result.totalUpdated} 个工具状态）`)
      await refreshProgress()
    } else {
      ElMessage.warning(result.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '完成盘点失败')
  } finally {
    completing.value = false
  }
}
</script>
