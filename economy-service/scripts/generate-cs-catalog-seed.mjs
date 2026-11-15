/**
 * Generates Flyway seed SQL from ByMykel CSGO-API crates.json.
 * Usage: node scripts/generate-cs-catalog-seed.mjs
 * Re-run when refreshing catalog; do not hand-edit the huge INSERT block.
 */
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const CRATES_URL =
  'https://raw.githubusercontent.com/ByMykel/CSGO-API/main/public/api/en/crates.json'
const LOCAL_CRATES = join(__dirname, 'data', 'crates.json')

const CASE_ALLOWLIST = [
  'Revolution Case',
  'Dreams & Nightmares Case',
  'Kilowatt Case',
  'Recoil Case',
  'Fracture Case',
  'Snakebite Case',
  'Clutch Case',
  'Spectrum 2 Case',
  'Prisma 2 Case',
  'Danger Zone Case',
  'CS20 Case',
  'Operation Broken Fang Case',
]

/** Approximate CS odds as shared band weights (split evenly per skin). */
const BAND_WEIGHT = {
  MIL_SPEC: 8000,
  RESTRICTED: 1600,
  CLASSIFIED: 320,
  COVERT: 64,
  RARE: 26,
}

const CASE_PRICE = {
  REVOLUTION_CASE: 125,
  DREAMS_NIGHTMARES_CASE: 110,
  KILOWATT_CASE: 95,
  RECOIL_CASE: 100,
  FRACTURE_CASE: 90,
  SNAKEBITE_CASE: 85,
  CLUTCH_CASE: 80,
  SPECTRUM_2_CASE: 75,
  PRISMA_2_CASE: 88,
  DANGER_ZONE_CASE: 70,
  CS20_CASE: 65,
  OPERATION_BROKEN_FANG_CASE: 105,
}

const OLD_CASES = [
  'ANVIL',
  'TEMPER',
  'FORGEFIRE',
  'EMBER',
  'NOVA',
  'RIFT',
  'PULSE',
  'SHADOW',
]

const LEGACY_ITEM_NAMES = {
  GLOCK_WATER_ELEMENTAL: 'Glock-18 | Water Elemental',
  USP_GUARDIAN: 'USP-S | Guardian',
  AK_REDLINE: 'AK-47 | Redline',
  AWP_ELECTRIC_HIVE: 'AWP | Electric Hive',
  M4A4_DESERT_STRIKE: 'M4A4 | Desert-Strike',
  AWP_ASIIMOV: 'AWP | Asiimov',
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

function mapRarity(rarityName, isRareSpecial) {
  if (isRareSpecial) return 'COVERT'
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

  const selected = CASE_ALLOWLIST.map((wanted) => {
    const crate = crates.find(
      (c) =>
        c.type === 'Case' &&
        (c.name === wanted || c.market_hash_name === wanted),
    )
    if (!crate) throw new Error(`Case not found in API: ${wanted}`)
    return crate
  })

  /** @type {Map<string, { code: string, displayName: string, rarity: string }>} */
  const items = new Map()
  const caseRows = []
  const dropRows = []

  selected.forEach((crate, index) => {
    const caseCode = slugify(crate.market_hash_name || crate.name)
    const displayName = crate.market_hash_name || crate.name
    const price = CASE_PRICE[caseCode] ?? 100 + index * 5

    caseRows.push({
      code: caseCode,
      displayName,
      price,
      sortOrder: index + 1,
    })

    /** @type {Record<string, string[]>} */
    const bands = {
      MIL_SPEC: [],
      RESTRICTED: [],
      CLASSIFIED: [],
      COVERT: [],
      RARE: [],
    }

    for (const skin of crate.contains || []) {
      const rarity = mapRarity(skin.rarity?.name, false)
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
        items.set(code, {
          code,
          displayName: skin.name,
          rarity: 'COVERT',
        })
      }
      bands.RARE.push(code)
    }

    for (const [band, codes] of Object.entries(bands)) {
      const unique = [...new Set(codes)]
      if (unique.length === 0) continue
      const total = BAND_WEIGHT[band]
      const each = Math.max(1, Math.floor(total / unique.length))
      for (const code of unique) {
        dropRows.push({ caseCode, itemCode: code, weight: each })
      }
    }
  })

  const itemList = [...items.values()].sort((a, b) => a.code.localeCompare(b.code))

  const dailySkins = itemList
    .filter((i) => i.rarity === 'MIL_SPEC' || i.rarity === 'RESTRICTED')
    .slice(0, 12)

  const lines = []
  lines.push('-- Generated by scripts/generate-cs-catalog-seed.mjs — do not hand-edit bulk inserts.')
  lines.push('-- Schema + CS catalog seed (real case/skin names).')
  lines.push('')
  lines.push('ALTER TABLE items ALTER COLUMN code TYPE VARCHAR(128);')
  lines.push('ALTER TABLE cases ALTER COLUMN code TYPE VARCHAR(128);')
  lines.push('')
  lines.push("ALTER TABLE items ADD COLUMN IF NOT EXISTS display_name VARCHAR(200) NOT NULL DEFAULT '';")
  lines.push("ALTER TABLE cases ADD COLUMN IF NOT EXISTS display_name VARCHAR(200) NOT NULL DEFAULT '';")
  lines.push('')
  lines.push('-- Backfill legacy display names')
  for (const [code, name] of Object.entries(LEGACY_ITEM_NAMES)) {
    lines.push(
      `UPDATE items SET display_name = ${sqlString(name)} WHERE code = ${sqlString(code)} AND (display_name = '' OR display_name IS NULL);`,
    )
  }
  lines.push(`UPDATE items SET display_name = code WHERE display_name = '';`)
  lines.push(`UPDATE cases SET display_name = code WHERE display_name = '';`)
  lines.push('')
  lines.push(
    `UPDATE cases SET active = FALSE WHERE code IN (${OLD_CASES.map(sqlString).join(', ')});`,
  )
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
  lines.push('INSERT INTO cases (code, price, sort_order, display_name, active) VALUES')
  lines.push(
    caseRows
      .map(
        (c) =>
          `    (${sqlString(c.code)}, ${c.price.toFixed(2)}, ${c.sortOrder}, ${sqlString(c.displayName)}, TRUE)`,
      )
      .join(',\n'),
  )
  lines.push('ON CONFLICT (code) DO UPDATE SET')
  lines.push('    price = EXCLUDED.price,')
  lines.push('    sort_order = EXCLUDED.sort_order,')
  lines.push('    display_name = EXCLUDED.display_name,')
  lines.push('    active = TRUE;')
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
  lines.push('-- Refresh daily skin pool (keep CASH rows)')
  lines.push("DELETE FROM daily_drops WHERE reward_type = 'SKIN';")
  lines.push('INSERT INTO daily_drops (reward_type, item_id, cash_amount, weight)')
  lines.push('SELECT \'SKIN\', i.id, NULL, d.weight')
  lines.push('FROM (VALUES')
  lines.push(
    dailySkins
      .map((s, idx) => {
        const weight = s.rarity === 'MIL_SPEC' ? 40 - idx : 20
        return `    (${sqlString(s.code)}, ${Math.max(5, weight)})`
      })
      .join(',\n'),
  )
  lines.push(') AS d(item_code, weight)')
  lines.push('JOIN items i ON i.code = d.item_code;')
  lines.push('')

  const outPath = join(
    __dirname,
    '..',
    'src',
    'main',
    'resources',
    'db',
    'migration',
    'V8__seed_cs_catalog.sql',
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
