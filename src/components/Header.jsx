import { useState } from 'react'

// The board's masthead: the maxim, its reading, a vermilion hanko, and the
// day's editable commitment written on a "sticky" note.
export default function Header({ commitment, setCommitment }) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState(commitment)

  const save = () => {
    const value = draft.trim() || 'Just show up. 5 minutes is a win.'
    setCommitment(value)
    setDraft(value)
    setEditing(false)
  }

  return (
    <header className="header">
      <div className="masthead">
        <div className="maxim">
          <h1 lang="ja">継続は力なり</h1>
          <p className="maxim-en">Consistency is strength.</p>
        </div>
        <span className="hanko" aria-hidden="true">
          継続
        </span>
      </div>

      <div className="commitment">
        <span className="commitment-label">Today&rsquo;s commitment</span>
        {editing ? (
          <div className="commitment-edit">
            <textarea
              autoFocus
              rows={2}
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onBlur={save}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  save()
                }
              }}
              maxLength={90}
            />
          </div>
        ) : (
          <button
            className="commitment-text"
            onClick={() => {
              setDraft(commitment)
              setEditing(true)
            }}
            aria-label="Edit today's commitment"
          >
            {commitment}
          </button>
        )}
      </div>
    </header>
  )
}
