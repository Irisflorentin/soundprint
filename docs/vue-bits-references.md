# vue-bits Component References

This project uses selected components from vue-bits (https://vue-bits.dev),
licensed under MIT with Commons Clause.

## Integration Rule

The copied vue-bits source files under `frontend/src/components/vue-bits/` are kept unchanged.
Soundprint customization is applied through wrapper components, props, and scoped CSS so the
third-party source remains easy to audit and replace.

## Galaxy

- Source: https://vue-bits.dev/backgrounds/galaxy
- File: `frontend/src/components/vue-bits/backgrounds/Galaxy.vue`
- Soundprint integration: `frontend/src/components/common/SoundprintGalaxy.vue`
- Customization: `hueShift: 260`, purple/cyan brand tuning, transparent mode for dashboard Hero.

## Circular Gallery

- Source: https://vue-bits.dev/components/circular-gallery
- File: `frontend/src/components/vue-bits/components/CircullarGallery.vue`
- Note: The provided local filename is `CircullarGallery.vue`.
- Soundprint integration: `frontend/src/components/common/SoundprintCircularGallery.vue`
- Customization: business data to `{ image, text }[]`, real cover URLs, SVG data URL fallback,
  `textColor: '#F5F5F7'`, `font: 'bold 22px Inter'`, `bend: 2`.

## Antigravity

- Source: https://vue-bits.dev/animations/antigravity
- File: `frontend/src/components/vue-bits/animations/Antigravity.vue`
- Soundprint integration: `frontend/src/components/common/SoundprintAntigravity.vue`
- Customization: brand purple particles, reduced count for one-page performance budget.

## Balatro

- Source: https://vue-bits.dev/animations/balatro
- File: `frontend/src/components/vue-bits/animations/Balatro.vue`
- Soundprint integration: `frontend/src/components/common/SoundprintBalatro.vue`
- Customization: `color1/2/3` set to Soundprint brand purple/cyan/ink. Scoped CSS overrides
  the child canvas z-index so conversion progress remains readable.
