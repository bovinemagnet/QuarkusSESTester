# Design System Specification: The Kinetic Terminal

## 1. Overview & Creative North Star

### Creative North Star: "The Kinetic Terminal"
This design system rejects the "SaaS-template" aesthetic in favor of a high-end editorial experience tailored for the modern developer. It treats code and data as high-art, utilizing a "Kinetic Terminal" philosophy where the UI feels like a living, breathing extension of the machine. 

To move beyond standard UI, we leverage intentional asymmetry—placing metadata in unexpected but logical gutters—and a high-contrast typography scale. Because this is built for an HTMX-based dashboard, the design prioritizes "Hyper-Reactivity." Every state change, partial fragment swap, and background sync is communicated through sophisticated tonal shifts and micro-interactions rather than jarring layout shifts.

---

## 2. Colors & Surface Philosophy

The palette is anchored in a deep charcoal foundation (`#0e0e0e`), punctuated by vibrant, high-energy accents that draw the eye to critical developer metrics.

### The "No-Line" Rule
**Strict Mandate:** Designers are prohibited from using 1px solid borders to section off the UI. 
Structural boundaries must be defined exclusively through background color shifts. For example, a side navigation panel should use `surface_container_low`, while the main workspace utilizes the base `surface` color. This creates a seamless, "molded" look that feels more premium than a grid of boxes.

### Surface Hierarchy & Nesting
Depth is achieved through a 5-tier stacking system. Instead of flat cards, treat the UI as a series of physical layers:
- **Level 1 (Base):** `surface` (#0e0e0e) for the primary application background.
- **Level 2 (Sectioning):** `surface_container_low` (#131313) for large layout regions.
- **Level 3 (Content):** `surface_container` (#1a1a1a) for primary cards or dashboard widgets.
- **Level 4 (Interactive):** `surface_container_high` (#20201f) for hover states or active items.
- **Level 5 (Accent):** `surface_container_highest` (#262626) for nested code blocks or metadata chips.

### The "Glass & Gradient" Rule
To add visual "soul," main CTAs and primary status indicators should utilize a subtle linear gradient transitioning from `primary` (#9fa7ff) to `primary_container` (#8e98ff). For floating elements (modals, command palettes), use **Glassmorphism**: apply `surface_variant` at 60% opacity with a `20px` backdrop blur to allow the HTMX-driven dashboard to "peek" through the overlay.

---

## 3. Typography

The typography strategy pairs a brutalist, tech-forward sans-serif with a functional monospace to bridge the gap between "Editorial" and "Engineering."

- **Headlines & Display:** `Space Grotesk`. Use `display-lg` and `headline-md` for high-impact metrics. The wide aperture of this font provides a modern, authoritative voice.
- **UI & Navigation:** `Inter`. Use `title-sm` and `body-md` for functional elements. Its neutral tone ensures readability in complex configurations.
- **Code & Logs:** `JetBrains Mono` (or equivalent high-quality Monospace). All technical data, log streams, and HTMX attributes must be rendered in `label-md` or `label-sm` using the Monospace stack to distinguish "System Output" from "User Interface."

---

## 4. Elevation & Depth

We avoid traditional "drop shadows" which can feel muddy on deep charcoal backgrounds.

- **Tonal Layering:** Depth is conveyed by placing a `surface_container_lowest` card on a `surface_container_low` section. This "soft lift" mimics natural light without requiring artificial shadows.
- **Ambient Glows:** When an element must "float" (e.g., a notification toast), use an ambient glow instead of a black shadow. Use the `primary` token at 8% opacity with a `48px` blur.
- **The "Ghost Border" Fallback:** If a container requires a boundary for accessibility (e.g., an input field), use a "Ghost Border": `outline_variant` (#484847) at **15% opacity**. Never use 100% opaque borders.
- **Reactive Depth:** When HTMX triggers a fragment update, the target container should momentarily transition to `surface_bright` before settling back to its original tier.

---

## 5. Components

### Buttons
- **Primary:** Gradient fill (`primary` to `primary_container`), `on_primary` text. No border.
- **Secondary:** `surface_container_highest` fill. Subtle `outline` ghost border (15% opacity).
- **Tertiary/Ghost:** No fill. Text color is `primary`. On hover, apply a `primary` tint at 10% opacity to the background.

### Developer Logs & Cards
- **The Log Block:** Use `surface_container_highest`. Forbid dividers between log lines; use `0.5rem` vertical spacing (Space Scale) to group entries.
- **Status Chips:** Use `secondary` (#69f6b8) for "Success" and `error` (#ff6e84) for "Failure." Chips should be pill-shaped (`rounded-full`) but use a "Dim" background (`secondary_container`) with high-vibrancy text (`secondary`) for an editorial look.

### Reactive Inputs
- **Text Fields:** `surface_container_low` background with a `bottom-only` ghost border. On focus, the border animates to 100% opacity `primary`.
- **HTMX Indicators:** Use a `0.125rem` height linear progress bar at the very top of the `surface_container` using the `tertiary` (#ffa9df) color to indicate active "swapping" or "loading" states.

---

## 6. Do's and Don'ts

### Do
- **Do** use `space_grotesk` for numbers. Metrics like "Response Time" or "Error Rate" should feel like headlines.
- **Do** allow elements to overlap slightly. A floating log-filter button can overlap the edge of a card to break the rigid grid.
- **Do** use `secondary_dim` for "Success" states to maintain high-contrast legibility against the charcoal background.

### Don't
- **Don't** use pure black (#000000) for anything other than `surface_container_lowest`. It kills the depth of the charcoal theme.
- **Don't** use standard "Blue" for links. Use `primary` (#9fa7ff) to keep the "Quarkus-inspired" vibrancy.
- **Don't** use dividers or lines. If you feel the need for a line, add `16px` of whitespace instead. If it still feels cluttered, shift the background color of one of the sections.