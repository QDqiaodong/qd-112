<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <h2 class="text-2xl font-bold text-gray-800">保养管理</h2>
        <el-badge v-if="dueCount > 0" :value="dueCount" type="warning" />
      </div>
      <el-button type="primary" @click="showDialog = true">
        <Plus :size="16" class="mr-1" /> 新增保养
      </el-button>
    </div>

    <div class="flex items-center gap-4 mb-6">
      <el-radio-group v-model="viewMode" @change="handleViewChange">
        <el-radio-button value="list">列表视图</el-radio-button>
        <el-radio-button value="due">到期提醒</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="viewMode === 'list'">
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <el-table :data="maintenanceStore.maintenanceRecords" stripe>
          <el-table-column prop="maintenanceDate" label="日期" width="120" />
          <el-table-column prop="toolId" label="工具ID" width="100" />
          <el-table-column prop="maintenanceType" label="保养类型" width="100">
            <template #default="{ row }">
              {{ typeMap[row.maintenanceType] || row.maintenanceType }}
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="cost" label="费用" width="100">
            <template #default="{ row }">¥{{ row.cost }}</template>
          </el-table-column>
          <el-table-column prop="description" label="内容" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button text size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div v-else>
      <div v-if="maintenanceStore.dueMaintenance.length" class="grid grid-cols-3 gap-6">
        <div
          v-for="item in maintenanceStore.dueMaintenance"
          :key="item.id"
          class="bg-white rounded-lg p-5 shadow-sm card-hover border-l-4"
          :class="isOverdue(item.nextMaintenanceDate) ? 'border-accent' : 'border-primary'"
        >
          <div class="flex items-center justify-between mb-2">
            <h4 class="font-semibold text-gray-800">工具ID: {{ item.toolId }}</h4>
            <span
              class="text-xs px-2 py-0.5 rounded"
              :class="isOverdue(item.nextMaintenanceDate) ? 'bg-orange-100 text-accent' : 'bg-blue-100 text-primary'"
            >
              {{ isOverdue(item.nextMaintenanceDate) ? '已逾期' : '即将到期' }}
            </span>
          </div>
          <p class="text-sm text-gray-500">下次保养：{{ item.nextMaintenanceDate }}</p>
          <p class="text-sm text-gray-500 mt-1">类型：{{ typeMap[item.maintenanceType] || item.maintenanceType }}</p>
        </div>
      </div>
      <div v-else class="text-center py-20 text-gray-400">暂无到期保养提醒</div>
    </div>

    <el-dialog v-model="showDialog" title="新增保养记录" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工具" prop="toolId">
          <el-input-number v-model="form.toolId" :min="1" placeholder="工具ID" class="w-full" />
        </el-form-item>
        <el-form-item label="保养类型" prop="maintenanceType">
          <el-select v-model="form.maintenanceType" placeholder="请选择保养类型" class="w-full">
            <el-option v-for="(label, key) in typeMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="费用" prop="cost">
          <el-input-number v-model="form.cost" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="保养日期" prop="maintenanceDate">
          <el-date-picker v-model="form.maintenanceDate" type="date" value-format="YYYY-MM-DD" placeholder="选择保养日期" class="w-full" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="保养内容" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { useMaintenanceStore } from '@/stores/maintenance'
import { usePagination } from '@/composables/usePagination'
import type { MaintenanceType } from '@/types'

const maintenanceStore = useMaintenanceStore()
const pagination = usePagination()
const showDialog = ref(false)
const formRef = ref<FormInstance>()
const viewMode = ref('list')

const typeMap: Record<string, string> = {
  CLEAN: '清洁',
  OIL: '润滑',
  TIGHTEN: '紧固',
  INSPECT: '检查',
  REPAIR: '修理',
  OTHER: '其他'
}

const dueCount = computed(() => maintenanceStore.dueMaintenance.length)

const form = reactive({
  toolId: undefined as number | undefined,
  maintenanceType: '' as MaintenanceType | '',
  operator: '',
  cost: 0,
  maintenanceDate: '',
  description: ''
})

const rules: FormRules = {
  toolId: [{ required: true, message: '请输入工具ID', trigger: 'blur' }],
  maintenanceType: [{ required: true, message: '请选择保养类型', trigger: 'change' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
  maintenanceDate: [{ required: true, message: '请选择保养日期', trigger: 'change' }]
}

onMounted(() => {
  loadData()
  maintenanceStore.fetchDue()
})

function loadData() {
  maintenanceStore.fetchMaintenance({ page: pagination.page.value, size: pagination.size.value })
}

function handleViewChange() {
  if (viewMode.value === 'due') {
    maintenanceStore.fetchDue()
  }
}

function isOverdue(dateStr: string): boolean {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

async function handleCreate() {
  await formRef.value?.validate()
  await maintenanceStore.createMaintenance(form as any)
  ElMessage.success('创建成功')
  showDialog.value = false
  Object.assign(form, { toolId: undefined, maintenanceType: '', operator: '', cost: 0, maintenanceDate: '', description: '' })
  loadData()
  maintenanceStore.fetchDue()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除此记录？', '提示', { type: 'warning' })
  await maintenanceStore.deleteMaintenance(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
