// Small line icons drawn in a loose, single-weight style so they sit well
// against the ink aesthetic. They inherit `color` via currentColor.

const base = {
  width: 24,
  height: 24,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.6,
  strokeLinecap: 'round',
  strokeLinejoin: 'round'
}

function Torii(props) {
  return (
    <svg {...base} {...props}>
      <path d="M3 6c3 1.5 15 1.5 18 0" />
      <path d="M4 9h16" />
      <path d="M6.5 6.5V20" />
      <path d="M17.5 6.5V20" />
    </svg>
  )
}

function Voice(props) {
  return (
    <svg {...base} {...props}>
      <path d="M12 4v16" />
      <path d="M8 8v8" />
      <path d="M16 8v8" />
      <path d="M4 10v4" />
      <path d="M20 10v4" />
    </svg>
  )
}

function Dumbbell(props) {
  return (
    <svg {...base} {...props}>
      <path d="M6.5 8.5v7" />
      <path d="M4 10v4" />
      <path d="M17.5 8.5v7" />
      <path d="M20 10v4" />
      <path d="M6.5 12h11" />
    </svg>
  )
}

function Brush(props) {
  return (
    <svg {...base} {...props}>
      <path d="M14 4l6 6" />
      <path d="M15 9L8 16l-2.5.5L5 14l7-7" />
      <path d="M5.5 14.5C4 16 3.5 20 3.5 20s4-.5 5.5-2" />
    </svg>
  )
}

function Sakura(props) {
  return (
    <svg viewBox="0 0 24 24" width={16} height={16} fill="currentColor" {...props}>
      <path d="M12 3c1.2 0 2 1.2 1.7 2.6 1.3-.7 2.9-.1 3.3 1.1.4 1.2-.5 2.5-1.9 2.7 1.1.9 1.1 2.5.1 3.3-1 .8-2.5.5-3.1-.7-.6 1.2-2.1 1.5-3.1.7-1-.8-1-2.4.1-3.3-1.4-.2-2.3-1.5-1.9-2.7.4-1.2 2-1.8 3.3-1.1C10 4.2 10.8 3 12 3z" />
      <circle cx="12" cy="10" r="1.4" fill="#fff" opacity=".55" />
    </svg>
  )
}

const MAP = { torii: Torii, voice: Voice, dumbbell: Dumbbell, brush: Brush }

export function HabitIcon({ name, ...props }) {
  const C = MAP[name] || Brush
  return <C {...props} />
}

export { Sakura }
