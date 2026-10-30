export const orderStatusTag = {
  '待分配医生': 'danger',
  '待接收': 'warning',
  '进行中': 'primary',
  '巡访中': 'primary',
  '已完成': 'success',
  '已退回': 'info'
}

export const deviceStatusTag = {
  '正常': 'success',
  '离线': 'info',
  '故障': 'warning'
}

export const eventStatusTag = {
  '待处理': 'danger',
  '已派单': 'primary',
  '已处理': 'success'
}

export const followUpStatusTag = {
  '进行中': 'primary',
  '已结束': 'success'
}

export const roleMap = {
  community_worker: { label: '社区人员', tag: 'success' },
  system_admin: { label: '系统管理员', tag: 'danger' },
  doctor: { label: '医生', tag: 'primary' }
}

export function roleLabel(role) {
  return roleMap[role]?.label || role
}

export function isDeviceAlert(eventType) {
  return eventType === '摔倒告警' || eventType === '手动呼救'
}
