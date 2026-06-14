import { ref } from 'vue'

export function usePagination(defaultSize = 10) {
  const page = ref(1)
  const size = ref(defaultSize)
  const total = ref(0)

  function handleChange(p: number, s?: number) {
    page.value = p
    if (s !== undefined) size.value = s
  }

  function reset() {
    page.value = 1
  }

  return { page, size, total, handleChange, reset }
}
