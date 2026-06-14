<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">使用记录</h2>
      <el-button type="primary" @click="openDialog">
        <Plus :size="16" class="mr-1" /> 新增记录
      </el-button>
    </div>

    <div class="flex items-center gap-4 mb-6">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="handleSearch"
      />
      <el-input
        v-model="toolKeyword"
        placeholder="工具ID筛选"
        clearable
        class="w-48"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-button @click="handleSearch">筛选</el-button>
    </div>

    <div class="bg-white rounded-lg p-6 shadow-sm">
      <div v-if="usageStore.loading" class="text-center py-10 text-gray-400">加载中...</div>
      <div v-else-if="usageStore.usageRecords.length" class="space-y-4">
        <div
          v-for="record in usageStore.usageRecords"
          :key="record.id"
          class="flex items-center gap-4 p-4 border-b border-gray-100 last:border-0"
        >
          <div class="w-3 h-3 rounded-full bg-blue-400 shrink-0"></div>
          <div class="flex-1">
            <div class="flex items-center gap-3">
              <span class="font-medium text-gray-800">工具ID: {{ record.toolId }}</span>
              <span class="text-sm text-gray-400">{{ record.scenario }}</span>
            </div>
            <div class="text-xs text-gray-400 mt-1">
              {{ record.useDate }} · 时长 {{ record.durationMinutes }}分钟 · 操作人：{{ record.operator }}
            </div>
          </div>
          <el-button text size="small" type="danger" @click="handleDelete(record.id)">删除</el-button>
        </div>
      </div>
      <div v-else class="text-center py-10 text-gray-400">暂无使用记录</div>
    </div>

    <el-dialog v-model="showDialog" title="新增使用记录" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工具" prop="toolId">
          <el-select
            v-model="form.toolId"
            placeholder="请选择工具"
            filterable
            class="w-full"
            @change="handleToolChange"
          >
            <el-option
              v-for="tool in toolOptions"
              :key="tool.id"
              :label="`${tool.name}（ID: ${tool.id}）`"
              :value="tool.id"
            />
          </el-select>
        </el-form-item>

        <template v-if="selectedTool">
          <el-form-item label="分类">
            <span class="text-gray-700">{{ categoryLabel }}</span>
          </el-form-item>
          <el-form-item label="位置">
            <span class="text-gray-700">{{ selectedTool.location }}</span>
          </el-form-item>
          <el-form-item label="当前状态">
            <StatusBadge :status="selectedTool.status" />
          </el-form-item>
          <el-form-item label="最近保养">
            <span class="text-gray-700">{{ selectedTool.lastMaintenanceDate || '暂无' }}</span>
          </el-form-item>

          <el-alert
            v-if="statusConflict"
            :title="statusConflict"
            type="warning"
            show-icon
            :closable="false"
            class="mb-4"
          />
        </template>

        <el-form-item label="使用场景" prop="scenario">
          <el-input v-model="form.scenario" placeholder="请输入使用场景" />
        </el-form-item>
        <el-form-item label="使用日期" prop="useDate">
          <el-date-picker v-model="form.useDate" type="date" value-format="YYYY-MM-DD" placeholder="选择使用日期" class="w-full" />
        </el-form-item>
        <el-form-item label="时长(分)" prop="durationMinutes">
          <el-input-number v-model="form.durationMinutes" :min="1" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remarks" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!!statusConflict" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { useUsageStore } from '@/stores/usage'
import { useToolStore } from '@/stores/tool'
import { useCategoryStore } from '@/stores/category'
import { usePagination } from '@/composables/usePagination'
import StatusBadge from '@/components/StatusBadge.vue'
import type { Tool } from '@/types'

const usageStore = useUsageStore()
const toolStore = useToolStore()
const categoryStore = useCategoryStore()
const pagination = usePagination()
const showDialog = ref(false)
const formRef = ref<FormInstance>()
const dateRange = ref<string[]>([])
const toolKeyword = ref('')
const toolOptions = ref<Tool[]>([])
const selectedTool = ref<Tool | null>(null)

const form = reactive({
  toolId: undefined as number | undefined,
  scenario: '',
  useDate: '',
  durationMinutes: undefined as number | undefined,
  operator: '',
  remarks: ''
})

const rules: FormRules = {
  toolId: [{ required: true, message: '请选择工具', trigger: 'change' }],
  scenario: [{ required: true, message: '请输入使用场景', trigger: 'blur' }],
  useDate: [{ required: true, message: '请选择使用日期', trigger: 'change' }],
  durationMinutes: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const categoryLabel = computed(() => {
  if (!selectedTool.value) return ''
  const tree = categoryStore.categoryTree
  const cat = tree.find((c) => c.id === selectedTool.value!.categoryId)
  if (!cat) return ''
  const sub = cat.children?.find((s) => s.id === selectedTool.value!.subCategoryId)
  return sub ? `${cat.name} / ${sub.name}` : cat.name
})

const statusConflict = computed(() => {
  if (!selectedTool.value) return ''
  const status = selectedTool.value.status
  if (status === 'MAINTENANCE') return '该工具当前处于「保养中」状态，无法录入使用记录'
  if (status === 'LOST') return '该工具当前处于「遗失」状态，无法录入使用记录'
  return ''
})

onMounted(() => {
  loadData()
  categoryStore.fetchCategoryTree()
})

function loadData() {
  const params: Record<string, any> = {
    page: pagination.page.value,
    size: pagination.size.value
  }
  if (dateRange.value?.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  if (toolKeyword.value) params.toolId = toolKeyword.value
  usageStore.fetchUsage(params)
}

function handleSearch() {
  pagination.reset()
  loadData()
}

async function openDialog() {
  toolOptions.value = await toolStore.fetchToolOptions()
  showDialog.value = true
}

function handleToolChange(toolId: number) {
  selectedTool.value = toolOptions.value.find((t) => t.id === toolId) || null
}

function resetForm() {
  Object.assign(form, { toolId: undefined, scenario: '', useDate: '', durationMinutes: undefined, operator: '', remarks: '' })
  selectedTool.value = null
}

async function handleCreate() {
  await formRef.value?.validate()
  await usageStore.createUsage(form)
  ElMessage.success('创建成功')
  showDialog.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除此记录？', '提示', { type: 'warning' })
  await usageStore.deleteUsage(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
