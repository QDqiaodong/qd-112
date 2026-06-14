<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">{{ isEdit ? '编辑工具' : '新增工具' }}</h2>

    <div class="bg-white rounded-lg p-6 shadow-sm max-w-2xl">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工具名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入工具名称" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="form.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <CategoryCascade v-model="categoryValue" />
        </el-form-item>
        <el-form-item label="用途" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="请输入用途" />
        </el-form-item>
        <el-form-item label="规格参数" prop="specification">
          <el-input v-model="form.specification" type="textarea" :rows="2" placeholder="请输入规格参数" />
        </el-form-item>
        <el-form-item label="存放位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入存放位置" />
        </el-form-item>
        <el-form-item label="购入日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择购入日期" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="保养周期" prop="maintenanceCycleDays">
          <el-input-number v-model="form.maintenanceCycleDays" :min="0" />
          <span class="ml-2 text-sm text-gray-400">天</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useToolStore } from '@/stores/tool'
import { useCategoryStore } from '@/stores/category'
import CategoryCascade from '@/components/CategoryCascade.vue'

const route = useRoute()
const router = useRouter()
const toolStore = useToolStore()
const categoryStore = useCategoryStore()

const isEdit = computed(() => !!route.params.id)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  model: '',
  brand: '',
  categoryId: undefined as number | undefined,
  subCategoryId: undefined as number | undefined,
  purpose: '',
  specification: '',
  location: '',
  purchaseDate: '',
  price: undefined as number | undefined,
  maintenanceCycleDays: undefined as number | undefined
})

const categoryValue = computed({
  get: () => ({ categoryId: form.categoryId, subCategoryId: form.subCategoryId }),
  set: (val) => {
    form.categoryId = val.categoryId
    form.subCategoryId = val.subCategoryId
  }
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入工具名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入存放位置', trigger: 'blur' }]
}

onMounted(async () => {
  await categoryStore.fetchCategoryTree()
  if (isEdit.value) {
    await toolStore.fetchTool(Number(route.params.id))
    const tool = toolStore.currentTool
    if (tool) {
      Object.assign(form, {
        name: tool.name,
        model: tool.model,
        brand: tool.brand,
        categoryId: tool.categoryId,
        subCategoryId: tool.subCategoryId,
        purpose: tool.purpose,
        specification: tool.specification,
        location: tool.location,
        purchaseDate: tool.purchaseDate,
        price: tool.price,
        maintenanceCycleDays: tool.maintenanceCycleDays
      })
    }
  }
})

async function handleSubmit() {
  await formRef.value?.validate()
  if (isEdit.value) {
    await toolStore.updateTool(Number(route.params.id), form)
    ElMessage.success('更新成功')
  } else {
    await toolStore.createTool(form)
    ElMessage.success('创建成功')
  }
  router.push('/tools')
}
</script>
