import { MILESTONES, allHabitsStreak } from '../utils/streak.js'
import { HABIT_IDS } from '../data/habits.js'
import { Sakura } from './Icons.jsx'

// The dark footer: the all-four consistency streak, a rank of milestone
// banners that bloom as you pass them, and the closing encouragement.
export default function StreakBar({ completed }) {
  const streak = allHabitsStreak(completed, HABIT_IDS)
  const nextMilestone = MILESTONES.find((m) => m > streak)

  return (
    <footer className="streak-bar">
      <div className="streak-top">
        <div className="streak-count">
          <span className="streak-caption">
            Consistency streak
            <br />
            <span className="streak-sub">(All 4)</span>
          </span>
          <span className="streak-number">{streak}</span>
          <span className="streak-days">{streak === 1 ? 'DAY' : 'DAYS'}</span>
        </div>

        <div className="milestones" role="list" aria-label="Milestones">
          {MILESTONES.map((m) => {
            const reached = streak >= m
            const isNext = m === nextMilestone
            return (
              <div
                className={'banner' + (reached ? ' reached' : '') + (isNext ? ' next' : '')}
                role="listitem"
                key={m}
              >
                <span className="banner-num">{m}</span>
                <span className="banner-flower">{reached ? <Sakura /> : ''}</span>
              </div>
            )
          })}
        </div>
      </div>

      <div className="streak-foot">
        <div className="five">
          <span className="five-num">5</span>
          <span className="five-text">
            MINUTES
            <br />
            COUNTS.
          </span>
        </div>
        <p className="five-sub">Show up. Do the work. Be proud.</p>
      </div>

      <p className="footer-motto">
        <Sakura className="motto-sakura" /> Not perfect. Just consistent.
      </p>
    </footer>
  )
}
