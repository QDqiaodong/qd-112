<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">零件更换记录</h2>
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
      <el-select
        v-model="partTypeFilter"
        placeholder="零件类型"
        clearable
        class="w-40"
        @change="handleSearch"
      >
        <el-option v-for="(label, key) in partTypeMap" :key="key" :label="label" :value="key" />
      </el-select>
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

    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <el-table :data="store.records" stripe v-loading="store.loading">
        <el-table-column prop="replacementDate" label="更换日期" width="120" />
        <el-table-column prop="toolId" label="工具ID" width="100" />
        <el-table-column prop="partName" label="零件名称" min-width="140" />
        <el-table-column prop="partType" label="零件类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="tagTypeMap[row.partType] || 'info'">
              {{ partTypeMap[row.partType] || row.partType || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cost" label="费用" width="100">
          <template #default="{ row }">
            <span class="font-medium text-gray-800">¥{{ row.cost || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="supplier" label="供应商" width="140" />
        <el-table-column prop="remarks" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="flex justify-end mt-4">
      <el-pagination
        v-model:current-page="pagination.page.value"
        v-model:page-size="pagination.size.value"
        :total="store.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
      />
    </div>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑更换记录' : '新增更换记录'" width="560px" @closed="resetForm">
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
        </template>

        <el-form-item label="零件名称" prop="partName">
          <el-input v-model="form.partName" placeholder="如：3寸锯片、12V锂电池" />
        </el-form-item>
        <el-form-item label="零件类型" prop="partType">
          <el-select v-model="form.partType" placeholder="请选择零件类型" class="w-full">
            <el-option v-for="(label, key) in partTypeMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="更换日期" prop="replacementDate">
          <el-date-picker v-model="form.replacementDate" type="date" value-format="YYYY-MM-DD" placeholder="选择更换日期" class="w-full" />
        </el-form-item>
        <el-form-item label="费用" prop="cost">
          <el-input-number v-model="form.cost" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remarks" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { usePartReplacementStore } from '@/stores/partReplacement'
import { useToolStore } from '@/stores/tool'
import { useCategoryStore } from '@/stores/category'
import { usePagination } from '@/composables/usePagination'
import StatusBadge from '@/components/StatusBadge.vue'
import type { Tool, PartReplacement } from '@/types'

const store = usePartReplacementStore()
const toolStore = useToolStore()
const categoryStore = useCategoryStore()
const pagination = usePagination()
const showDialog = ref(false)
const formRef = ref<FormInstance>()
const dateRange = ref<string[]>([])
const toolKeyword = ref('')
const partTypeFilter = ref('')
const toolOptions = ref<Tool[]>([])
const selectedTool = ref<Tool | null>(null)
const editingId = ref<number | null>(null)

const partTypeMap: Record<string, string> = {
  BLADE: '刀片',
  BATTERY: '电池',
  DRILL_BIT: '钻头',
  MOTOR: '电机',
  BEARING: '轴承',
  SEAL: '密封件',
  CABLE: '线缆',
  FILTER: '滤网',
  OTHER: '其他'
}

const tagTypeMap: Record<string, string> = {
  BLADE: 'danger',
  BATTERY: 'warning',
  DRILL_BIT: '',
  MOTOR: 'success',
  BEARING: 'info',
  SEAL: 'warning',
  CABLE: 'info',
  FILTER: 'success',
  OTHER: 'info'
}

const form = reactive({
  toolId: undefined as number | undefined,
  partName: '',
  partType: '',
  replacementDate: '',
  cost: undefined as number | undefined,
  operator: '',
  supplier: '',
  remarks: ''
})

const rules: FormRules = {
  toolId: [{ required: true, message: '请选择工具', trigger: 'change' }],
  partName: [{ required: true, message: '请输入零件名称', trigger: 'blur' }],
  replacementDate: [{ required: true, message: '请选择更换日期', trigger: 'change' }],
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
  if (partTypeFilter.value) params.partType = partTypeFilter.value
  store.fetchRecords(params)
}

function handleSearch() {
  pagination.reset()
  loadData()
}

async function openDialog() {
  toolOptions.value = await toolStore.fetchToolOptions()
  editingId.value = null
  showDialog.value = true
}

function openEditDialog(row: PartReplacement) {
  editingId.value = row.id
  Object.assign(form, {
    toolId: row.toolId,
    partName: row.partName,
    partType: row.partType || '',
    replacementDate: row.replacementDate,
    cost: row.cost,
    operator: row.operator,
    supplier: row.supplier || '',
    remarks: row.remarks || ''
  })
  toolStore.fetchToolOptions().then((tools) => {
    toolOptions.value = tools
    selectedTool.value = tools.find((t) => t.id === row.toolId) || null
  })
  showDialog.value = true
}

function handleToolChange(toolId: number) {
  selectedTool.value = toolOptions.value.find((t) => t.id === toolId) || null
}

function resetForm() {
  Object.assign(form, {
    toolId: undefined,
    partName: '',
    partType: '',
    replacementDate: '',
    cost: undefined,
    operator: '',
    supplier: '',
    remarks: ''
  })
  selectedTool.value = null
  editingId.value = null
}

async function handleSubmit() {
  await formRef.value?.validate()
  if (editingId.value) {
    await store.updateRecord(editingId.value, form)
    ElMessage.success('更新成功')
  } else {
    await store.createRecord(form)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除此记录？', '提示', { type: 'warning' })
  await store.deleteRecord(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
