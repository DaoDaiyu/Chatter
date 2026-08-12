// Local-date helpers. We key everything on YYYY-MM-DD in the user's local time
// so a "day" flips at local midnight, not UTC.

export function toISO(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function todayISO() {
  return toISO(new Date())
}

export function fromISO(iso) {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}

export function addDays(iso, delta) {
  const date = fromISO(iso)
  date.setDate(date.getDate() + delta)
  return toISO(date)
}

// Monday-indexed day of week: Mon = 0 ... Sun = 6
export function mondayIndex(date) {
  return (date.getDay() + 6) % 7
}

// The 7 ISO dates (Mon..Sun) for the week containing `anchorISO`,
// shifted by `weekOffset` weeks (0 = this week, -1 = last week).
export function weekDates(anchorISO, weekOffset = 0) {
  const anchor = fromISO(anchorISO)
  const monday = new Date(anchor)
  monday.setDate(anchor.getDate() - mondayIndex(anchor) + weekOffset * 7)
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    return toISO(d)
  })
}

export const DAY_LABELS = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']

export function isFuture(iso) {
  return fromISO(iso).getTime() > fromISO(todayISO()).getTime()
}

export function monthLabel(iso) {
  const d = fromISO(iso)
  return d.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })
}
