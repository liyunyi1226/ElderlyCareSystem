import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

const service = axios.create({
  baseURL: '/api',
  timeout: 20000,
  withCredentials: false
})

service.interceptors.request.use((config) => {
  const sid = sessionStorage.getItem('sessionId')
  if (sid) {
    const url = config.url
    const qIndex = url.indexOf('?')
    const path = qIndex >= 0 ? url.slice(0, qIndex) : url
    const query = qIndex >= 0 ? url.slice(qIndex) : ''
    config.url = path + ';jsessionid=' + encodeURIComponent(sid) + query
  }
  return config
})

service.interceptors.response.use(
  (resp) => resp.data,
  (err) => {
    const status = err.response?.status
    const message = err.response?.data?.message || err.message || '网络错误'
    if (status === 401) {
      const auth = useAuthStore()
      auth.clear()
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    } else {
      ElMessage.error(message)
    }
    return Promise.resolve({ success: false, message })
  }
)

function get(url) {
  return service({ url, method: 'GET' })
}

function post(url, data) {
  const params = new URLSearchParams()
  if (data) {
    Object.keys(data).forEach((k) => {
      if (data[k] !== null && data[k] !== undefined && data[k] !== '') {
        params.append(k, data[k])
      }
    })
  }
  return service({ url, method: 'POST', data: params })
}

function put(url, data) {
  const params = new URLSearchParams()
  if (data) {
    Object.keys(data).forEach((k) => {
      if (data[k] !== null && data[k] !== undefined && data[k] !== '') {
        params.append(k, data[k])
      }
    })
  }
  const qs = params.toString()
  return service({ url: qs ? url + (url.includes('?') ? '&' : '?') + qs : url, method: 'PUT' })
}

function del(url) {
  return service({ url, method: 'DELETE' })
}

export function loginRequest(username, password) {
  const params = new URLSearchParams()
  params.append('username', username)
  params.append('password', password)
  return axios.post('/auth/login', params, {
    baseURL: '/api',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    withCredentials: false,
    timeout: 20000
  }).then((r) => r.data).catch((err) => {
    const message = err.response?.data?.message || '网络错误'
    return { success: false, message }
  })
}

export default { get, post, put, del }
