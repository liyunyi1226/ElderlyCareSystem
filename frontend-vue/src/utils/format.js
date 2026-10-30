export function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  const pad = (n) => String(n).padStart(2, '0')
  const datePart = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`

  const hasTime = dateStr && (dateStr.length > 10 || /[T ]\d{2}:\d{2}/.test(dateStr))
  if (!hasTime) return datePart
  return `${datePart} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}
