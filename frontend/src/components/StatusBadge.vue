<template>
  <span
    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
    :class="badgeClass"
  >
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ToolStatus } from '@/types'

const props = defineProps<{
  status: ToolStatus | string
}>()

const statusMap: Record<string, { label: string; class: string }> = {
  AVAILABLE: { label: '可用', class: 'bg-green-100 text-green-800' },
  IN_USE: { label: '使用中', class: 'bg-blue-100 text-blue-800' },
  MAINTENANCE: { label: '保养中', class: 'bg-orange-100 text-orange-800' },
  LOANED: { label: '借出', class: 'bg-purple-100 text-purple-800' },
  LOST: { label: '丢失', class: 'bg-red-100 text-red-800' }
}

const badgeClass = computed(() => statusMap[props.status]?.class || 'bg-gray-100 text-gray-800')
const label = computed(() => statusMap[props.status]?.label || props.status)
</script>
