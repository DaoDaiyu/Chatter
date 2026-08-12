import { todayISO, addDays } from './dates.js'

// A day "counts" toward the all-habits streak when every habit is done that day.
export function isDayComplete(completed, habitIds, iso) {
  if (habitIds.length === 0) return false
  return habitIds.every((id) => completed[`${id}:${iso}`])
}

// Consecutive days (ending today) where all habits were completed.
// Today is forgiving: if today isn't finished yet we don't break the streak,
// we just start counting from yesterday — you only lose it when a full day passes.
export function allHabitsStreak(completed, habitIds) {
  if (habitIds.length === 0) return 0
  let cursor = todayISO()
  let streak = 0

  if (!isDayComplete(completed, habitIds, cursor)) {
    cursor = addDays(cursor, -1) // grace for the in-progress day
  }

  while (isDayComplete(completed, habitIds, cursor)) {
    streak += 1
    cursor = addDays(cursor, -1)
  }
  return streak
}

// Per-habit current streak, same forgiving-today rule.
export function habitStreak(completed, habitId) {
  let cursor = todayISO()
  let streak = 0
  if (!completed[`${habitId}:${cursor}`]) {
    cursor = addDays(cursor, -1)
  }
  while (completed[`${habitId}:${cursor}`]) {
    streak += 1
    cursor = addDays(cursor, -1)
  }
  return streak
}

export const MILESTONES = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 50]
