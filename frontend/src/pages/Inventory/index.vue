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
          <el-button @click="currentInventory = null">返回列表</el-button>
          <h3 class="text-lg font-semibold text-gray-800">盘点 #{{ currentInventory.id }}</h3>
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

      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <el-table :data="inventoryStore.inventoryItems" stripe>
          <el-table-column prop="toolId" label="工具ID" width="100" />
          <el-table-column prop="expectedStatus" label="预期状态" width="100">
            <template #default="{ row }">{{ statusLabel(row.expectedStatus) }}</template>
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
import { Plus } from 'lucide-vue-next'
import { useInventoryStore } from '@/stores/inventory'
import type { Inventory, InventoryItem, ToolStatus } from '@/types'

const inventoryStore = useInventoryStore()
const currentInventory = ref<Inventory | null>(null)

const progressPercent = computed(() => {
  if (!currentInventory.value || !currentInventory.value.totalTools) return 0
  return Math.round((currentInventory.value.checkedTools / currentInventory.value.totalTools) * 100)
})

const statusLabels: Record<string, string> = {
  AVAILABLE: '在位',
  IN_USE: '使用中',
  MAINTENANCE: '维修',
  LOANED: '借用',
  LOST: '丢失'
}

function statusLabel(status: string): string {
  return statusLabels[status] || status
}

onMounted(() => inventoryStore.fetchInventories())

async function handleCreate() {
  await ElMessageBox.confirm('确定新建盘点？将自动生成所有工具的盘点清单。', '提示', { type: 'info' })
  const inv = await inventoryStore.createInventory()
  ElMessage.success('盘点创建成功')
  await inventoryStore.fetchInventories()
  if (inv) handleSelect(inv.id)
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
