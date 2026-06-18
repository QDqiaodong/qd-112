<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">维修工具包</h2>
        <p class="text-sm text-gray-500 mt-1">管理家庭维修常用工具组合，查看清单和缺失状态</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <Plus :size="16" class="mr-1" /> 新建工具包
      </el-button>
    </div>

    <div v-if="toolKitStore.loading" class="text-center py-20 text-gray-400">加载中...</div>

    <div v-else-if="toolKitStore.toolKits.length" class="grid grid-cols-3 gap-6">
      <div
        v-for="kit in toolKitStore.toolKits"
        :key="kit.id"
        class="bg-white rounded-lg p-5 shadow-sm card-hover cursor-pointer"
        @click="viewKit(kit.id)"
      >
        <div class="flex items-start justify-between mb-3">
          <h3 class="text-lg font-semibold text-gray-800">{{ kit.name }}</h3>
          <div class="flex items-center gap-2">
            <el-tag v-if="kitDetailMap[kit.id]?.missingItems && kitDetailMap[kit.id].missingItems > 0" type="danger" effect="light">
              缺失 {{ kitDetailMap[kit.id].missingItems }}
            </el-tag>
            <el-tag v-else-if="kitDetailMap[kit.id]" type="success" effect="light">
              完整
            </el-tag>
          </div>
        </div>

        <p v-if="kit.description" class="text-sm text-gray-500 mb-3 line-clamp-2">{{ kit.description }}</p>
        <p v-if="kit.scenario" class="text-xs text-gray-400 mb-4">适用场景：{{ kit.scenario }}</p>

        <div v-if="kitDetailMap[kit.id]" class="flex items-center gap-4 mb-4">
          <div class="text-center">
            <p class="text-2xl font-bold text-gray-700">{{ kitDetailMap[kit.id].totalItems }}</p>
            <p class="text-xs text-gray-400">工具总数</p>
          </div>
          <div class="text-center">
            <p class="text-2xl font-bold text-green-600">{{ kitDetailMap[kit.id].availableItems }}</p>
            <p class="text-xs text-gray-400">可用</p>
          </div>
          <div class="text-center">
            <p class="text-2xl font-bold" :class="kitDetailMap[kit.id].missingItems > 0 ? 'text-red-600' : 'text-gray-300'">
              {{ kitDetailMap[kit.id].missingItems }}
            </p>
            <p class="text-xs text-gray-400">缺失</p>
          </div>
          <div class="flex-1 ml-4">
            <el-progress
              :percentage="kitDetailMap[kit.id].totalItems ? Math.round(kitDetailMap[kit.id].availableItems / kitDetailMap[kit.id].totalItems * 100) : 0"
              :color="kitDetailMap[kit.id].missingItems > 0 ? '#ef4444' : '#22c55e'"
              :stroke-width="8"
            />
          </div>
        </div>
        <div v-else class="mb-4">
          <el-skeleton :rows="2" animated />
        </div>

        <div class="flex items-center gap-2 pt-3 border-t border-gray-100">
          <el-button text size="small" type="primary" @click.stop="viewKit(kit.id)">查看详情</el-button>
          <el-button text size="small" @click.stop="openEditDialog(kit)">编辑</el-button>
          <el-button text size="small" type="danger" @click.stop="handleDelete(kit.id)">删除</el-button>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20">
      <Briefcase :size="48" class="mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400">暂无工具包，点击右上角新建</p>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑工具包' : '新建工具包'" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="formData.name" placeholder="请输入工具包名称" />
        </el-form-item>
        <el-form-item label="适用场景">
          <el-input v-model="formData.scenario" placeholder="如：水电维修、木工安装等" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" width="700px">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-lg font-semibold">{{ currentKitDetail?.kit?.name }} - 工具清单</span>
          <div class="flex items-center gap-2">
            <el-tag v-if="currentKitDetail?.missingItems && currentKitDetail.missingItems > 0" type="danger" effect="light">
              缺失 {{ currentKitDetail.missingItems }} 件
            </el-tag>
            <el-tag v-else type="success" effect="light">
              工具完整
            </el-tag>
          </div>
        </div>
      </template>

      <div v-if="currentKitDetail">
        <p v-if="currentKitDetail.kit.description" class="text-sm text-gray-500 mb-4">{{ currentKitDetail.kit.description }}</p>
        <p v-if="currentKitDetail.kit.scenario" class="text-xs text-gray-400 mb-4">适用场景：{{ currentKitDetail.kit.scenario }}</p>

        <div class="flex items-center justify-between mb-4">
          <div class="text-sm text-gray-600">
            共 {{ currentKitDetail.totalItems }} 件工具，
            <span class="text-green-600 font-medium">{{ currentKitDetail.availableItems }} 件可用</span>，
            <span class="text-red-600 font-medium">{{ currentKitDetail.missingItems }} 件缺失</span>
          </div>
          <el-button type="primary" size="small" @click="openAddItemDialog">
            <Plus :size="14" class="mr-1" /> 添加工具
          </el-button>
        </div>

        <div v-if="currentKitDetail.items.length" class="space-y-2">
          <div
            v-for="detail in currentKitDetail.items"
            :key="detail.item.toolId"
            class="flex items-center justify-between p-3 rounded-lg border"
            :class="detail.available ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'"
          >
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center"
                :class="detail.available ? 'bg-green-100' : 'bg-red-100'"
              >
                <Check :size="18" :class="detail.available ? 'text-green-600' : 'text-red-600'" />
              </div>
              <div>
                <p class="font-medium text-gray-800">{{ detail.tool?.name || '未知工具' }}</p>
                <p class="text-xs text-gray-500">
                  {{ detail.tool?.model || '-' }} · {{ detail.tool?.brand || '-' }} · {{ normalizeLocationForDisplay(detail.tool?.location) }}
                </p>
                <p v-if="!detail.available" class="text-xs text-red-600 mt-0.5">
                  <AlertTriangle :size="12" class="inline mr-1" />
                  {{ detail.missingReason }}
                </p>
                <p v-if="detail.item.remarks" class="text-xs text-gray-400 mt-0.5">备注：{{ detail.item.remarks }}</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">x{{ detail.item.quantity || 1 }}</span>
              <StatusBadge v-if="detail.tool" :status="detail.tool.status" />
              <el-button text size="small" type="danger" @click="handleRemoveItem(detail.item.toolId)">移除</el-button>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-12">
          <Wrench :size="36" class="mx-auto text-gray-300 mb-3" />
          <p class="text-gray-400">暂无工具，点击上方按钮添加</p>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="addItemVisible" title="添加工具" width="600px">
      <div class="mb-4">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索工具名称/型号"
          clearable
        >
          <template #prefix>
            <Search :size="16" />
          </template>
        </el-input>
      </div>
      <div class="max-h-96 overflow-auto space-y-2">
        <div
          v-for="tool in filteredTools"
          :key="tool.id"
          class="flex items-center justify-between p-3 rounded-lg border hover:border-primary cursor-pointer"
          :class="isToolInKit(tool.id) ? 'bg-gray-50 opacity-60' : ''"
          @click="!isToolInKit(tool.id) && selectTool(tool)"
        >
          <div>
            <p class="font-medium text-gray-800">{{ tool.name }}</p>
            <p class="text-xs text-gray-500">{{ tool.model || '-' }} · {{ tool.brand || '-' }}</p>
          </div>
          <div class="flex items-center gap-2">
            <StatusBadge :status="tool.status" />
            <el-tag v-if="isToolInKit(tool.id)" type="info" size="small">已添加</el-tag>
          </div>
        </div>
        <div v-if="!filteredTools.length" class="text-center py-8 text-gray-400">
          未找到匹配的工具
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Briefcase, Check, AlertTriangle, Wrench, Search } from 'lucide-vue-next'
import { useToolKitStore } from '@/stores/toolKit'
import { useToolStore } from '@/stores/tool'
import StatusBadge from '@/components/StatusBadge.vue'
import type { ToolKit, ToolKitDTO, ToolKitWithItems, Tool } from '@/types'
import { normalizeLocationForDisplay } from '@/utils/location'

const toolKitStore = useToolKitStore()
const toolStore = useToolStore()

const dialogVisible = ref(false)
const detailVisible = ref(false)
const addItemVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const currentKitDetail = ref<ToolKitWithItems | null>(null)
const kitDetailMap = reactive<Record<number, ToolKitWithItems>>({})
const searchKeyword = ref('')

const formData = reactive<ToolKitDTO>({
  name: '',
  description: '',
  scenario: '',
  sortOrder: 0
})

const allTools = computed<Tool[]>(() => toolStore.tools)
const filteredTools = computed(() => {
  if (!searchKeyword.value) return allTools.value
  const kw = searchKeyword.value.toLowerCase()
  return allTools.value.filter(t =>
    t.name.toLowerCase().includes(kw) ||
    (t.model && t.model.toLowerCase().includes(kw)) ||
    (t.brand && t.brand.toLowerCase().includes(kw))
  )
})

function isToolInKit(toolId: number): boolean {
  if (!currentKitDetail.value) return false
  return currentKitDetail.value.items.some(item => item.item.toolId === toolId)
}

async function loadAllData() {
  await toolKitStore.fetchToolKits()
  await toolStore.fetchTools({ page: 1, size: 1000 })
  for (const kit of toolKitStore.toolKits) {
    toolKitStore.fetchKitWithItems(kit.id).then(() => {
      kitDetailMap[kit.id] = toolKitStore.currentKit!
    })
  }
}

function openCreateDialog() {
  isEditing.value = false
  editingId.value = null
  formData.name = ''
  formData.description = ''
  formData.scenario = ''
  formData.sortOrder = 0
  dialogVisible.value = true
}

function openEditDialog(kit: ToolKit) {
  isEditing.value = true
  editingId.value = kit.id
  formData.name = kit.name
  formData.description = kit.description || ''
  formData.scenario = kit.scenario || ''
  formData.sortOrder = kit.sortOrder || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  try {
    if (isEditing.value && editingId.value) {
      await toolKitStore.updateKit(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await toolKitStore.createKit(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadAllData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除此工具包吗？', '提示', { type: 'warning' })
  await toolKitStore.deleteKit(id)
  ElMessage.success('删除成功')
  delete kitDetailMap[id]
  await loadAllData()
}

async function viewKit(id: number) {
  await toolKitStore.fetchKitWithItems(id)
  currentKitDetail.value = toolKitStore.currentKit
  kitDetailMap[id] = toolKitStore.currentKit!
  detailVisible.value = true
}

function openAddItemDialog() {
  searchKeyword.value = ''
  addItemVisible.value = true
}

async function selectTool(tool: Tool) {
  if (!currentKitDetail.value) return
  try {
    await toolKitStore.addItem(currentKitDetail.value.kit.id, { toolId: tool.id })
    ElMessage.success('添加成功')
    addItemVisible.value = false
    await viewKit(currentKitDetail.value.kit.id)
  } catch (e: any) {
    ElMessage.error(e.message || '添加失败')
  }
}

async function handleRemoveItem(toolId: number) {
  if (!currentKitDetail.value) return
  await ElMessageBox.confirm('确定要移除此工具吗？', '提示', { type: 'warning' })
  await toolKitStore.removeItem(currentKitDetail.value.kit.id, toolId)
  ElMessage.success('移除成功')
  await viewKit(currentKitDetail.value.kit.id)
}

onMounted(() => {
  loadAllData()
})
</script>
