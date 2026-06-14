<template>
  <div class="flex gap-2">
    <el-select
      :model-value="categoryId"
      placeholder="一级分类"
      clearable
      class="w-40"
      @update:model-value="handleCategoryChange"
    >
      <el-option
        v-for="cat in categories"
        :key="cat.id"
        :label="cat.name"
        :value="cat.id"
      />
    </el-select>
    <el-select
      :model-value="subCategoryId"
      placeholder="二级分类"
      clearable
      class="w-40"
      :disabled="!categoryId"
      @update:model-value="handleSubCategoryChange"
    >
      <el-option
        v-for="sub in subCategories"
        :key="sub.id"
        :label="sub.name"
        :value="sub.id"
      />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useCategoryStore } from '@/stores/category'

const props = defineProps<{
  modelValue: { categoryId: number | undefined; subCategoryId: number | undefined }
}>()

const emit = defineEmits<{
  'update:modelValue': [value: { categoryId: number | undefined; subCategoryId: number | undefined }]
}>()

const categoryStore = useCategoryStore()

const categories = computed(() => categoryStore.categoryTree)
const categoryId = computed(() => props.modelValue?.categoryId)
const subCategoryId = computed(() => props.modelValue?.subCategoryId)

const subCategories = computed(() => {
  const cat = categories.value.find((c) => c.id === categoryId.value)
  return cat?.children || []
})

function handleCategoryChange(val: number | undefined) {
  emit('update:modelValue', { categoryId: val, subCategoryId: undefined })
}

function handleSubCategoryChange(val: number | undefined) {
  emit('update:modelValue', { categoryId: categoryId.value, subCategoryId: val })
}
</script>
