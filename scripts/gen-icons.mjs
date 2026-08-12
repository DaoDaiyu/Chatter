// One-off: rasterize the app icon (enso + sakura on washi) into PWA PNGs.
// Run with `node scripts/gen-icons.mjs`. sharp is used only for this step.
import sharp from 'sharp'
import { writeFileSync } from 'node:fs'

const svg = (size) => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
  <rect width="512" height="512" fill="#ede8da"/>
  <rect x="46" y="46" width="420" height="420" rx="0" fill="#ede8da"/>
  <path d="M366 150a170 170 0 1 0 26 188" fill="none" stroke="#26221e"
        stroke-width="30" stroke-linecap="round"/>
  <path d="M256 176c13 0 22 14 18 30 15-9 34-1 38 13 4 14-6 28-23 30 12 10 12 28 1 37-11 9-29 5-36-9-7 14-25 18-36 9-11-9-11-27 1-37-17-2-27-16-23-30 4-14 23-22 38-13-4-16 5-30 18-30z"
        fill="#b5402f"/>
  <circle cx="256" cy="250" r="15" fill="#ede8da" opacity="0.5"/>
</svg>`

for (const size of [192, 512]) {
  const png = await sharp(Buffer.from(svg(size)))
    .resize(size, size)
    .png()
    .toBuffer()
  writeFileSync(new URL(`../public/icon-${size}.png`, import.meta.url), png)
  console.log(`wrote public/icon-${size}.png`)
}
