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
            <span class="text-xs px-2 py-0.5 rounded bg-green-100 text-green-700">
              已完成
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
                :disabled="row.checked"
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
import { Plus, Camera } from 'lucide-vue-next'
import { useInventoryStore } from '@/stores/inventory'
import StatusBadge from '@/components/StatusBadge.vue'
import DifferencePanel from '@/components/DifferencePanel.vue'
import type { Inventory, InventoryItem, ToolStatus } from '@/types'

const inventoryStore = useInventoryStore()
const currentInventory = ref<Inventory | null>(null)

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
}

async function handleSelect(id: number) {
  await inventoryStore.fetchInventory(id)
  await inventoryStore.fetchInventoryItems(id)
  currentInventory.value = inventoryStore.currentInventory
}

async function handleUpdateItem(row: InventoryItem, val: string) {
  await inventoryStore.updateItem(currentInventory.value!.id, row.id, { actualStatus: val })
  row.actualStatus = val as ToolStatus
}

async function handleCheckItem(row: InventoryItem, val: boolean) {
  await inventoryStore.updateItem(currentInventory.value!.id, row.id, { checked: val })
  row.checked = val
  if (currentInventory.value) {
    currentInventory.value.checkedTools = inventoryStore.inventoryItems.filter((i: InventoryItem) => i.checked).length
  }
}

async function handleUpdateRemark(row: InventoryItem) {
  await inventoryStore.updateItem(currentInventory.value!.id, row.id, { remarks: row.remarks })
}
</script>
