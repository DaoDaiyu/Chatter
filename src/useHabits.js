import { useState, useEffect, useCallback } from 'react'

const STORAGE_KEY = 'keizoku.completions.v1'
const COMMIT_KEY = 'keizoku.commitment.v1'

function load(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

// `completed` is a flat map of "habitId:YYYY-MM-DD" -> true.
export function useHabits() {
  const [completed, setCompleted] = useState(() => load(STORAGE_KEY, {}))
  const [commitment, setCommitment] = useState(() =>
    load(COMMIT_KEY, 'Just show up. 5 minutes is a win.')
  )

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(completed))
  }, [completed])

  useEffect(() => {
    localStorage.setItem(COMMIT_KEY, JSON.stringify(commitment))
  }, [commitment])

  const toggle = useCallback((habitId, iso) => {
    const key = `${habitId}:${iso}`
    setCompleted((prev) => {
      const next = { ...prev }
      if (next[key]) delete next[key]
      else next[key] = true
      return next
    })
  }, [])

  return { completed, toggle, commitment, setCommitment }
}
