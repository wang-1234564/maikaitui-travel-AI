import { ref } from 'vue'

// 全局 AI 悬浮窗状态（轻量级，不依赖 Pinia）
const panelOpen = ref(false)
const context = ref(null) // { attractionId: 5, name: '黄山' }

export function useAiChatStore() {
  function open() { panelOpen.value = true }
  function close() { panelOpen.value = false }
  function toggle() { panelOpen.value = !panelOpen.value }

  function setContext(ctx) { context.value = ctx }
  function clearContext() { context.value = null }

  return { panelOpen, context, open, close, toggle, setContext, clearContext }
}
