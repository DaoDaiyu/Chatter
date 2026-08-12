import { DAY_LABELS, todayISO, isFuture } from '../utils/dates.js'
import { habitStreak } from '../utils/streak.js'
import { HabitIcon, Sakura } from './Icons.jsx'

// One discipline: identity block, a Mon–Sun row of ink rings you tap to fill,
// the brush-mantra, and a live per-habit streak.
export default function HabitCard({ habit, weekDays, completed, onToggle }) {
  const today = todayISO()
  const streak = habitStreak(completed, habit.id)
  const style = { '--habit': habit.color }

  return (
    <section className="habit-card" style={style}>
      <div className="habit-head">
        <span className="habit-icon">
          <HabitIcon name={habit.icon} />
        </span>
        <div className="habit-names">
          <span className="habit-jp" lang="ja">
            {habit.jp}
          </span>
          <span className="habit-en">{habit.en}</span>
        </div>
        <span className="habit-goal">{habit.goal}</span>
      </div>

      <div className="week-row" role="group" aria-label={`${habit.en} this week`}>
        {weekDays.map((iso, i) => {
          const key = `${habit.id}:${iso}`
          const done = !!completed[key]
          const future = isFuture(iso)
          const isToday = iso === today
          return (
            <div className="day-cell" key={iso}>
              <span className="day-label">{DAY_LABELS[i]}</span>
              <button
                className={
                  'ring' +
                  (done ? ' is-done' : '') +
                  (isToday ? ' is-today' : '') +
                  (future ? ' is-future' : '')
                }
                disabled={future}
                aria-pressed={done}
                aria-label={`${habit.en}, ${DAY_LABELS[i]}${done ? ', done' : ''}`}
                onClick={() => onToggle(habit.id, iso)}
              >
                {done && (
                  <svg viewBox="0 0 24 24" className="check" aria-hidden="true">
                    <path d="M5 13l4 4L19 7" />
                  </svg>
                )}
              </button>
            </div>
          )
        })}
      </div>

      <div className="habit-foot">
        <span className="mantra" lang="ja">
          {habit.mantra}
        </span>
        <span className="daily-progress">
          {streak > 0 ? (
            <>
              <Sakura className="foot-sakura" />
              {streak} day{streak === 1 ? '' : 's'}
            </>
          ) : (
            'Daily progress'
          )}
        </span>
      </div>
    </section>
  )
}
