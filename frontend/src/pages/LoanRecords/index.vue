<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">借出交接台账</h2>
      <el-button type="primary" @click="openLoanDialog">
        <Plus :size="16" class="mr-1" /> 新增借出
      </el-button>
    </div>

    <div class="flex items-center gap-4 mb-6 flex-wrap">
      <el-select
        v-model="statusFilter"
        placeholder="状态筛选"
        clearable
        class="w-40"
        @change="handleSearch"
      >
        <el-option label="借出中" value="BORROWED" />
        <el-option label="已归还" value="RETURNED" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="借出开始"
        end-placeholder="借出结束"
        value-format="YYYY-MM-DD"
        @change="handleSearch"
      />
      <el-input
        v-model="borrowerKeyword"
        placeholder="借用人搜索"
        clearable
        class="w-48"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-button @click="handleSearch">筛选</el-button>
    </div>

    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <el-table :data="loanStore.loanRecords" v-loading="loanStore.loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="工具" width="200">
          <template #default="{ row }">
            <div class="font-medium text-gray-800">
              {{ getToolName(row.toolId) }}
            </div>
            <div class="text-xs text-gray-400">ID: {{ row.toolId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="borrower" label="借用人" width="120" />
        <el-table-column prop="loanDate" label="借出日期" width="120" />
        <el-table-column prop="expectedReturnDate" label="预计归还" width="120">
          <template #default="{ row }">
            <span :class="{ 'text-red-500': isOverdue(row) }">{{ row.expectedReturnDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualReturnDate" label="实际归还" width="120">
          <template #default="{ row }">
            {{ row.actualReturnDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="交接状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'BORROWED' ? 'warning' : 'success'">
              {{ row.status === 'BORROWED' ? '借出中' : '已归还' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归还状态" width="100">
          <template #default="{ row }">
            <span v-if="row.returnStatus">
              <el-tag v-if="row.returnStatus === 'GOOD'" type="success" size="small">完好</el-tag>
              <el-tag v-else-if="row.returnStatus === 'DAMAGED'" type="warning" size="small">损坏</el-tag>
              <el-tag v-else-if="row.returnStatus === 'LOST'" type="danger" size="small">遗失</el-tag>
            </span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'BORROWED'"
              type="primary"
              size="small"
              @click="openReturnDialog(row)"
            >
              归还
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex items-center justify-between px-6 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ loanStore.total }} 条记录</span>
        <el-pagination
          v-model:current-page="pagination.page.value"
          v-model:page-size="pagination.size.value"
          :page-sizes="[10, 20, 50, 100]"
          :total="loanStore.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>

    <el-dialog v-model="showLoanDialog" title="新增借出" width="600px" @closed="resetLoanForm">
      <el-form ref="loanFormRef" :model="loanForm" :rules="loanRules" label-width="100px">
        <el-form-item label="工具" prop="toolId">
          <el-select
            v-model="loanForm.toolId"
            placeholder="请选择工具"
            filterable
            class="w-full"
            @change="handleToolChange"
          >
            <el-option
              v-for="tool in availableTools"
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

        <el-form-item label="借用人" prop="borrower">
          <el-input v-model="loanForm.borrower" placeholder="请输入借用人" />
        </el-form-item>
        <el-form-item label="借出日期" prop="loanDate">
          <el-date-picker v-model="loanForm.loanDate" type="date" value-format="YYYY-MM-DD" placeholder="选择借出日期" class="w-full" />
        </el-form-item>
        <el-form-item label="预计归还" prop="expectedReturnDate">
          <el-date-picker v-model="loanForm.expectedReturnDate" type="date" value-format="YYYY-MM-DD" placeholder="选择预计归还日期" class="w-full" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="loanForm.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="loanForm.remarks" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoanDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateLoan">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showReturnDialog" title="归还工具" width="500px" @closed="resetReturnForm">
      <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="100px">
        <el-form-item label="工具">
          <span class="text-gray-700 font-medium">{{ currentRecord ? getToolName(currentRecord.toolId) : '-' }}</span>
        </el-form-item>
        <el-form-item label="借用人">
          <span class="text-gray-700">{{ currentRecord?.borrower }}</span>
        </el-form-item>
        <el-form-item label="借出日期">
          <span class="text-gray-700">{{ currentRecord?.loanDate }}</span>
        </el-form-item>
        <el-form-item label="归还日期" prop="actualReturnDate">
          <el-date-picker v-model="returnForm.actualReturnDate" type="date" value-format="YYYY-MM-DD" placeholder="选择归还日期" class="w-full" />
        </el-form-item>
        <el-form-item label="归还状态" prop="returnStatus">
          <el-radio-group v-model="returnForm.returnStatus">
            <el-radio value="GOOD">完好</el-radio>
            <el-radio value="DAMAGED">损坏</el-radio>
            <el-radio value="LOST">遗失</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="returnForm.remarks" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReturnDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReturn">确认归还</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { useLoanStore } from '@/stores/loan'
import { useToolStore } from '@/stores/tool'
import { useCategoryStore } from '@/stores/category'
import { usePagination } from '@/composables/usePagination'
import StatusBadge from '@/components/StatusBadge.vue'
import type { Tool, LoanRecord } from '@/types'

const loanStore = useLoanStore()
const toolStore = useToolStore()
const categoryStore = useCategoryStore()
const pagination = usePagination()

const statusFilter = ref<string>('')
const dateRange = ref<string[]>([])
const borrowerKeyword = ref('')

const showLoanDialog = ref(false)
const showReturnDialog = ref(false)
const loanFormRef = ref<FormInstance>()
const returnFormRef = ref<FormInstance>()

const availableTools = ref<Tool[]>([])
const selectedTool = ref<Tool | null>(null)
const currentRecord = ref<LoanRecord | null>(null)

const loanForm = reactive({
  toolId: undefined as number | undefined,
  borrower: '',
  loanDate: '',
  expectedReturnDate: '',
  operator: '',
  remarks: ''
})

const returnForm = reactive({
  actualReturnDate: '',
  returnStatus: 'GOOD' as const,
  remarks: ''
})

const loanRules: FormRules = {
  toolId: [{ required: true, message: '请选择工具', trigger: 'change' }],
  borrower: [{ required: true, message: '请输入借用人', trigger: 'blur' }],
  loanDate: [{ required: true, message: '请选择借出日期', trigger: 'change' }],
  expectedReturnDate: [{ required: true, message: '请选择预计归还日期', trigger: 'change' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const returnRules: FormRules = {
  actualReturnDate: [{ required: true, message: '请选择归还日期', trigger: 'change' }],
  returnStatus: [{ required: true, message: '请选择归还状态', trigger: 'change' }]
}

const categoryLabel = computed(() => {
  if (!selectedTool.value) return ''
  const tree = categoryStore.categoryTree
  const cat = tree.find((c) => c.id === selectedTool.value!.categoryId)
  if (!cat) return ''
  const sub = cat.children?.find((s) => s.id === selectedTool.value!.subCategoryId)
  return sub ? `${cat.name} / ${sub.name}` : cat.name
})

function getToolName(toolId: number): string {
  const tool = toolStore.tools.find((t) => t.id === toolId)
  return tool ? tool.name : `工具#${toolId}`
}

function isOverdue(record: LoanRecord): boolean {
  if (record.status !== 'BORROWED') return false
  if (!record.expectedReturnDate) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const expected = new Date(record.expectedReturnDate)
  return today > expected
}

onMounted(() => {
  loadData()
  categoryStore.fetchCategoryTree()
  toolStore.fetchAllTools()
})

function loadData() {
  const params: Record<string, any> = {
    page: pagination.page.value,
    size: pagination.size.value
  }
  if (statusFilter.value) {
    params.status = statusFilter.value
  }
  if (dateRange.value?.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  loanStore.fetchLoanRecords(params)
}

function handleSearch() {
  pagination.reset()
  loadData()
}

async function openLoanDialog() {
  try {
    await toolStore.fetchAllTools()
    availableTools.value = toolStore.tools.filter(
      (t) => t.status === 'AVAILABLE' || t.status === 'IN_USE'
    )
  } catch (e) {
    availableTools.value = []
  }
  showLoanDialog.value = true
}

function handleToolChange(toolId: number) {
  selectedTool.value = availableTools.value.find((t) => t.id === toolId) || null
}

function resetLoanForm() {
  Object.assign(loanForm, {
    toolId: undefined,
    borrower: '',
    loanDate: '',
    expectedReturnDate: '',
    operator: '',
    remarks: ''
  })
  selectedTool.value = null
}

async function handleCreateLoan() {
  await loanFormRef.value?.validate()
  try {
    await loanStore.createLoanRecord(loanForm)
    ElMessage.success('借出记录创建成功')
    showLoanDialog.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  }
}

function openReturnDialog(record: LoanRecord) {
  currentRecord.value = record
  returnForm.actualReturnDate = new Date().toISOString().split('T')[0]
  returnForm.returnStatus = 'GOOD'
  returnForm.remarks = ''
  showReturnDialog.value = true
}

function resetReturnForm() {
  currentRecord.value = null
}

async function handleReturn() {
  await returnFormRef.value?.validate()
  if (!currentRecord.value) return
  try {
    await loanStore.returnLoanRecord(currentRecord.value.id, returnForm)
    ElMessage.success('归还成功')
    showReturnDialog.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '归还失败')
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除此借出记录？删除后工具状态将恢复为可用。', '提示', {
    type: 'warning'
  })
  try {
    await loanStore.deleteLoanRecord(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}
</script>
