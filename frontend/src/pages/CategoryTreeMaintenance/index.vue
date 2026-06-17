<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">分类树维护</h2>
      <el-button type="primary" :icon="Plus" @click="handleAddRoot">
        新增大分类
      </el-button>
    </div>

    <div class="bg-white rounded-lg shadow-sm">
      <div class="p-4 border-b border-gray-100">
        <div class="flex items-center gap-4 text-sm text-gray-600">
          <div class="flex items-center gap-2">
            <span class="w-3 h-3 rounded-full bg-blue-500"></span>
            <span>工具数量</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="w-3 h-3 rounded-full bg-green-500"></span>
            <span>保养项覆盖率</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="w-3 h-3 rounded-full bg-orange-500"></span>
            <span>排序位置</span>
          </div>
        </div>
      </div>

      <el-tree
        :data="categoryStore.categoryTreeWithStats"
        :props="{ children: 'children', label: 'name' }"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        class="category-tree"
      >
        <template #default="{ data }">
          <div class="flex items-center justify-between w-full py-1 pr-4">
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <span class="font-medium text-gray-800 truncate">{{ data.name }}</span>
              <span class="text-xs text-gray-400 shrink-0">{{ data.code }}</span>
            </div>
            <div class="flex items-center gap-4 shrink-0">
              <div class="flex items-center gap-1 text-xs">
                <span class="w-2 h-2 rounded-full bg-blue-500"></span>
                <span class="text-gray-600">{{ data.toolCount }} 件</span>
              </div>
              <div class="flex items-center gap-1 text-xs">
                <span class="w-2 h-2 rounded-full bg-green-500"></span>
                <span class="text-gray-600">{{ data.maintenanceItemCount }}/{{ data.maintenanceItemTotal }}</span>
                <el-progress
                  :percentage="Math.round(data.maintenanceCoverageRate)"
                  :stroke-width="4"
                  :show-text="false"
                  class="w-16"
                />
              </div>
              <div class="flex items-center gap-1 text-xs">
                <span class="w-2 h-2 rounded-full bg-orange-500"></span>
                <span class="text-gray-600">排序: {{ data.sortOrder ?? 0 }}</span>
              </div>
              <div class="flex items-center gap-1">
                <el-button
                  v-if="data.level < 2"
                  type="primary"
                  link
                  size="small"
                  :icon="Plus"
                  @click.stop="handleAddChild(data)"
                >
                  子分类
                </el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="Edit"
                  @click.stop="handleEdit(data)"
                >
                  编辑
                </el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="Sort"
                  @click.stop="handleSort(data)"
                >
                  排序
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  :icon="Delete"
                  @click.stop="handleDelete(data)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </template>
      </el-tree>

      <div v-if="!categoryStore.categoryTreeWithStats.length" class="text-center py-16 text-gray-400">
        暂无分类数据，点击上方按钮新增大分类
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : (isAddChild ? '新增子分类' : '新增大分类')"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item v-if="parentCategory" label="上级分类">
          <el-tag type="info">{{ parentCategory.name }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sortDialogVisible" title="调整排序" width="400px">
      <div class="space-y-4">
        <p class="text-sm text-gray-600">
          当前分类：<span class="font-medium">{{ currentSortNode?.name }}</span>
        </p>
        <el-form label-width="80px">
          <el-form-item label="排序值">
            <el-input-number v-model="newSortOrder" :min="0" :max="999" />
          </el-form-item>
        </el-form>
        <p class="text-xs text-gray-500">
          排序值越小越靠前，相同排序值按创建时间排序。
        </p>
      </div>
      <template #footer>
        <el-button @click="sortDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSortSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deleteDialogVisible" title="删除确认" width="500px">
      <div v-if="deletionCheckResult">
        <div v-if="!deletionCheckResult.canDelete" class="space-y-4">
          <el-alert :title="deletionCheckResult.message" type="warning" show-icon />
          <div class="bg-gray-50 rounded-lg p-4 space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-600">子分类数量：</span>
              <span class="font-medium text-orange-600">{{ deletionCheckResult.subCategoryCount }} 个</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">关联工具数量：</span>
              <span class="font-medium text-orange-600">{{ deletionCheckResult.toolCount }} 个</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">保养记录数量：</span>
              <span class="font-medium text-orange-600">{{ deletionCheckResult.maintenanceRecordCount }} 条</span>
            </div>
          </div>
          <p class="text-sm text-gray-500">
            请先解除上述关联后再尝试删除该分类。
          </p>
        </div>
        <div v-else class="space-y-4">
          <el-alert title="该分类可以安全删除" type="success" show-icon />
          <p class="text-gray-600">
            确定要删除分类 <strong class="text-red-600">{{ currentDeleteNode?.name }}</strong> 吗？
          </p>
          <p class="text-sm text-gray-500">此操作不可恢复，请谨慎操作。</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button
          v-if="deletionCheckResult?.canDelete"
          type="danger"
          :loading="deleting"
          @click="confirmDelete"
        >
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCategoryStore } from '@/stores/category'
import { Plus, Edit, Delete, Sort } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { CategoryTreeNode, CategoryDeletionCheck, Category } from '@/types'

const categoryStore = useCategoryStore()
const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const sortDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const isEdit = ref(false)
const isAddChild = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const parentCategory = ref<CategoryTreeNode | null>(null)
const currentSortNode = ref<CategoryTreeNode | null>(null)
const currentDeleteNode = ref<CategoryTreeNode | null>(null)
const newSortOrder = ref(0)
const deletionCheckResult = ref<CategoryDeletionCheck | null>(null)

const formData = ref<Partial<Category>>({
  name: '',
  code: '',
  sortOrder: 0,
  description: '',
  parentId: undefined
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
}

onMounted(() => {
  categoryStore.fetchCategoryTreeWithStats()
})

function handleAddRoot() {
  isEdit.value = false
  isAddChild.value = false
  parentCategory.value = null
  formData.value = {
    name: '',
    code: '',
    sortOrder: 0,
    description: '',
    parentId: undefined
  }
  dialogVisible.value = true
}

function handleAddChild(node: CategoryTreeNode) {
  isEdit.value = false
  isAddChild.value = true
  parentCategory.value = node
  formData.value = {
    name: '',
    code: '',
    sortOrder: 0,
    description: '',
    parentId: node.id
  }
  dialogVisible.value = true
}

function handleEdit(node: CategoryTreeNode) {
  isEdit.value = true
  isAddChild.value = false
  parentCategory.value = null
  formData.value = {
    id: node.id,
    name: node.name,
    code: node.code,
    sortOrder: node.sortOrder,
    description: node.description,
    parentId: node.parentId
  }
  dialogVisible.value = true
}

function handleSort(node: CategoryTreeNode) {
  currentSortNode.value = node
  newSortOrder.value = node.sortOrder ?? 0
  sortDialogVisible.value = true
}

async function handleSortSubmit() {
  if (!currentSortNode.value) return
  const success = await categoryStore.updateSortOrder(currentSortNode.value.id, newSortOrder.value)
  if (success) {
    sortDialogVisible.value = false
    ElMessage.success('排序更新成功')
  }
}

async function handleDelete(node: CategoryTreeNode) {
  currentDeleteNode.value = node
  deletionCheckResult.value = null
  deleteDialogVisible.value = true
  try {
    deletionCheckResult.value = await categoryStore.checkDeletion(node.id)
  } catch (error) {
    console.error('检查删除状态失败', error)
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value && formData.value.id) {
        await categoryStore.updateCategory(formData.value.id, formData.value)
      } else {
        await categoryStore.createCategory(formData.value)
      }
      dialogVisible.value = false
    } finally {
      submitting.value = false
    }
  })
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

async function confirmDelete() {
  if (!currentDeleteNode.value || !deletionCheckResult.value?.canDelete) return
  try {
    await ElMessageBox.confirm(
      `确定要删除分类「${currentDeleteNode.value.name}」吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  deleting.value = true
  try {
    const success = await categoryStore.deleteCategory(currentDeleteNode.value.id)
    if (success) {
      deleteDialogVisible.value = false
      currentDeleteNode.value = null
    }
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.category-tree {
  :deep(.el-tree-node__content) {
    height: auto;
    padding: 8px 0;
  }

  :deep(.el-tree-node__label) {
    flex: 1;
  }
}
</style>
