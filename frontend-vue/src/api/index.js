import http, { loginRequest } from './request'

export const AuthApi = {
  login: (username, password) => loginRequest(username, password),
  current: () => http.get('/auth/current'),
  logout: () => http.post('/auth/logout')
}

export const ElderlyApi = {
  list: (keyword) => http.get(`/elderly/list${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`),
  get: (id) => http.get(`/elderly/${id}`),
  create: (data) => http.post('/elderly/create', data),
  update: (id, data) => http.put(`/elderly/${id}`, data),
  updateHealth: (id, data) => http.put(`/elderly/${id}/health`, data),
  delete: (id) => http.del(`/elderly/${id}`)
}

export const DoctorApi = {
  list: () => http.get('/doctor/list'),
  get: (id) => http.get(`/doctor/${id}`),
  create: (data) => http.post('/doctor/create', data),
  update: (id, data) => http.put(`/doctor/${id}`, data),
  delete: (id) => http.del(`/doctor/${id}`)
}

export const OrderApi = {
  list: (status) => http.get(`/order/list${status ? `?status=${encodeURIComponent(status)}` : ''}`),
  create: (data) => http.post('/order/create', data),
  receive: (id) => http.post(`/order/${id}/receive`),
  start: (id) => http.post(`/order/${id}/start`),
  return: (id) => http.post(`/order/${id}/return`),
  dispatch: (id, doctorId) => http.post(`/order/${id}/dispatch`, { doctorId }),
  update: (id, data) => http.put(`/order/${id}`, data),
  delete: (id) => http.del(`/order/${id}`)
}

export const VisitApi = {
  list: () => http.get('/visit/list'),
  getByOrder: (orderId) => http.get(`/visit/order/${orderId}`),
  getByElderly: (elderlyId) => http.get(`/visit/elderly/${elderlyId}`),
  create: (data) => http.post('/visit/create', data),
  update: (id, data) => http.put(`/visit/${id}`, data),
  delete: (id) => http.del(`/visit/${id}`)
}

export const FallVisitApi = {
  list: () => http.get('/fallvisit/list'),
  getByOrder: (orderId) => http.get(`/fallvisit/order/${orderId}`),
  getByElderly: (elderlyId) => http.get(`/fallvisit/elderly/${elderlyId}`),
  create: (data) => http.post('/fallvisit/create', data)
}

export const AiApi = {
  diagnose: (data) => http.post('/ai/diagnosis', data)
}

export const AdminApi = {
  getTable: (table) => http.get(`/admin/?table=${table}`),
  insert: (table, data) => http.post(`/admin/?table=${table}`, data),
  update: (table, id, data) => http.put(`/admin/?table=${table}&id=${id}`, data),
  delete: (table, id) => http.del(`/admin/?table=${table}&id=${id}`)
}

export const CommunityApi = {
  list: () => http.get('/community/list'),
  get: (id) => http.get(`/community/${id}`),
  create: (data) => http.post('/community/create', data),
  update: (id, data) => http.put(`/community/${id}`, data),
  delete: (id) => http.del(`/community/${id}`),
  staffList: () => http.get('/community/staff'),
  staffUsers: () => http.get('/community/staff/users'),
  staffCreate: (data) => http.post('/community/staff', data),
  staffDelete: (id) => http.del(`/community/staff/${id}`)
}

export const DeviceApi = {
  list: () => http.get('/device/list'),
  get: (id) => http.get(`/device/${id}`),
  create: (data) => http.post('/device/create', data),
  update: (id, data) => http.put(`/device/${id}`, data),
  delete: (id) => http.del(`/device/${id}`)
}

export const EventApi = {
  list: () => http.get('/event/list'),
  trigger: (data) => http.post('/event/trigger', data)
}

export const FollowUpApi = {
  list: () => http.get('/followup/list'),
  create: (data) => http.post('/followup/create', data),
  update: (id, data) => http.put(`/followup/${id}`, data),
  end: (id) => http.post(`/followup/${id}/end`),
  delete: (id) => http.del(`/followup/${id}`)
}
