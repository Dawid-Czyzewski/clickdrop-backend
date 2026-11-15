/**
 * Generates Flyway V15 expand catalog from ByMykel CSGO-API crates.json.
 * Adds new cases (distinct drop pools) + intentional min_level ladder 1–15.
 * Usage: node scripts/generate-catalog-expand.mjs
 */
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const CRATES_URL =
  'https://raw.githubusercontent.com/ByMykel/CSGO-API/main/public/api/en/crates.json'
const LOCAL_CRATES = join(__dirname, 'data', 'crates.json')

/** New active cases only (not already in V8 allowlist). */
const NEW_CASES = [
  { name: 'Gallery Case', price: 55, sortOrder: 13, minLevel: 1 },
  { name: 'Chroma 3 Case', price: 60, sortOrder: 14, minLevel: 2 },
  { name: 'Gamma 2 Case', price: 72, sortOrder: 15, minLevel: 4 },
  { name: 'Horizon Case', price: 78, sortOrder: 16, minLevel: 5 },
  { name: 'Prisma Case', price: 82, sortOrder: 17, minLevel: 6 },
  { name: 'Spectrum Case', price: 86, sortOrder: 18, minLevel: 7 },
  { name: 'Glove Case', price: 98, sortOrder: 19, minLevel: 9 },
  { name: 'Operation Hydra Case', price: 108, sortOrder: 20, minLevel: 11 },
  { name: 'Operation Riptide Case', price: 115, sortOrder: 21, minLevel: 13 },
  { name: 'Fever Case', price: 120, sortOrder: 22, minLevel: 14 },
]

/** Rebalance existing V8 cases — not price-only buckets. */
const EXISTING_MIN_LEVEL = {
  CS20_CASE: 1,
  DANGER_ZONE_CASE: 1,
  SPECTRUM_2_CASE: 2,
  CLUTCH_CASE: 3,
  SNAKEBITE_CASE: 4,
  PRISMA_2_CASE: 5,
  FRACTURE_CASE: 6,
  KILOWATT_CASE: 7,
  RECOIL_CASE: 8,
  OPERATION_BROKEN_FANG_CASE: 10,
  DREAMS_NIGHTMARES_CASE: 12,
  REVOLUTION_CASE: 15,
}

/** Per-case band totals so pools are not identical weight clones. */
const BAND_WEIGHT_BY_CASE = {
  GALLERY_CASE: { MIL_SPEC: 8200, RESTRICTED: 1500, CLASSIFIED: 300, COVERT: 70, RARE: 30 },
  CHROMA_3_CASE: { MIL_SPEC: 7800, RESTRICTED: 1700, CLASSIFIED: 340, COVERT: 80, RARE: 28 },
  GAMMA_2_CASE: { MIL_SPEC: 8000, RESTRICTED: 1600, CLASSIFIED: 320, COVERT: 64, RARE: 26 },
  HORIZON_CASE: { MIL_SPEC: 7600, RESTRICTED: 1800, CLASSIFIED: 360, COVERT: 72, RARE: 24 },
  PRISMA_CASE: { MIL_SPEC: 7900, RESTRICTED: 1650, CLASSIFIED: 310, COVERT: 68, RARE: 27 },
  SPECTRUM_CASE: { MIL_SPEC: 7700, RESTRICTED: 1750, CLASSIFIED: 330, COVERT: 75, RARE: 25 },
  GLOVE_CASE: { MIL_SPEC: 8100, RESTRICTED: 1550, CLASSIFIED: 280, COVERT: 60, RARE: 40 },
  OPERATION_HYDRA_CASE: { MIL_SPEC: 7500, RESTRICTED: 1850, CLASSIFIED: 380, COVERT: 90, RARE: 32 },
  OPERATION_RIPTIDE_CASE: { MIL_SPEC: 7850, RESTRICTED: 1680, CLASSIFIED: 325, COVERT: 66, RARE: 29 },
  FEVER_CASE: { MIL_SPEC: 7400, RESTRICTED: 1900, CLASSIFIED: 400, COVERT: 85, RARE: 35 },
}

const DEFAULT_BAND = {
  MIL_SPEC: 8000,
  RESTRICTED: 1600,
  CLASSIFIED: 320,
  COVERT: 64,
  RARE: 26,
}

function sqlString(value) {
  return `'${String(value).replace(/'/g, "''")}'`
}

function slugify(name) {
  return name
    .normalize('NFKD')
    .replace(/[★™]/g, '')
    .replace(/\|/g, ' ')
    .replace(/[^a-zA-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toUpperCase()
    .slice(0, 120)
}

function mapRarity(rarityName) {
  const n = (rarityName || '').toLowerCase()
  if (n.includes('mil-spec') || n.includes('mil spec')) return 'MIL_SPEC'
  if (n.includes('restricted')) return 'RESTRICTED'
  if (n.includes('classified')) return 'CLASSIFIED'
  if (n.includes('covert') || n.includes('extraordinary') || n.includes('ancient')) {
    return 'COVERT'
  }
  return null
}

function parseSkinName(name) {
  const cleaned = name.replace(/^★\s*/, '').replace(/\s*\(.*?\)\s*$/, '').trim()
  const parts = cleaned.split('|').map((p) => p.trim())
  if (parts.length >= 2) {
    return { weapon: parts[0], pattern: parts.slice(1).join(' | ') }
  }
  return { weapon: cleaned, pattern: cleaned }
}

function skinCode(name) {
  const { weapon, pattern } = parseSkinName(name)
  return slugify(`${weapon}_${pattern}`)
}

async function loadCrates() {
  if (existsSync(LOCAL_CRATES)) {
    return JSON.parse(readFileSync(LOCAL_CRATES, 'utf8'))
  }
  const res = await fetch(CRATES_URL)
  if (!res.ok) throw new Error(`Failed to fetch crates.json: ${res.status}`)
  return res.json()
}

async function main() {
  const crates = await loadCrates()
  const items = new Map()
  const caseRows = []
  const dropRows = []

  for (const spec of NEW_CASES) {
    const crate = crates.find(
      (c) =>
        c.type === 'Case' &&
        (c.name === spec.name || c.market_hash_name === spec.name),
    )
    if (!crate) throw new Error(`Case not found in API: ${spec.name}`)

    const caseCode = slugify(crate.market_hash_name || crate.name)
    const displayName = crate.market_hash_name || crate.name
    caseRows.push({
      code: caseCode,
      displayName,
      price: spec.price,
      sortOrder: spec.sortOrder,
      minLevel: spec.minLevel,
    })

    const bands = {
      MIL_SPEC: [],
      RESTRICTED: [],
      CLASSIFIED: [],
      COVERT: [],
      RARE: [],
    }

    for (const skin of crate.contains || []) {
      const rarity = mapRarity(skin.rarity?.name)
      if (!rarity) continue
      const code = skinCode(skin.name)
      if (!items.has(code)) {
        items.set(code, { code, displayName: skin.name, rarity })
      }
      bands[rarity].push(code)
    }

    for (const skin of crate.contains_rare || []) {
      const code = skinCode(skin.name)
      if (!items.has(code)) {
        items.set(code, { code, displayName: skin.name, rarity: 'COVERT' })
      }
      bands.RARE.push(code)
    }

    const bandWeight = BAND_WEIGHT_BY_CASE[caseCode] ?? DEFAULT_BAND
    for (const [band, codes] of Object.entries(bands)) {
      const unique = [...new Set(codes)]
      if (unique.length === 0) continue
      const total = bandWeight[band] ?? DEFAULT_BAND[band]
      const each = Math.max(1, Math.floor(total / unique.length))
      for (const code of unique) {
        dropRows.push({ caseCode, itemCode: code, weight: each })
      }
    }
  }

  const itemList = [...items.values()].sort((a, b) => a.code.localeCompare(b.code))
  const lines = []
  lines.push('-- Generated by scripts/generate-catalog-expand.mjs — do not hand-edit bulk inserts.')
  lines.push('-- Expand active CS catalog + intentional min_level ladder (1–15).')
  lines.push('')
  lines.push('INSERT INTO items (code, rarity, display_name, active) VALUES')
  lines.push(
    itemList
      .map(
        (i) =>
          `    (${sqlString(i.code)}, ${sqlString(i.rarity)}, ${sqlString(i.displayName)}, TRUE)`,
      )
      .join(',\n'),
  )
  lines.push('ON CONFLICT (code) DO UPDATE SET')
  lines.push('    display_name = EXCLUDED.display_name,')
  lines.push('    rarity = EXCLUDED.rarity,')
  lines.push('    active = TRUE;')
  lines.push('')
  lines.push(
    'INSERT INTO cases (code, price, sort_order, display_name, active, min_level) VALUES',
  )
  lines.push(
    caseRows
      .map(
        (c) =>
          `    (${sqlString(c.code)}, ${c.price.toFixed(2)}, ${c.sortOrder}, ${sqlString(c.displayName)}, TRUE, ${c.minLevel})`,
      )
      .join(',\n'),
  )
  lines.push('ON CONFLICT (code) DO UPDATE SET')
  lines.push('    price = EXCLUDED.price,')
  lines.push('    sort_order = EXCLUDED.sort_order,')
  lines.push('    display_name = EXCLUDED.display_name,')
  lines.push('    active = TRUE,')
  lines.push('    min_level = EXCLUDED.min_level;')
  lines.push('')
  lines.push('INSERT INTO case_drops (case_id, item_id, weight)')
  lines.push('SELECT c.id, i.id, d.weight')
  lines.push('FROM (VALUES')
  lines.push(
    dropRows
      .map(
        (d) =>
          `    (${sqlString(d.caseCode)}, ${sqlString(d.itemCode)}, ${d.weight})`,
      )
      .join(',\n'),
  )
  lines.push(') AS d(case_code, item_code, weight)')
  lines.push('JOIN cases c ON c.code = d.case_code')
  lines.push('JOIN items i ON i.code = d.item_code')
  lines.push('ON CONFLICT (case_id, item_id) DO UPDATE SET weight = EXCLUDED.weight;')
  lines.push('')
  lines.push('-- Rebalance min_level for existing V8 cases (ladder, not price-only buckets)')
  for (const [code, level] of Object.entries(EXISTING_MIN_LEVEL)) {
    lines.push(
      `UPDATE cases SET min_level = ${level} WHERE code = ${sqlString(code)};`,
    )
  }
  lines.push('')

  const outPath = join(
    __dirname,
    '..',
    'src',
    'main',
    'resources',
    'db',
    'migration',
    'V15__expand_catalog.sql',
  )
  writeFileSync(outPath, `${lines.join('\n')}\n`, 'utf8')
  console.log(
    `Wrote ${outPath} (${itemList.length} items, ${caseRows.length} cases, ${dropRows.length} drops)`,
  )
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
