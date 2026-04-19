# Localization Guide

## Adding a new locale
1. Create `app/src/main/res/values-XX/strings.xml` where XX is the BCP 47 language tag
2. Copy all entries from `app/src/main/res/values/strings.xml`
3. Translate each string value (keep resource names unchanged)
4. Mark untranslated strings with <!-- TODO: translate -->
5. Test on an emulator set to that locale

## Current locales
| Locale | Status |
|--------|--------|
| en (default) | Complete |
| es (Spanish) | Scaffold only — 9 strings translated |

## String guidelines
- Never hardcode UI text in Java — always use R.string.*
- Format strings (%1$s, %d) must keep identical argument positions across locales
- Plural rules vary by language — test plurals on native device
