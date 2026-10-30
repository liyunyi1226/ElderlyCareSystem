import { defineStore } from 'pinia'
import { AuthApi } from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(sessionStorage.getItem('currentUser') || 'null'),
    sessionId: sessionStorage.getItem('sessionId') || ''
  }),
  getters: {
    isLoggedIn: (s) => !!s.user,
    role: (s) => s.user?.role || '',
    homePath: (s) => {
      const role = s.user?.role
      if (role === 'system_admin') return '/admin/elderly'
      if (role === 'doctor') return '/doctor/tasks'
      return '/community/dashboard'
    }
  },
  actions: {
    setAuth(user, sessionId) {
      this.user = user
      this.sessionId = sessionId || user?.sessionId || ''
      sessionStorage.setItem('currentUser', JSON.stringify(user))
      sessionStorage.setItem('sessionId', this.sessionId)
      localStorage.removeItem('currentUser')
    },
    setUser(user) {
      this.user = user
      sessionStorage.setItem('currentUser', JSON.stringify(user))
    },
    clear() {
      this.user = null
      this.sessionId = ''
      sessionStorage.removeItem('currentUser')
      sessionStorage.removeItem('sessionId')
      localStorage.removeItem('currentUser')
    },

    async refreshFromServer() {
      const res = await AuthApi.current()
      if (res && res.success) {
        this.setUser(res.data)
        return true
      }
      this.clear()
      return false
    }
  }
})
