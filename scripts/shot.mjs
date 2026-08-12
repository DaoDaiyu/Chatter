import { chromium } from 'playwright'

const browser = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium' })
const page = await browser.newPage({
  viewport: { width: 390, height: 844 },
  deviceScaleFactor: 2
})
await page.goto('http://localhost:4173/', { waitUntil: 'networkidle' })

// seed some completions to show streaks/checks like the reference
await page.evaluate(() => {
  const iso = (d) => {
    const x = new Date()
    x.setDate(x.getDate() - d)
    return `${x.getFullYear()}-${String(x.getMonth() + 1).padStart(2, '0')}-${String(x.getDate()).padStart(2, '0')}`
  }
  const c = {}
  const habits = ['japanese', 'voice', 'workout', 'drawing']
  // 6-day all-4 streak
  for (let d = 1; d <= 6; d++) for (const h of habits) c[`${h}:${iso(d)}`] = true
  // today partial
  c[`japanese:${iso(0)}`] = true
  c[`voice:${iso(0)}`] = true
  localStorage.setItem('keizoku.completions.v1', JSON.stringify(c))
})
await page.reload({ waitUntil: 'networkidle' })
await page.waitForTimeout(800)
await page.screenshot({ path: 'scripts/preview.png', fullPage: true })
console.log('shot saved')
await browser.close()
