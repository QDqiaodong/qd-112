<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">使用记录</h2>
      <el-button type="primary" @click="showDialog = true">
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

    <el-dialog v-model="showDialog" title="新增使用记录" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工具" prop="toolId">
          <el-input-number v-model="form.toolId" :min="1" placeholder="工具ID" class="w-full" />
        </el-form-item>
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
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { useUsageStore } from '@/stores/usage'
import { usePagination } from '@/composables/usePagination'

const usageStore = useUsageStore()
const pagination = usePagination()
const showDialog = ref(false)
const formRef = ref<FormInstance>()
const dateRange = ref<string[]>([])
const toolKeyword = ref('')

const form = reactive({
  toolId: undefined as number | undefined,
  scenario: '',
  useDate: '',
  durationMinutes: undefined as number | undefined,
  operator: '',
  remarks: ''
})

const rules: FormRules = {
  toolId: [{ required: true, message: '请输入工具ID', trigger: 'blur' }],
  scenario: [{ required: true, message: '请输入使用场景', trigger: 'blur' }],
  useDate: [{ required: true, message: '请选择使用日期', trigger: 'change' }],
  durationMinutes: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

onMounted(() => loadData())

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

async function handleCreate() {
  await formRef.value?.validate()
  await usageStore.createUsage(form)
  ElMessage.success('创建成功')
  showDialog.value = false
  Object.assign(form, { toolId: undefined, scenario: '', useDate: '', durationMinutes: undefined, operator: '', remarks: '' })
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除此记录？', '提示', { type: 'warning' })
  await usageStore.deleteUsage(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
