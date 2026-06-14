<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">工具管理</h2>
      <el-button type="primary" @click="router.push('/tools/add')">
        <Plus :size="16" class="mr-1" /> 新增工具
      </el-button>
    </div>

    <div class="flex items-center gap-4 mb-6">
      <el-input
        v-model="keyword"
        placeholder="搜索工具名称/型号"
        clearable
        class="w-60"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <Search :size="16" />
        </template>
      </el-input>
      <CategoryCascade v-model="categoryFilter" />
      <el-button @click="handleSearch">搜索</el-button>
    </div>

    <div v-if="toolStore.loading" class="text-center py-20 text-gray-400">加载中...</div>

    <div v-else-if="toolStore.tools.length" class="grid grid-cols-3 gap-6">
      <div
        v-for="tool in toolStore.tools"
        :key="tool.id"
        class="bg-white rounded-lg p-5 shadow-sm card-hover"
      >
        <div class="flex items-start justify-between mb-3">
          <h3 class="text-lg font-semibold text-gray-800">{{ tool.name }}</h3>
          <StatusBadge :status="tool.status" />
        </div>
        <div class="space-y-1 text-sm text-gray-500 mb-4">
          <p>型号：{{ tool.model || '-' }}</p>
          <p>品牌：{{ tool.brand || '-' }}</p>
          <p>分类ID：{{ tool.categoryId }} / {{ tool.subCategoryId }}</p>
          <p>位置：{{ tool.location }}</p>
        </div>
        <div class="flex items-center gap-2 pt-3 border-t border-gray-100">
          <el-button text size="small" @click="router.push(`/tools/${tool.id}`)">查看</el-button>
          <el-button text size="small" type="primary" @click="router.push(`/tools/${tool.id}/edit`)">编辑</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(tool.id)">删除</el-button>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20">
      <Hammer :size="48" class="mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400">暂无工具数据</p>
    </div>

    <div v-if="toolStore.total > pagination.size.value" class="flex justify-end mt-6">
      <el-pagination
        :current-page="pagination.page.value"
        :page-size="pagination.size.value"
        :total="toolStore.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Search, Hammer } from 'lucide-vue-next'
import { useToolStore } from '@/stores/tool'
import { useCategoryStore } from '@/stores/category'
import CategoryCascade from '@/components/CategoryCascade.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()
const toolStore = useToolStore()
const categoryStore = useCategoryStore()
const pagination = usePagination()

const keyword = ref('')
const categoryFilter = reactive({ categoryId: undefined as number | undefined, subCategoryId: undefined as number | undefined })

onMounted(() => {
  categoryStore.fetchCategoryTree()
  loadData()
})

function loadData() {
  toolStore.fetchTools({
    page: pagination.page.value,
    size: pagination.size.value,
    keyword: keyword.value || undefined,
    categoryId: categoryFilter.categoryId,
    subCategoryId: categoryFilter.subCategoryId
  })
}

function handleSearch() {
  pagination.reset()
  loadData()
}

function handlePageChange(page: number) {
  pagination.handleChange(page)
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除此工具吗？', '提示', { type: 'warning' })
  await toolStore.deleteTool(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
