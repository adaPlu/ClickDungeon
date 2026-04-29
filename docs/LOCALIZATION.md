# Localization Guide

## Adding a New Locale
1. Create `app/src/main/res/values-XX/strings.xml` where `XX` is the BCP 47 language tag.
2. Copy all entries from `app/src/main/res/values/strings.xml`.
3. Translate each string value, keeping resource names and format arguments unchanged.
4. Do not commit partial locale folders. Android lint treats any `values-XX` folder as a supported locale and requires every translatable string.
5. Test on an emulator set to that locale.

## Current Locales
| Locale | Status |
|--------|--------|
| en (default) | Complete |

Spanish launch text was removed for v1 readiness because it was only a stub.
Add `values-es/strings.xml` again only when every translatable default string is
translated and lint-clean.

## String Guidelines
- Never hardcode UI text in Java; always use `R.string.*`.
- Format strings (`%1$s`, `%d`) must keep identical argument positions across locales.
- Plural rules vary by language; test plurals on a native device or emulator for that locale.
