<template>
  <div class="flex gap-2">
    <el-select
      :model-value="categoryId"
      placeholder="一级分类"
      clearable
      :class="showStats ? 'w-56' : 'w-40'"
      @update:model-value="handleCategoryChange"
    >
      <el-option
        v-for="cat in categories"
        :key="cat.id"
        :label="cat.name"
        :value="cat.id"
      >
        <div class="flex items-center justify-between w-full">
          <span>{{ cat.name }}</span>
          <div v-if="showStats" class="flex items-center gap-2 ml-3 shrink-0">
            <el-tag size="small" type="info" effect="plain" class="!text-xs !px-1.5 !py-0">
              {{ cat.toolCount }}件
            </el-tag>
            <el-tag v-if="cat.defaultCycleDays" size="small" type="warning" effect="plain" class="!text-xs !px-1.5 !py-0">
              {{ cat.defaultCycleDays }}天
            </el-tag>
          </div>
        </div>
      </el-option>
    </el-select>
    <el-select
      :model-value="subCategoryId"
      placeholder="二级分类"
      clearable
      :class="showStats ? 'w-56' : 'w-40'"
      :disabled="!categoryId"
      @update:model-value="handleSubCategoryChange"
    >
      <el-option
        v-for="sub in subCategories"
        :key="sub.id"
        :label="sub.name"
        :value="sub.id"
      >
        <div class="flex items-center justify-between w-full">
          <span>{{ sub.name }}</span>
          <div v-if="showStats" class="flex items-center gap-2 ml-3 shrink-0">
            <el-tag size="small" type="info" effect="plain" class="!text-xs !px-1.5 !py-0">
              {{ sub.toolCount }}件
            </el-tag>
            <el-tag v-if="sub.defaultCycleDays" size="small" type="warning" effect="plain" class="!text-xs !px-1.5 !py-0">
              {{ sub.defaultCycleDays }}天
            </el-tag>
          </div>
        </div>
      </el-option>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useCategoryStore } from '@/stores/category'
import type { CategoryTreeNode } from '@/types'

const props = withDefaults(defineProps<{
  modelValue: { categoryId: number | undefined; subCategoryId: number | undefined }
  showStats?: boolean
}>(), {
  showStats: true
})

const emit = defineEmits<{
  'update:modelValue': [value: { categoryId: number | undefined; subCategoryId: number | undefined }]
}>()

const categoryStore = useCategoryStore()

const categories = computed<CategoryTreeNode[]>(() => categoryStore.categoryTreeWithStats)
const categoryId = computed(() => props.modelValue?.categoryId)
const subCategoryId = computed(() => props.modelValue?.subCategoryId)

const subCategories = computed(() => {
  const cat = categories.value.find((c) => c.id === categoryId.value)
  return cat?.children || []
})

onMounted(() => {
  if (!categoryStore.categoryTreeWithStats.length) {
    categoryStore.fetchCategoryTreeWithStats()
  }
})

function handleCategoryChange(val: number | undefined) {
  emit('update:modelValue', { categoryId: val, subCategoryId: undefined })
}

function handleSubCategoryChange(val: number | undefined) {
  emit('update:modelValue', { categoryId: categoryId.value, subCategoryId: val })
}
</script>
