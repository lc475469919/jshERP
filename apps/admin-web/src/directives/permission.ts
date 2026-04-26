import type { App, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

function mounted(element: HTMLElement, binding: DirectiveBinding<string>) {
  const auth = useAuthStore()
  const permission = binding.value
  if (permission && !auth.permissions.includes(permission)) {
    element.parentNode?.removeChild(element)
  }
}

export function registerPermissionDirective(app: App) {
  app.directive('permission', { mounted })
}
