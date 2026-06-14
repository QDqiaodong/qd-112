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
                <p class="text-xs text-gray-400 mt-1">操作人：{{ record.operator }} · 费用：¥{{ record.cost }}</p>
              </div>
              <div v-if="!maintenanceRecords.length" class="text-center text-gray-400 py-8">暂无保养记录</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useToolStore } from '@/stores/tool'
import { getUsageByTool } from '@/api/usage'
import { getMaintenanceByTool } from '@/api/maintenance'
import StatusBadge from '@/components/StatusBadge.vue'
import type { UsageRecord, MaintenanceRecord } from '@/types'

const route = useRoute()
const router = useRouter()
const toolStore = useToolStore()
const tool = computed(() => toolStore.currentTool)
const activeTab = ref('info')
const usageRecords = ref<UsageRecord[]>([])
const maintenanceRecords = ref<MaintenanceRecord[]>([])

const maintenanceTypeMap: Record<string, string> = {
  CLEAN: '清洁',
  OIL: '润滑',
  TIGHTEN: '紧固',
  INSPECT: '检查',
  REPAIR: '修理',
  OTHER: '其他'
}

const infoRows = computed(() => {
  if (!tool.value) return []
  return [
    { label: '名称', value: tool.value.name },
    { label: '型号', value: tool.value.model || '-' },
    { label: '品牌', value: tool.value.brand || '-' },
    { label: '分类ID', value: `${tool.value.categoryId} / ${tool.value.subCategoryId}` },
    { label: '位置', value: tool.value.location },
    { label: '购入日期', value: tool.value.purchaseDate || '-' },
    { label: '价格', value: tool.value.price ? `¥${tool.value.price}` : '-' },
    { label: '保养周期', value: tool.value.maintenanceCycleDays ? `${tool.value.maintenanceCycleDays}天` : '-' }
  ]
})

onMounted(async () => {
  const id = Number(route.params.id)
  await toolStore.fetchTool(id)
  try {
    const [usageRes, maintRes] = await Promise.all([
      getUsageByTool(id, { size: 20 }),
      getMaintenanceByTool(id, { size: 20 })
    ])
    usageRecords.value = usageRes.data?.list || []
    maintenanceRecords.value = maintRes.data?.list || []
  } catch {
    // silently handle
  }
})
</script>
