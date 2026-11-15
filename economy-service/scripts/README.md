# Catalog scripts

## Regenerate CS seed SQL

```bash
# optional local cache (gitignored):
# scripts/data/crates.json from ByMykel/CSGO-API

node scripts/generate-cs-catalog-seed.mjs
```

Writes `src/main/resources/db/migration/V8__seed_cs_catalog.sql`. Do not hand-edit bulk inserts.

## Expand catalog (extra cases + min_level ladder)

```bash
node scripts/generate-catalog-expand.mjs
```

Writes `src/main/resources/db/migration/V15__expand_catalog.sql` (new cases/items/drops + min_level 1–15). Do not hand-edit bulk inserts.
