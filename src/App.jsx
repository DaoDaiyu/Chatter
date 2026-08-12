import { useState, useMemo } from 'react'
import Header from './components/Header.jsx'
import HabitCard from './components/HabitCard.jsx'
import StreakBar from './components/StreakBar.jsx'
import { HABITS } from './data/habits.js'
import { useHabits } from './useHabits.js'
import { todayISO, weekDates, fromISO } from './utils/dates.js'

export default function App() {
  const { completed, toggle, commitment, setCommitment } = useHabits()
  const [weekOffset, setWeekOffset] = useState(0)

  const weekDays = useMemo(() => weekDates(todayISO(), weekOffset), [weekOffset])

  const rangeLabel = useMemo(() => {
    const start = fromISO(weekDays[0])
    const end = fromISO(weekDays[6])
    const opts = { month: 'short', day: 'numeric' }
    if (weekOffset === 0) return 'This week'
    return `${start.toLocaleDateString(undefined, opts)} – ${end.toLocaleDateString(undefined, opts)}`
  }, [weekDays, weekOffset])

  return (
    <div className="app">
      <div className="paper">
        <Header commitment={commitment} setCommitment={setCommitment} />

        <nav className="week-nav" aria-label="Change week">
          <button onClick={() => setWeekOffset((w) => w - 1)} aria-label="Previous week">
            ‹
          </button>
          <span className="week-label">{rangeLabel}</span>
          <button
            onClick={() => setWeekOffset((w) => Math.min(0, w + 1))}
            disabled={weekOffset >= 0}
            aria-label="Next week"
          >
            ›
          </button>
        </nav>

        <main className="habits">
          {HABITS.map((habit) => (
            <HabitCard
              key={habit.id}
              habit={habit}
              weekDays={weekDays}
              completed={completed}
              onToggle={toggle}
            />
          ))}
        </main>

        <StreakBar completed={completed} />
      </div>
    </div>
  )
}
