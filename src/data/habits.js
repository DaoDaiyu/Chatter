// The four disciplines. Each carries its own signature ink color, a Japanese
// name, an English name, and a small brush-mantra shown beneath the week row.
//
// Colors echo the original sumi-e board: warm vermilion, teal, moss, and iris.

export const HABITS = [
  {
    id: 'japanese',
    jp: '日本語',
    en: 'Japanese',
    goal: '5+ MINUTES',
    mantra: '少しずつ、毎日。', // little by little, every day
    color: '#C0553C',
    icon: 'torii'
  },
  {
    id: 'voice',
    jp: 'ボイス',
    en: 'Voice Training',
    goal: '5+ MINUTES',
    mantra: '声は、鍛えれば変わる。', // the voice changes when trained
    color: '#2E8593',
    icon: 'voice'
  },
  {
    id: 'workout',
    jp: '筋トレ',
    en: 'Workout',
    goal: '5+ MINUTES',
    mantra: '強さは、積み重ねから。', // strength comes from accumulation
    color: '#6E8B3D',
    icon: 'dumbbell'
  },
  {
    id: 'drawing',
    jp: 'お絵描き',
    en: 'Drawing',
    goal: '5+ MINUTES',
    mantra: '描くことが、上達への一歩。', // drawing is a step toward mastery
    color: '#8B77C0',
    icon: 'brush'
  }
]

export const HABIT_IDS = HABITS.map((h) => h.id)
