/* @ds-bundle: {"format":3,"namespace":"SahayakDesignSystem_f4b29a","components":[{"name":"Button","sourcePath":"components/buttons/Button.jsx"},{"name":"IconButton","sourcePath":"components/buttons/IconButton.jsx"},{"name":"Avatar","sourcePath":"components/data/Avatar.jsx"},{"name":"Badge","sourcePath":"components/data/Badge.jsx"},{"name":"Chip","sourcePath":"components/data/Chip.jsx"},{"name":"GlyphBubble","sourcePath":"components/data/GlyphBubble.jsx"},{"name":"Sparkline","sourcePath":"components/data/Sparkline.jsx"},{"name":"SegmentedControl","sourcePath":"components/forms/SegmentedControl.jsx"},{"name":"Toggle","sourcePath":"components/forms/Toggle.jsx"},{"name":"Icon","sourcePath":"components/foundation/Icon.jsx"},{"name":"ICON_NAMES","sourcePath":"components/foundation/Icon.jsx"},{"name":"BottomNav","sourcePath":"components/navigation/BottomNav.jsx"},{"name":"ActionRow","sourcePath":"components/surfaces/ActionRow.jsx"},{"name":"Card","sourcePath":"components/surfaces/Card.jsx"}],"sourceHashes":{"components/buttons/Button.jsx":"12294163ffe9","components/buttons/IconButton.jsx":"401e399dad0d","components/data/Avatar.jsx":"2df72697b55f","components/data/Badge.jsx":"d90259fbbbc2","components/data/Chip.jsx":"1d9cc298d48c","components/data/GlyphBubble.jsx":"b6c5dbf6caac","components/data/Sparkline.jsx":"599cacb752c1","components/forms/SegmentedControl.jsx":"0dc99475f13f","components/forms/Toggle.jsx":"db9df69a05c2","components/foundation/Icon.jsx":"fe0e1cde0988","components/navigation/BottomNav.jsx":"b809425a30c2","components/surfaces/ActionRow.jsx":"faea5d42b98d","components/surfaces/Card.jsx":"f77bd13a8bb1","ui_kits/sahayak/kit.jsx":"4482c9dce96d","ui_kits/sahayak/screens-health-asha.jsx":"47c6c294b467","ui_kits/sahayak/screens-today-care.jsx":"813fa13ba324"},"inlinedExternals":[],"unexposedExports":[]} */

(() => {

const __ds_ns = (window.SahayakDesignSystem_f4b29a = window.SahayakDesignSystem_f4b29a || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// components/buttons/Button.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Button
// The system's pill button. Sage by default (the brand action), with ghost
// (hairline-outlined) and clay (warm secondary action) variants. Full-width
// is the norm at the bottom of a screen. Press = a gentle 0.985 shrink.

function Button({
  variant = 'primary',
  // 'primary' | 'ghost' | 'clay'
  full = false,
  disabled = false,
  type = 'button',
  onClick,
  style,
  children,
  ...rest
}) {
  const base = {
    appearance: 'none',
    border: 0,
    padding: '14px 18px',
    borderRadius: 'var(--radius-pill)',
    fontFamily: 'var(--font-sans)',
    fontSize: 15,
    fontWeight: 600,
    cursor: disabled ? 'default' : 'pointer',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    width: full ? '100%' : undefined,
    opacity: disabled ? 0.45 : 1,
    transition: 'transform var(--dur-fast), opacity var(--dur-fast)',
    WebkitTapHighlightColor: 'transparent'
  };
  const skins = {
    primary: {
      background: 'var(--sage)',
      color: 'var(--sage-ink)'
    },
    clay: {
      background: 'var(--clay)',
      color: '#fff'
    },
    ghost: {
      background: 'transparent',
      color: 'var(--ink)',
      border: '0.5px solid var(--line-strong)'
    }
  };
  const [pressed, setPressed] = React.useState(false);
  return /*#__PURE__*/React.createElement("button", _extends({
    type: type,
    disabled: disabled,
    onClick: onClick,
    onPointerDown: () => setPressed(true),
    onPointerUp: () => setPressed(false),
    onPointerLeave: () => setPressed(false),
    style: {
      ...base,
      ...skins[variant],
      transform: pressed && !disabled ? 'scale(0.985)' : 'none',
      ...style
    }
  }, rest), children);
}
Object.assign(__ds_scope, { Button });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/Button.jsx", error: String((e && e.message) || e) }); }

// components/buttons/IconButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — IconButton
// The round 38px control used in topbars (back, bell, message, overflow).
// Cream-elevated with a hairline border. An optional notification `dot`
// sits at the top-right (e.g. unread bell). Children = an <Icon>.

function IconButton({
  dot = false,
  size = 38,
  ariaLabel,
  onClick,
  style,
  children,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("button", _extends({
    "aria-label": ariaLabel,
    onClick: onClick,
    style: {
      appearance: 'none',
      width: size,
      height: size,
      borderRadius: '50%',
      background: 'var(--bg-elev)',
      color: 'var(--ink)',
      border: '0.5px solid var(--line)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      cursor: 'pointer',
      position: 'relative',
      flexShrink: 0,
      WebkitTapHighlightColor: 'transparent',
      ...style
    }
  }, rest), children, dot && /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      top: 8,
      right: 8,
      width: 8,
      height: 8,
      borderRadius: '50%',
      background: 'var(--clay)',
      border: '1.5px solid var(--bg-elev)'
    }
  }));
}
Object.assign(__ds_scope, { IconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/buttons/IconButton.jsx", error: String((e && e.message) || e) }); }

// components/data/Avatar.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Avatar
// A round avatar for people. Three fills: 'striped' (the warm sand
// barber-pole used as a photo placeholder, with a serif initial), 'sage'
// (solid brand) or 'muted' (quiet sand). Initials are set in Instrument
// Serif to echo the wordmark.

function Avatar({
  name = '',
  initial,
  size = 64,
  fill = 'striped',
  style,
  ...rest
}) {
  const ch = (initial || name.trim()[0] || '?').toUpperCase();
  const stripes = 'repeating-linear-gradient(135deg, #cfc6b2 0 6px, #ded6c4 6px 12px)';
  const skins = {
    striped: {
      background: stripes,
      color: 'var(--ink-soft)'
    },
    sage: {
      background: 'var(--sage)',
      color: 'var(--sage-ink)'
    },
    muted: {
      background: 'var(--muted)',
      color: 'var(--ink)'
    }
  };
  return /*#__PURE__*/React.createElement("span", _extends({
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: size,
      height: size,
      borderRadius: '50%',
      fontFamily: 'var(--font-serif)',
      fontSize: size * 0.42,
      lineHeight: 1,
      border: '0.5px solid var(--line)',
      flexShrink: 0,
      ...skins[fill],
      ...style
    }
  }, rest), ch);
}
Object.assign(__ds_scope, { Avatar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/Avatar.jsx", error: String((e && e.message) || e) }); }

// components/data/Badge.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Badge (status pill)
// A small rounded pill with a leading dot, used for wellbeing status:
// "All steady" (ok), "1 to watch" (watch), "Needs a look" (act), or a warm
// note. Tinted fill + matching ink. The `watch` tone gently pulses its dot.
// Set `onAccent` when it sits on a sage/dark hero so it uses cream tints.

const TONES = {
  ok: {
    fill: 'rgba(93,122,78,0.13)',
    ink: 'var(--leaf)',
    dot: 'var(--leaf)'
  },
  watch: {
    fill: 'rgba(201,138,58,0.13)',
    ink: 'var(--amber)',
    dot: 'var(--amber)'
  },
  act: {
    fill: 'rgba(164,73,61,0.13)',
    ink: 'var(--rose)',
    dot: 'var(--rose)'
  },
  warm: {
    fill: 'rgba(182,100,61,0.12)',
    ink: 'var(--clay)',
    dot: 'var(--clay)'
  },
  neutral: {
    fill: 'rgba(29,33,30,0.06)',
    ink: 'var(--ink-soft)',
    dot: 'var(--ink-mute)'
  }
};
function Badge({
  tone = 'ok',
  dot = true,
  onAccent = false,
  style,
  children,
  ...rest
}) {
  const t = TONES[tone] || TONES.ok;
  const skin = onAccent ? {
    fill: 'rgba(246,243,238,0.14)',
    ink: 'var(--sage-ink)',
    dot: tone === 'watch' ? '#f0cd92' : '#a9d3b0'
  } : t;
  return /*#__PURE__*/React.createElement("span", _extends({
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: 7,
      padding: '4px 11px 4px 9px',
      borderRadius: 'var(--radius-pill)',
      fontFamily: 'var(--font-sans)',
      fontSize: 12,
      fontWeight: 600,
      letterSpacing: '0.01em',
      whiteSpace: 'nowrap',
      background: skin.fill,
      color: skin.ink,
      ...style
    }
  }, rest), dot && /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'relative',
      width: 7,
      height: 7,
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: '50%',
      background: skin.dot
    }
  }), tone === 'watch' && /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: '50%',
      background: skin.dot,
      animation: 'sahayakPulse 2.6s ease-out infinite'
    }
  })), children, /*#__PURE__*/React.createElement("style", null, `@keyframes sahayakPulse{0%{transform:scale(1);opacity:.55}70%{transform:scale(3.2);opacity:0}100%{transform:scale(3.2);opacity:0}}`));
}
Object.assign(__ds_scope, { Badge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/Badge.jsx", error: String((e && e.message) || e) }); }

// components/data/Chip.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Chip (vital tile)
// A small stacked tile: a mono UPPERCASE key over a large value with an
// optional small unit. Used in the horizontal "vitals" strip on Today.
// `tone="watch"` turns the value amber to flag the one worth a look.

function Chip({
  label,
  value,
  unit,
  tone = 'ok',
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    style: {
      display: 'inline-flex',
      flexDirection: 'column',
      gap: 4,
      padding: '12px 14px',
      minWidth: 92,
      background: 'var(--bg-elev)',
      border: '0.5px solid var(--line)',
      borderRadius: 'var(--radius-soft)',
      boxShadow: 'var(--shadow-soft)',
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10,
      letterSpacing: '0.06em',
      textTransform: 'uppercase',
      color: 'var(--ink-mute)'
    }
  }, label), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 17,
      fontWeight: 700,
      letterSpacing: '-0.01em',
      color: tone === 'watch' ? 'var(--amber)' : 'var(--ink)'
    }
  }, value, unit ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 11,
      fontWeight: 500,
      color: 'var(--ink-mute)',
      marginLeft: 2
    }
  }, unit) : null));
}
Object.assign(__ds_scope, { Chip });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/Chip.jsx", error: String((e && e.message) || e) }); }

// components/data/GlyphBubble.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — GlyphBubble
// The small rounded-square tile that anchors action rows and provider cards.
// Holds a 1–2 character glyph (e.g. "Rx", "Pt", "AM") set in serif italic.
// Tones: sage / clay / amber / leaf (filled) or muted (sand, default).

const SKINS = {
  sage: {
    background: 'var(--sage)',
    color: 'var(--sage-ink)'
  },
  clay: {
    background: 'var(--clay)',
    color: '#fff'
  },
  amber: {
    background: 'var(--amber)',
    color: '#fff'
  },
  leaf: {
    background: 'var(--leaf)',
    color: '#fff'
  },
  muted: {
    background: 'var(--muted)',
    color: 'var(--ink)'
  }
};
function GlyphBubble({
  glyph,
  tone = 'muted',
  size = 44,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("span", _extends({
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: size,
      height: size,
      borderRadius: 'var(--radius-bubble)',
      fontFamily: 'var(--font-serif)',
      fontStyle: 'italic',
      fontSize: size * 0.5,
      lineHeight: 1,
      flexShrink: 0,
      ...(SKINS[tone] || SKINS.muted),
      ...style
    }
  }, rest), glyph);
}
Object.assign(__ds_scope, { GlyphBubble });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/GlyphBubble.jsx", error: String((e && e.message) || e) }); }

// components/data/Sparkline.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Sparkline
// A tiny 7-point bar chart for trends. Each bar carries a tone: 'ok'
// (sand/muted), 'on' (sage, a "good" day), or 'watch' (amber). Bars are
// normalised 0–1 by height. Used inside insight cards and health metrics.

const BAR_TONE = {
  ok: 'var(--muted)',
  on: 'var(--sage)',
  watch: 'var(--amber)',
  act: 'var(--rose)'
};
function Sparkline({
  bars = [],
  height = 56,
  gap = 4,
  radius = 3,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    style: {
      display: 'flex',
      alignItems: 'flex-end',
      gap,
      height,
      padding: '0 2px',
      ...style
    }
  }, rest), bars.map((b, i) => {
    const v = typeof b === 'number' ? b : b.v;
    const t = typeof b === 'number' ? 'ok' : b.t || 'ok';
    return /*#__PURE__*/React.createElement("span", {
      key: i,
      style: {
        flex: 1,
        minHeight: 6,
        height: `${Math.max(0.08, Math.min(1, v)) * 100}%`,
        background: BAR_TONE[t] || BAR_TONE.ok,
        borderRadius: `${radius}px ${radius}px 0 0`
      }
    });
  }));
}
Object.assign(__ds_scope, { Sparkline });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/Sparkline.jsx", error: String((e && e.message) || e) }); }

// components/forms/SegmentedControl.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — SegmentedControl
// A pill-track tab switch (e.g. 7 days / 30 days / 3 months). The selected
// segment lifts onto a cream pad with a hairline ring + faint shadow; the
// track sits in a quiet sand well. Controlled via `value` + `onChange`.

function SegmentedControl({
  options = [],
  value,
  onChange,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "tablist",
    style: {
      display: 'inline-flex',
      gap: 2,
      padding: 3,
      background: 'rgba(29,33,30,0.05)',
      borderRadius: 'var(--radius-pill)',
      ...style
    }
  }, rest), options.map(o => {
    const opt = typeof o === 'string' ? {
      value: o,
      label: o
    } : o;
    const on = opt.value === value;
    return /*#__PURE__*/React.createElement("button", {
      key: opt.value,
      role: "tab",
      "aria-selected": on,
      onClick: () => onChange && onChange(opt.value),
      style: {
        appearance: 'none',
        border: 0,
        cursor: 'pointer',
        padding: '8px 16px',
        borderRadius: 'var(--radius-pill)',
        fontFamily: 'var(--font-sans)',
        fontSize: 13,
        fontWeight: 600,
        whiteSpace: 'nowrap',
        color: on ? 'var(--ink)' : 'var(--ink-mute)',
        background: on ? 'var(--bg-elev)' : 'transparent',
        boxShadow: on ? '0 1px 2px rgba(0,0,0,0.06), 0 0 0 0.5px var(--line-strong)' : 'none',
        transition: 'color var(--dur-fast), background var(--dur-fast)'
      }
    }, opt.label);
  }));
}
Object.assign(__ds_scope, { SegmentedControl });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/SegmentedControl.jsx", error: String((e && e.message) || e) }); }

// components/forms/Toggle.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Toggle (switch)
// A 36×22 pill switch. Off = sand track; on = accent track. The white knob
// glides 14px with a soft shadow. Controlled: pass `checked` + `onChange`.

function Toggle({
  checked = false,
  onChange,
  ariaLabel,
  disabled = false,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("button", _extends({
    role: "switch",
    "aria-checked": checked,
    "aria-label": ariaLabel,
    disabled: disabled,
    onClick: () => !disabled && onChange && onChange(!checked),
    style: {
      appearance: 'none',
      border: 0,
      position: 'relative',
      width: 36,
      height: 22,
      borderRadius: 'var(--radius-pill)',
      background: checked ? 'var(--accent)' : 'var(--muted)',
      cursor: disabled ? 'default' : 'pointer',
      opacity: disabled ? 0.5 : 1,
      transition: 'background var(--dur-fast)',
      flexShrink: 0,
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      top: 2,
      left: 2,
      width: 18,
      height: 18,
      borderRadius: '50%',
      background: '#fff',
      boxShadow: '0 1px 2px rgba(0,0,0,0.15)',
      transform: checked ? 'translateX(14px)' : 'none',
      transition: 'transform var(--dur-base) var(--ease-soft)'
    }
  }));
}
Object.assign(__ds_scope, { Toggle });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Toggle.jsx", error: String((e && e.message) || e) }); }

// components/foundation/Icon.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Icon
// The brand's bespoke line-icon set: small, abstract, calm. 24×24 grid,
// 1.6 stroke, round caps & joins. No clinical/medical iconography — these
// read as gentle glyphs, never as a hospital chart. Use `currentColor` so
// icons inherit text colour from their context.

function Icon({
  name,
  size = 20,
  color = 'currentColor',
  stroke = 1.6,
  ...rest
}) {
  const p = {
    width: size,
    height: size,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: color,
    strokeWidth: stroke,
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
    ...rest
  };
  switch (name) {
    case 'home':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M4 11 12 4l8 7v9a1 1 0 0 1-1 1h-4v-6h-6v6H5a1 1 0 0 1-1-1z"
      }));
    case 'spark':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3v4M12 17v4M3 12h4M17 12h4M5.6 5.6l2.8 2.8M15.6 15.6l2.8 2.8M5.6 18.4l2.8-2.8M15.6 8.4l2.8-2.8"
      }));
    case 'history':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "12",
        r: "8"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 7v5l3 2"
      }));
    case 'pulse':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M3 12h4l2-5 3 10 2-7 2 4h5"
      }));
    case 'search':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "11",
        cy: "11",
        r: "6"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M20 20l-4-4"
      }));
    case 'pill':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("rect", {
        x: "3",
        y: "9",
        width: "18",
        height: "6",
        rx: "3"
      }), /*#__PURE__*/React.createElement("line", {
        x1: "12",
        y1: "9",
        x2: "12",
        y2: "15"
      }));
    case 'file':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M6 3h8l4 4v14H6z"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M14 3v4h4"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M9 12h6M9 16h6"
      }));
    case 'wallet':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("rect", {
        x: "3",
        y: "6",
        width: "18",
        height: "14",
        rx: "2"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M3 10h18"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "17",
        cy: "15",
        r: "1.2",
        fill: color,
        stroke: "none"
      }));
    case 'person':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "8",
        r: "3.5"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M5 20c0-3.5 3.1-6 7-6s7 2.5 7 6"
      }));
    case 'bell':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M6 16V11a6 6 0 0 1 12 0v5l1.5 2H4.5L6 16z"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M10 20a2 2 0 0 0 4 0"
      }));
    case 'back':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M15 6l-6 6 6 6"
      }));
    case 'chev':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M9 6l6 6-6 6"
      }));
    case 'check':
      return /*#__PURE__*/React.createElement("svg", _extends({}, p, {
        strokeWidth: 2.2
      }), /*#__PURE__*/React.createElement("path", {
        d: "M5 12l5 5L20 7"
      }));
    case 'lock':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("rect", {
        x: "5",
        y: "11",
        width: "14",
        height: "9",
        rx: "2"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M8 11V8a4 4 0 0 1 8 0v3"
      }));
    case 'phone':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a1 1 0 0 1-1 1A15 15 0 0 1 4 5a1 1 0 0 1 1-1z"
      }));
    case 'calendar':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("rect", {
        x: "4",
        y: "5",
        width: "16",
        height: "15",
        rx: "2"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M4 10h16M9 3v4M15 3v4"
      }));
    case 'share':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3v13M7 8l5-5 5 5M5 14v5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-5"
      }));
    case 'message':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M4 6a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-7l-4 4v-4H6a2 2 0 0 1-2-2z"
      }));
    case 'plus':
      return /*#__PURE__*/React.createElement("svg", _extends({}, p, {
        strokeWidth: 2
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 5v14M5 12h14"
      }));
    case 'dots':
      return /*#__PURE__*/React.createElement("svg", _extends({}, p, {
        stroke: "none",
        fill: color
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "6",
        cy: "12",
        r: "1.5"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "12",
        r: "1.5"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "18",
        cy: "12",
        r: "1.5"
      }));
    case 'leaf':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M5 19c10 0 14-6 14-14-5 0-13 1-13 9 0 3 1 5-1 5z"
      }));
    case 'gauge':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M4.5 17a7.5 7.5 0 1 1 15 0"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 16l3.5-3.5"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "16.5",
        r: "1.1",
        fill: color,
        stroke: "none"
      }));
    case 'drop':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3.5s5.5 6 5.5 9.5a5.5 5.5 0 0 1-11 0C6.5 9.5 12 3.5 12 3.5z"
      }));
    case 'moon':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M20 14a8 8 0 0 1-11-11 9 9 0 1 0 11 11z"
      }));
    case 'walk':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "13",
        cy: "4.3",
        r: "1.7"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M13 6.5l-1 5.5-3 6"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 12l3.2 5"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M13 8.5l-3 3M13 8.5l3 2"
      }));
    case 'heart':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 20s-7-4.6-7-11a4.4 4.4 0 0 1 8.4-2.2A4.4 4.4 0 0 1 21 9c0 6.4-7 11-7 11z"
      }));
    case 'lungs':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 4v8"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 8c0-1.8-1.4-3-3-2.8C8 5.4 7 9.5 7 12.5c0 2.8 1.4 3.8 3 3.6 1-.1 1-1.1 1-2.9"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 8c0-1.8 1.4-3 3-2.8C16 5.4 17 9.5 17 12.5c0 2.8-1.4 3.8-3 3.6-1-.1-1-1.1-1-2.9"
      }));
    case 'shield':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3.5l7 2.8V11c0 4.4-3 7.4-7 8.8-4-1.4-7-4.4-7-8.8V6.3z"
      }));
    case 'smile':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "12",
        r: "8"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M8.5 13.5s1.3 2 3.5 2 3.5-2 3.5-2"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "9.3",
        cy: "10",
        r: "0.8",
        fill: color,
        stroke: "none"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "14.7",
        cy: "10",
        r: "0.8",
        fill: color,
        stroke: "none"
      }));
    default:
      return null;
  }
}
const ICON_NAMES = ['home', 'spark', 'history', 'pulse', 'search', 'pill', 'file', 'wallet', 'person', 'bell', 'back', 'chev', 'check', 'lock', 'phone', 'calendar', 'share', 'message', 'plus', 'dots', 'leaf', 'gauge', 'drop', 'moon', 'walk', 'heart', 'lungs', 'shield', 'smile'];
Object.assign(__ds_scope, { Icon, ICON_NAMES });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/foundation/Icon.jsx", error: String((e && e.message) || e) }); }

// components/navigation/BottomNav.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — BottomNav
// The app's tab bar. Four tabs by default — Today, Care, Health, Asha — each
// an Icon over a small label. The active tab tints to --accent and thickens
// its icon stroke. Pass your own `tabs` to reuse the bar elsewhere.

const DEFAULT_TABS = [{
  id: 'home',
  label: 'Today',
  icon: 'home'
}, {
  id: 'actions',
  label: 'Care',
  icon: 'spark'
}, {
  id: 'health',
  label: 'Health',
  icon: 'pulse'
}, {
  id: 'wellness',
  label: 'Wellness',
  icon: 'leaf'
}, {
  id: 'profile',
  label: 'Ranjit',
  icon: 'person'
}];
function BottomNav({
  active,
  onNav,
  tabs = DEFAULT_TABS,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("nav", _extends({
    style: {
      display: 'flex',
      gap: 4,
      padding: '10px 18px 8px',
      background: 'var(--bg)',
      borderTop: '0.5px solid var(--line)',
      ...style
    }
  }, rest), tabs.map(t => {
    const on = active === t.id;
    return /*#__PURE__*/React.createElement("button", {
      key: t.id,
      "aria-current": on ? 'page' : undefined,
      onClick: () => onNav && onNav(t.id),
      style: {
        appearance: 'none',
        border: 0,
        background: 'transparent',
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 4,
        padding: '8px 4px',
        cursor: 'pointer',
        fontFamily: 'var(--font-sans)',
        fontSize: 11,
        fontWeight: 600,
        letterSpacing: '0.02em',
        borderRadius: 12,
        color: on ? 'var(--accent)' : 'var(--ink-mute)'
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
      name: t.icon,
      size: 20,
      stroke: on ? 1.8 : 1.4
    }), /*#__PURE__*/React.createElement("span", null, t.label));
  }));
}
Object.assign(__ds_scope, { BottomNav });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/BottomNav.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/ActionRow.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — ActionRow
// A tappable row: glyph bubble · title + subtitle · trailing chevron. The
// workhorse of Care (suggested actions, services) and lists throughout.
// Composes GlyphBubble for the leading tile and Icon for the chevron.

function ActionRow({
  glyph,
  glyphTone = 'muted',
  title,
  sub,
  trailing,
  onClick,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("button", _extends({
    onClick: onClick,
    style: {
      appearance: 'none',
      width: '100%',
      textAlign: 'left',
      display: 'flex',
      alignItems: 'center',
      gap: 14,
      padding: '16px 18px',
      background: 'var(--bg-elev)',
      border: '0.5px solid var(--line)',
      borderRadius: 'var(--radius-soft)',
      cursor: 'pointer',
      fontFamily: 'var(--font-sans)',
      WebkitTapHighlightColor: 'transparent',
      ...style
    }
  }, rest), glyph != null && /*#__PURE__*/React.createElement(__ds_scope.GlyphBubble, {
    glyph: glyph,
    tone: glyphTone
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'block',
      fontSize: 15,
      fontWeight: 600,
      color: 'var(--ink)',
      lineHeight: 1.3
    }
  }, title), sub && /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'block',
      fontSize: 13,
      color: 'var(--ink-mute)',
      marginTop: 2,
      lineHeight: 1.3
    }
  }, sub)), trailing != null ? trailing : /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "chev",
    size: 18,
    color: "var(--ink-mute)"
  }));
}
Object.assign(__ds_scope, { ActionRow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/ActionRow.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/Card.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak — Card
// The system's surface. Three variants:
//   · soft (default)    — cream-elevated, 22px radius, soft sage-tinted shadow
//   · clinical          — tighter 12px radius, stronger hairline, no shadow,
//                          for dense record/data lists
//   · hero              — the filled sage "statement" slab, cream ink
// Pass `as` to change the element (e.g. 'button' for a tappable card).

function Card({
  variant = 'soft',
  as = 'div',
  style,
  children,
  ...rest
}) {
  const El = as;
  const base = {
    position: 'relative',
    overflow: 'hidden',
    borderRadius: 'var(--radius-card)',
    fontFamily: 'var(--font-sans)'
  };
  const variants = {
    soft: {
      background: 'var(--bg-elev)',
      border: '0.5px solid var(--line)',
      boxShadow: 'var(--shadow-soft)',
      padding: '20px 20px 18px'
    },
    clinical: {
      background: 'var(--bg-elev)',
      border: '1px solid var(--line-strong)',
      borderRadius: 'var(--radius-clinical)',
      boxShadow: 'none',
      padding: '16px 18px 14px'
    },
    hero: {
      background: 'var(--surface-hero)',
      color: 'var(--sage-ink)',
      border: 0,
      boxShadow: 'var(--shadow-hero)',
      padding: '26px 24px 24px'
    }
  };
  return /*#__PURE__*/React.createElement(El, _extends({
    style: {
      ...base,
      ...variants[variant],
      ...style
    }
  }, rest), children);
}
Object.assign(__ds_scope, { Card });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/Card.jsx", error: String((e && e.message) || e) }); }

// ui_kits/sahayak/kit.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
// Sahayak UI kit — primitives + app chrome.
// These mirror the published components/* (same markup & tokens) but are
// declared locally and attached to window so the kit renders standalone
// (in preview, in the Design System tab, and offline in the skill download).

const {
  useState
} = React;

// ─── Icon — the bespoke 29-glyph line set ──────────────────────────────────
function Icon({
  name,
  size = 20,
  color = 'currentColor',
  stroke = 1.6,
  ...rest
}) {
  const p = {
    width: size,
    height: size,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: color,
    strokeWidth: stroke,
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
    ...rest
  };
  switch (name) {
    case 'home':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M4 11 12 4l8 7v9a1 1 0 0 1-1 1h-4v-6h-6v6H5a1 1 0 0 1-1-1z"
      }));
    case 'spark':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3v4M12 17v4M3 12h4M17 12h4M5.6 5.6l2.8 2.8M15.6 15.6l2.8 2.8M5.6 18.4l2.8-2.8M15.6 8.4l2.8-2.8"
      }));
    case 'pulse':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M3 12h4l2-5 3 10 2-7 2 4h5"
      }));
    case 'person':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "8",
        r: "3.5"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M5 20c0-3.5 3.1-6 7-6s7 2.5 7 6"
      }));
    case 'bell':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M6 16V11a6 6 0 0 1 12 0v5l1.5 2H4.5L6 16z"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M10 20a2 2 0 0 0 4 0"
      }));
    case 'message':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M4 6a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-7l-4 4v-4H6a2 2 0 0 1-2-2z"
      }));
    case 'back':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M15 6l-6 6 6 6"
      }));
    case 'chev':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M9 6l6 6-6 6"
      }));
    case 'check':
      return /*#__PURE__*/React.createElement("svg", _extends({}, p, {
        strokeWidth: 2.2
      }), /*#__PURE__*/React.createElement("path", {
        d: "M5 12l5 5L20 7"
      }));
    case 'plus':
      return /*#__PURE__*/React.createElement("svg", _extends({}, p, {
        strokeWidth: 2
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 5v14M5 12h14"
      }));
    case 'dots':
      return /*#__PURE__*/React.createElement("svg", _extends({}, p, {
        stroke: "none",
        fill: color
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "6",
        cy: "12",
        r: "1.5"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "12",
        r: "1.5"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "18",
        cy: "12",
        r: "1.5"
      }));
    case 'gauge':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M4.5 17a7.5 7.5 0 1 1 15 0"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 16l3.5-3.5"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "16.5",
        r: "1.1",
        fill: color,
        stroke: "none"
      }));
    case 'drop':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3.5s5.5 6 5.5 9.5a5.5 5.5 0 0 1-11 0C6.5 9.5 12 3.5 12 3.5z"
      }));
    case 'moon':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M20 14a8 8 0 0 1-11-11 9 9 0 1 0 11 11z"
      }));
    case 'walk':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "13",
        cy: "4.3",
        r: "1.7"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M13 6.5l-1 5.5-3 6"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 12l3.2 5"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M13 8.5l-3 3M13 8.5l3 2"
      }));
    case 'heart':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 20s-7-4.6-7-11a4.4 4.4 0 0 1 8.4-2.2A4.4 4.4 0 0 1 21 9c0 6.4-7 11-7 11z"
      }));
    case 'pill':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("rect", {
        x: "3",
        y: "9",
        width: "18",
        height: "6",
        rx: "3"
      }), /*#__PURE__*/React.createElement("line", {
        x1: "12",
        y1: "9",
        x2: "12",
        y2: "15"
      }));
    case 'lungs':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 4v8"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 8c0-1.8-1.4-3-3-2.8C8 5.4 7 9.5 7 12.5c0 2.8 1.4 3.8 3 3.6 1-.1 1-1.1 1-2.9"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M12 8c0-1.8 1.4-3 3-2.8C16 5.4 17 9.5 17 12.5c0 2.8-1.4 3.8-3 3.6-1-.1-1-1.1-1-2.9"
      }));
    case 'shield':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3.5l7 2.8V11c0 4.4-3 7.4-7 8.8-4-1.4-7-4.4-7-8.8V6.3z"
      }));
    case 'smile':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("circle", {
        cx: "12",
        cy: "12",
        r: "8"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M8.5 13.5s1.3 2 3.5 2 3.5-2 3.5-2"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "9.3",
        cy: "10",
        r: "0.8",
        fill: color,
        stroke: "none"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "14.7",
        cy: "10",
        r: "0.8",
        fill: color,
        stroke: "none"
      }));
    case 'share':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M12 3v13M7 8l5-5 5 5M5 14v5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-5"
      }));
    case 'calendar':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("rect", {
        x: "4",
        y: "5",
        width: "16",
        height: "15",
        rx: "2"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M4 10h16M9 3v4M15 3v4"
      }));
    case 'file':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M6 3h8l4 4v14H6z"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M14 3v4h4"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M9 12h6M9 16h6"
      }));
    case 'wallet':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("rect", {
        x: "3",
        y: "6",
        width: "18",
        height: "14",
        rx: "2"
      }), /*#__PURE__*/React.createElement("path", {
        d: "M3 10h18"
      }), /*#__PURE__*/React.createElement("circle", {
        cx: "17",
        cy: "15",
        r: "1.2",
        fill: color,
        stroke: "none"
      }));
    case 'phone':
      return /*#__PURE__*/React.createElement("svg", p, /*#__PURE__*/React.createElement("path", {
        d: "M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a1 1 0 0 1-1 1A15 15 0 0 1 4 5a1 1 0 0 1 1-1z"
      }));
    default:
      return null;
  }
}

// ─── Buttons ────────────────────────────────────────────────────────────────
function Button({
  variant = 'primary',
  full,
  disabled,
  onClick,
  style,
  children
}) {
  const [pressed, setPressed] = useState(false);
  const skins = {
    primary: {
      background: 'var(--sage)',
      color: 'var(--sage-ink)'
    },
    clay: {
      background: 'var(--clay)',
      color: '#fff'
    },
    ghost: {
      background: 'transparent',
      color: 'var(--ink)',
      border: '0.5px solid var(--line-strong)'
    }
  };
  return /*#__PURE__*/React.createElement("button", {
    onClick: onClick,
    disabled: disabled,
    onPointerDown: () => setPressed(true),
    onPointerUp: () => setPressed(false),
    onPointerLeave: () => setPressed(false),
    style: {
      appearance: 'none',
      border: 0,
      padding: '14px 18px',
      borderRadius: 'var(--radius-pill)',
      fontFamily: 'var(--font-sans)',
      fontSize: 15,
      fontWeight: 600,
      cursor: disabled ? 'default' : 'pointer',
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: 8,
      width: full ? '100%' : undefined,
      opacity: disabled ? 0.45 : 1,
      transition: 'transform .18s',
      transform: pressed && !disabled ? 'scale(.985)' : 'none',
      WebkitTapHighlightColor: 'transparent',
      ...skins[variant],
      ...style
    }
  }, children);
}
function IconButton({
  dot,
  ariaLabel,
  onClick,
  children
}) {
  return /*#__PURE__*/React.createElement("button", {
    "aria-label": ariaLabel,
    onClick: onClick,
    style: {
      appearance: 'none',
      width: 38,
      height: 38,
      borderRadius: '50%',
      background: 'var(--bg-elev)',
      color: 'var(--ink)',
      border: '0.5px solid var(--line)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      cursor: 'pointer',
      position: 'relative',
      flexShrink: 0,
      WebkitTapHighlightColor: 'transparent'
    }
  }, children, dot && /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      top: 8,
      right: 8,
      width: 8,
      height: 8,
      borderRadius: '50%',
      background: 'var(--clay)',
      border: '1.5px solid var(--bg-elev)'
    }
  }));
}

// ─── Cards & rows ────────────────────────────────────────────────────────────
function Card({
  variant = 'soft',
  as = 'div',
  style,
  children,
  ...rest
}) {
  const El = as;
  const v = {
    soft: {
      background: 'var(--bg-elev)',
      border: '0.5px solid var(--line)',
      boxShadow: 'var(--shadow-soft)',
      padding: '20px 20px 18px'
    },
    clinical: {
      background: 'var(--bg-elev)',
      border: '1px solid var(--line-strong)',
      borderRadius: 'var(--radius-clinical)',
      boxShadow: 'none',
      padding: '16px 18px 14px'
    },
    hero: {
      background: 'var(--surface-hero)',
      color: 'var(--sage-ink)',
      border: 0,
      boxShadow: 'var(--shadow-hero)',
      padding: '20px 22px 16px'
    }
  }[variant];
  return /*#__PURE__*/React.createElement(El, _extends({
    style: {
      position: 'relative',
      overflow: 'hidden',
      borderRadius: 'var(--radius-card)',
      fontFamily: 'var(--font-sans)',
      ...v,
      ...style
    }
  }, rest), children);
}
function GlyphBubble({
  glyph,
  tone = 'muted',
  size = 44
}) {
  const s = {
    sage: {
      background: 'var(--sage)',
      color: 'var(--sage-ink)'
    },
    clay: {
      background: 'var(--clay)',
      color: '#fff'
    },
    amber: {
      background: 'var(--amber)',
      color: '#fff'
    },
    leaf: {
      background: 'var(--leaf)',
      color: '#fff'
    },
    muted: {
      background: 'var(--muted)',
      color: 'var(--ink)'
    }
  }[tone];
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: size,
      height: size,
      borderRadius: 'var(--radius-bubble)',
      fontFamily: 'var(--font-serif)',
      fontStyle: 'italic',
      fontSize: size * 0.5,
      lineHeight: 1,
      flexShrink: 0,
      ...s
    }
  }, glyph);
}
function ActionRow({
  glyph,
  glyphTone = 'muted',
  title,
  sub,
  onClick
}) {
  return /*#__PURE__*/React.createElement("button", {
    onClick: onClick,
    style: {
      appearance: 'none',
      width: '100%',
      textAlign: 'left',
      display: 'flex',
      alignItems: 'center',
      gap: 14,
      padding: '16px 18px',
      background: 'var(--bg-elev)',
      border: '0.5px solid var(--line)',
      borderRadius: 'var(--radius-soft)',
      cursor: 'pointer',
      fontFamily: 'var(--font-sans)',
      WebkitTapHighlightColor: 'transparent'
    }
  }, glyph != null && /*#__PURE__*/React.createElement(GlyphBubble, {
    glyph: glyph,
    tone: glyphTone
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'block',
      fontSize: 15,
      fontWeight: 600,
      color: 'var(--ink)',
      lineHeight: 1.3
    }
  }, title), sub && /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'block',
      fontSize: 13,
      color: 'var(--ink-mute)',
      marginTop: 2,
      lineHeight: 1.3
    }
  }, sub)), /*#__PURE__*/React.createElement(Icon, {
    name: "chev",
    size: 18,
    color: "var(--ink-mute)"
  }));
}

// ─── Badge · Chip · Sparkline · Avatar ───────────────────────────────────────
function Badge({
  tone = 'ok',
  dot = true,
  onAccent,
  children,
  style
}) {
  const T = {
    ok: {
      f: 'rgba(93,122,78,.13)',
      i: 'var(--leaf)',
      d: 'var(--leaf)'
    },
    watch: {
      f: 'rgba(201,138,58,.13)',
      i: 'var(--amber)',
      d: 'var(--amber)'
    },
    act: {
      f: 'rgba(164,73,61,.13)',
      i: 'var(--rose)',
      d: 'var(--rose)'
    },
    warm: {
      f: 'rgba(182,100,61,.12)',
      i: 'var(--clay)',
      d: 'var(--clay)'
    }
  }[tone];
  const skin = onAccent ? {
    f: 'rgba(246,243,238,.14)',
    i: 'var(--sage-ink)',
    d: tone === 'watch' ? '#f0cd92' : '#a9d3b0'
  } : T;
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: 7,
      padding: '4px 11px 4px 9px',
      borderRadius: 'var(--radius-pill)',
      fontFamily: 'var(--font-sans)',
      fontSize: 12,
      fontWeight: 600,
      whiteSpace: 'nowrap',
      background: skin.f,
      color: skin.i,
      ...style
    }
  }, dot && /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'relative',
      width: 7,
      height: 7,
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: '50%',
      background: skin.d
    }
  }), tone === 'watch' && /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: '50%',
      background: skin.d,
      animation: 'skPulse 2.6s ease-out infinite'
    }
  })), children);
}
function Chip({
  label,
  value,
  unit,
  tone = 'ok'
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'inline-flex',
      flexDirection: 'column',
      gap: 4,
      padding: '12px 14px',
      minWidth: 92,
      background: 'var(--bg-elev)',
      border: '0.5px solid var(--line)',
      borderRadius: 'var(--radius-soft)',
      boxShadow: 'var(--shadow-soft)',
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10,
      letterSpacing: '.06em',
      textTransform: 'uppercase',
      color: 'var(--ink-mute)'
    }
  }, label), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 17,
      fontWeight: 700,
      letterSpacing: '-.01em',
      color: tone === 'watch' ? 'var(--amber)' : 'var(--ink)'
    }
  }, value, unit ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 11,
      fontWeight: 500,
      color: 'var(--ink-mute)',
      marginLeft: 2
    }
  }, unit) : null));
}
function Sparkline({
  bars = [],
  height = 56,
  gap = 4
}) {
  const C = {
    ok: 'var(--muted)',
    on: 'var(--sage)',
    watch: 'var(--amber)',
    act: 'var(--rose)'
  };
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'flex-end',
      gap,
      height,
      padding: '0 2px'
    }
  }, bars.map((b, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    style: {
      flex: 1,
      minHeight: 6,
      height: Math.max(.08, Math.min(1, b.v)) * 100 + '%',
      background: C[b.t] || C.ok,
      borderRadius: '3px 3px 0 0'
    }
  })));
}
function Avatar({
  name = '',
  initial,
  size = 64,
  fill = 'striped'
}) {
  const ch = (initial || name.trim()[0] || '?').toUpperCase();
  const stripes = 'repeating-linear-gradient(135deg,#cfc6b2 0 6px,#ded6c4 6px 12px)';
  const s = {
    striped: {
      background: stripes,
      color: 'var(--ink-soft)'
    },
    sage: {
      background: 'var(--sage)',
      color: 'var(--sage-ink)'
    },
    muted: {
      background: 'var(--muted)',
      color: 'var(--ink)'
    }
  }[fill];
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: size,
      height: size,
      borderRadius: '50%',
      fontFamily: 'var(--font-serif)',
      fontSize: size * 0.42,
      lineHeight: 1,
      border: '0.5px solid var(--line)',
      flexShrink: 0,
      ...s
    }
  }, ch);
}
function SegmentedControl({
  options,
  value,
  onChange
}) {
  return /*#__PURE__*/React.createElement("div", {
    role: "tablist",
    style: {
      display: 'inline-flex',
      gap: 2,
      padding: 3,
      background: 'rgba(29,33,30,.05)',
      borderRadius: 'var(--radius-pill)'
    }
  }, options.map(o => {
    const on = o.value === value;
    return /*#__PURE__*/React.createElement("button", {
      key: o.value,
      onClick: () => onChange(o.value),
      style: {
        appearance: 'none',
        border: 0,
        cursor: 'pointer',
        padding: '8px 16px',
        borderRadius: 'var(--radius-pill)',
        fontFamily: 'var(--font-sans)',
        fontSize: 13,
        fontWeight: 600,
        whiteSpace: 'nowrap',
        color: on ? 'var(--ink)' : 'var(--ink-mute)',
        background: on ? 'var(--bg-elev)' : 'transparent',
        boxShadow: on ? '0 1px 2px rgba(0,0,0,.06),0 0 0 .5px var(--line-strong)' : 'none',
        transition: 'color .18s,background .18s'
      }
    }, o.label);
  }));
}
function Toggle({
  checked,
  onChange,
  ariaLabel
}) {
  return /*#__PURE__*/React.createElement("button", {
    role: "switch",
    "aria-checked": checked,
    "aria-label": ariaLabel,
    onClick: () => onChange(!checked),
    style: {
      appearance: 'none',
      border: 0,
      position: 'relative',
      width: 36,
      height: 22,
      borderRadius: 'var(--radius-pill)',
      background: checked ? 'var(--accent)' : 'var(--muted)',
      cursor: 'pointer',
      transition: 'background .18s',
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      top: 2,
      left: 2,
      width: 18,
      height: 18,
      borderRadius: '50%',
      background: '#fff',
      boxShadow: '0 1px 2px rgba(0,0,0,.15)',
      transform: checked ? 'translateX(14px)' : 'none',
      transition: 'transform .22s var(--ease-soft)'
    }
  }));
}

// ─── App chrome ───────────────────────────────────────────────────────────────
function StatusBar() {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      height: 40,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 22px 0 24px',
      fontFamily: 'var(--font-sans)',
      fontSize: 14,
      fontWeight: 600,
      color: 'var(--ink)',
      position: 'relative',
      flexShrink: 0,
      letterSpacing: '.01em'
    }
  }, /*#__PURE__*/React.createElement("span", null, "9:30"), /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      left: '50%',
      top: 10,
      transform: 'translateX(-50%)',
      width: 18,
      height: 18,
      borderRadius: '50%',
      background: '#0d0d0d'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 5
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: "14",
    height: "14",
    viewBox: "0 0 16 16"
  }, /*#__PURE__*/React.createElement("path", {
    d: "M8 13.3L.67 5.97a10.37 10.37 0 0114.66 0L8 13.3z",
    fill: "currentColor"
  })), /*#__PURE__*/React.createElement("svg", {
    width: "14",
    height: "14",
    viewBox: "0 0 16 16"
  }, /*#__PURE__*/React.createElement("path", {
    d: "M14.67 14.67V1.33L1.33 14.67h13.34z",
    fill: "currentColor"
  })), /*#__PURE__*/React.createElement("svg", {
    width: "22",
    height: "11",
    viewBox: "0 0 22 11"
  }, /*#__PURE__*/React.createElement("rect", {
    x: "0.5",
    y: "0.5",
    width: "18",
    height: "10",
    rx: "2.5",
    fill: "none",
    stroke: "currentColor",
    strokeOpacity: "0.45"
  }), /*#__PURE__*/React.createElement("rect", {
    x: "2",
    y: "2",
    width: "13",
    height: "7",
    rx: "1",
    fill: "currentColor"
  }), /*#__PURE__*/React.createElement("rect", {
    x: "19.5",
    y: "3.5",
    width: "2",
    height: "4",
    rx: "1",
    fill: "currentColor",
    fillOpacity: "0.45"
  }))));
}
function GestureBar() {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      height: 24,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement("i", {
    style: {
      width: 124,
      height: 4,
      borderRadius: 4,
      background: 'var(--ink)',
      opacity: .32,
      display: 'block'
    }
  }));
}
function TopBar({
  greet,
  onBack,
  onBell,
  onMessage,
  hasUnread
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '12px 22px 14px',
      gap: 12
    }
  }, onBack ? /*#__PURE__*/React.createElement(IconButton, {
    ariaLabel: "Back",
    onClick: onBack
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "back",
    size: 18,
    stroke: 1.8
  })) : /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 13,
      color: 'var(--ink-mute)',
      letterSpacing: '.04em',
      textTransform: 'uppercase',
      fontWeight: 600
    }
  }, greet), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 8
    }
  }, onMessage && /*#__PURE__*/React.createElement(IconButton, {
    ariaLabel: "Family group",
    onClick: onMessage
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "message",
    size: 18
  })), onBell && /*#__PURE__*/React.createElement(IconButton, {
    ariaLabel: "Notifications",
    dot: hasUnread,
    onClick: onBell
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "bell",
    size: 18
  }))));
}
function BottomNav({
  active,
  onNav
}) {
  const tabs = [{
    id: 'home',
    label: 'Today',
    icon: 'home'
  }, {
    id: 'care',
    label: 'Care',
    icon: 'spark'
  }, {
    id: 'health',
    label: 'Health',
    icon: 'pulse'
  }, {
    id: 'asha',
    label: 'Asha',
    icon: 'person'
  }];
  return /*#__PURE__*/React.createElement("nav", {
    style: {
      display: 'flex',
      gap: 4,
      padding: '10px 18px 8px',
      background: 'var(--bg)',
      borderTop: '0.5px solid var(--line)',
      flexShrink: 0
    }
  }, tabs.map(t => {
    const on = active === t.id;
    return /*#__PURE__*/React.createElement("button", {
      key: t.id,
      onClick: () => onNav(t.id),
      style: {
        appearance: 'none',
        border: 0,
        background: 'transparent',
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 4,
        padding: '8px 4px',
        cursor: 'pointer',
        fontFamily: 'var(--font-sans)',
        fontSize: 11,
        fontWeight: 600,
        letterSpacing: '.02em',
        borderRadius: 12,
        color: on ? 'var(--accent)' : 'var(--ink-mute)'
      }
    }, /*#__PURE__*/React.createElement(Icon, {
      name: t.icon,
      size: 20,
      stroke: on ? 1.8 : 1.4
    }), /*#__PURE__*/React.createElement("span", null, t.label));
  }));
}

// shared eyebrow helper
function Eyebrow({
  children,
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 11,
      letterSpacing: '.08em',
      textTransform: 'uppercase',
      color: 'var(--ink-mute)',
      ...style
    }
  }, children);
}
Object.assign(window, {
  Icon,
  Button,
  IconButton,
  Card,
  GlyphBubble,
  ActionRow,
  Badge,
  Chip,
  Sparkline,
  Avatar,
  SegmentedControl,
  Toggle,
  StatusBar,
  GestureBar,
  TopBar,
  BottomNav,
  Eyebrow
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/sahayak/kit.jsx", error: String((e && e.message) || e) }); }

// ui_kits/sahayak/screens-health-asha.jsx
try { (() => {
// Sahayak UI kit — Health & Asha screens.
const {
  useState: useStateB
} = React;
const METRICS = {
  '7d': [{
    id: 'bp',
    icon: 'gauge',
    label: 'Blood pressure',
    value: '128/82',
    unit: 'avg',
    tone: 'watch',
    foot: 'Two slightly elevated mornings — worth a quick BP review.',
    bars: [{
      v: .5,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .6,
      t: 'ok'
    }, {
      v: .65,
      t: 'ok'
    }, {
      v: .6,
      t: 'ok'
    }, {
      v: .8,
      t: 'watch'
    }, {
      v: .9,
      t: 'watch'
    }]
  }, {
    id: 'glucose',
    icon: 'drop',
    label: 'Blood sugar',
    value: '108',
    unit: 'mg/dL fasting',
    tone: 'ok',
    foot: 'In her usual range every morning this week.',
    bars: [{
      v: .4,
      t: 'ok'
    }, {
      v: .45,
      t: 'ok'
    }, {
      v: .42,
      t: 'ok'
    }, {
      v: .5,
      t: 'ok'
    }, {
      v: .45,
      t: 'ok'
    }, {
      v: .48,
      t: 'ok'
    }, {
      v: .46,
      t: 'ok'
    }]
  }, {
    id: 'sleep',
    icon: 'moon',
    label: 'Sleep',
    value: '7h 04m',
    unit: 'avg',
    tone: 'ok',
    foot: 'Her steadiest week in a fortnight.',
    bars: [{
      v: .5,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .65,
      t: 'ok'
    }, {
      v: .7,
      t: 'ok'
    }, {
      v: .75,
      t: 'ok'
    }, {
      v: .78,
      t: 'ok'
    }, {
      v: .85,
      t: 'on'
    }]
  }, {
    id: 'move',
    icon: 'walk',
    label: 'Movement',
    value: '1,540',
    unit: 'steps/day',
    tone: 'ok',
    foot: 'Both daily walks, every day. Knee comfortable.',
    bars: [{
      v: .4,
      t: 'ok'
    }, {
      v: .5,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .5,
      t: 'ok'
    }, {
      v: .6,
      t: 'ok'
    }, {
      v: .7,
      t: 'ok'
    }, {
      v: .72,
      t: 'on'
    }]
  }, {
    id: 'meds',
    icon: 'pill',
    label: 'Medication',
    value: '13/14',
    unit: 'doses',
    tone: 'ok',
    foot: 'One evening dose 40 minutes late on Tuesday.',
    bars: [{
      v: 1,
      t: 'on'
    }, {
      v: 1,
      t: 'on'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: 1,
      t: 'on'
    }, {
      v: 1,
      t: 'on'
    }, {
      v: .7,
      t: 'ok'
    }, {
      v: 1,
      t: 'on'
    }]
  }],
  '30d': [{
    id: 'bp',
    icon: 'gauge',
    label: 'Blood pressure',
    value: '126/81',
    unit: 'avg',
    tone: 'watch',
    foot: 'Slow drift upward over the last fortnight.',
    bars: [{
      v: .5,
      t: 'ok'
    }, {
      v: .52,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .58,
      t: 'ok'
    }, {
      v: .62,
      t: 'ok'
    }, {
      v: .75,
      t: 'watch'
    }, {
      v: .8,
      t: 'watch'
    }]
  }, {
    id: 'move',
    icon: 'walk',
    label: 'Movement',
    value: '1,260',
    unit: 'steps/day',
    tone: 'watch',
    foot: 'Quieter than usual — the rainy week kept her in.',
    bars: [{
      v: .7,
      t: 'ok'
    }, {
      v: .6,
      t: 'ok'
    }, {
      v: .5,
      t: 'ok'
    }, {
      v: .4,
      t: 'watch'
    }, {
      v: .45,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .6,
      t: 'ok'
    }]
  }, {
    id: 'sleep',
    icon: 'moon',
    label: 'Sleep',
    value: '6h 48m',
    unit: 'avg',
    tone: 'ok',
    foot: 'A few restless nights mid-month, otherwise calm.',
    bars: [{
      v: .6,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .5,
      t: 'ok'
    }, {
      v: .7,
      t: 'ok'
    }, {
      v: .65,
      t: 'ok'
    }, {
      v: .72,
      t: 'ok'
    }, {
      v: .78,
      t: 'ok'
    }]
  }],
  '3mo': [{
    id: 'bp',
    icon: 'gauge',
    label: 'Blood pressure',
    value: '125/80',
    unit: 'avg',
    tone: 'act',
    foot: 'Three months above her February baseline — time to reassess dose.',
    bars: [{
      v: .45,
      t: 'ok'
    }, {
      v: .5,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .6,
      t: 'watch'
    }, {
      v: .65,
      t: 'watch'
    }, {
      v: .72,
      t: 'watch'
    }, {
      v: .8,
      t: 'watch'
    }]
  }, {
    id: 'move',
    icon: 'walk',
    label: 'Movement',
    value: '1,380',
    unit: 'steps/day',
    tone: 'ok',
    foot: 'Steady recovery from her knee surgery in January.',
    bars: [{
      v: .3,
      t: 'ok'
    }, {
      v: .4,
      t: 'ok'
    }, {
      v: .5,
      t: 'ok'
    }, {
      v: .55,
      t: 'ok'
    }, {
      v: .6,
      t: 'ok'
    }, {
      v: .65,
      t: 'ok'
    }, {
      v: .7,
      t: 'ok'
    }]
  }]
};
function HealthScreen({
  onBack
}) {
  const [range, setRange] = useStateB('7d');
  const meta = {
    '7d': 'Past 7 days · vs. fortnight before',
    '30d': 'Past 30 days · vs. the month before',
    '3mo': 'Past 3 months · since February'
  }[range];
  const list = METRICS[range];
  const watch = list.filter(m => m.tone !== 'ok');
  const steady = list.filter(m => m.tone === 'ok');
  return /*#__PURE__*/React.createElement("div", {
    className: "screen"
  }, /*#__PURE__*/React.createElement(TopBar, {
    onBack: onBack
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '8px 22px 24px'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    style: {
      paddingLeft: 2
    }
  }, "Mom's health"), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "'Manrope', system-ui, sans-serif",
      fontSize: 28,
      lineHeight: 1.1,
      color: 'var(--ink)',
      margin: '8px 0 0'
    }
  }, "Mostly steady. ", /*#__PURE__*/React.createElement("em", {
    style: {
      fontStyle: 'italic',
      color: 'var(--accent)'
    }
  }, watch.length, " worth watching.")), /*#__PURE__*/React.createElement("p", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 15,
      lineHeight: 1.45,
      color: 'var(--ink-soft)',
      margin: '8px 0 18px'
    }
  }, "A quiet snapshot \u2014 vitals, sensors, medication, labs and prescriptions, all in one place."), /*#__PURE__*/React.createElement(SegmentedControl, {
    value: range,
    onChange: setRange,
    options: [{
      value: '7d',
      label: '7 days'
    }, {
      value: '30d',
      label: '30 days'
    }, {
      value: '3mo',
      label: '3 months'
    }]
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10.5,
      letterSpacing: '.04em',
      textTransform: 'uppercase',
      color: 'var(--ink-mute)',
      margin: '14px 0 6px'
    }
  }, meta), watch.length > 0 && /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 10
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    style: {
      color: 'var(--amber)',
      marginBottom: 10
    }
  }, "\u25CF Worth watching"), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 12
    }
  }, watch.map(m => /*#__PURE__*/React.createElement(MetricCard, {
    key: m.id,
    m: m
  })))), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 20
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    style: {
      marginBottom: 10
    }
  }, "\u25CF Steady"), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 12
    }
  }, steady.map(m => /*#__PURE__*/React.createElement(MetricCard, {
    key: m.id,
    m: m
  })))), /*#__PURE__*/React.createElement(Card, {
    variant: "soft",
    as: "button",
    style: {
      width: '100%',
      textAlign: 'left',
      marginTop: 16,
      cursor: 'pointer',
      display: 'flex',
      alignItems: 'center',
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(GlyphBubble, {
    glyph: /*#__PURE__*/React.createElement(Icon, {
      name: "wallet",
      size: 20,
      color: "var(--sage-ink)"
    }),
    tone: "sage",
    size: 40
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 15,
      fontWeight: 600,
      color: 'var(--ink)'
    }
  }, "Health Wallet"), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 13,
      color: 'var(--ink-mute)',
      marginTop: 2
    }
  }, "18 documents \xB7 6 medicines")), /*#__PURE__*/React.createElement(Icon, {
    name: "chev",
    size: 18,
    color: "var(--ink-mute)"
  }))));
}
function MetricCard({
  m
}) {
  return /*#__PURE__*/React.createElement(Card, {
    variant: "soft"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: 10
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 8
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      width: 28,
      height: 28,
      borderRadius: 9,
      background: m.tone === 'ok' ? 'rgba(93,122,78,.12)' : 'rgba(201,138,58,.14)',
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: m.tone === 'ok' ? 'var(--leaf)' : 'var(--amber)'
    }
  }, /*#__PURE__*/React.createElement(Icon, {
    name: m.icon,
    size: 16
  })), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10.5,
      letterSpacing: '.06em',
      textTransform: 'uppercase',
      color: 'var(--ink-soft)'
    }
  }, m.label)), /*#__PURE__*/React.createElement(Icon, {
    name: "chev",
    size: 16,
    color: "var(--ink-mute)"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'flex-end',
      justifyContent: 'space-between',
      gap: 16
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 30,
      fontWeight: 700,
      letterSpacing: '-.02em',
      color: m.tone === 'ok' ? 'var(--ink)' : 'var(--amber)',
      fontStyle: 'normal'
    }
  }, m.value), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10,
      color: 'var(--ink-mute)',
      marginLeft: 6,
      textTransform: 'uppercase'
    }
  }, m.unit)), /*#__PURE__*/React.createElement("div", {
    style: {
      width: 116,
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement(Sparkline, {
    bars: m.bars,
    height: 40
  }))), /*#__PURE__*/React.createElement("p", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 13,
      color: 'var(--ink-soft)',
      lineHeight: 1.4,
      margin: '12px 0 0'
    }
  }, m.foot));
}

// ── Asha (profile) ──────────────────────────────────────────────────────────
function AshaScreen({
  onBack
}) {
  const [fall, setFall] = useStateB(true);
  const [quiet, setQuiet] = useStateB(false);
  return /*#__PURE__*/React.createElement("div", {
    className: "screen"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '12px 22px 4px'
    }
  }, /*#__PURE__*/React.createElement(IconButton, {
    ariaLabel: "Back",
    onClick: onBack
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "back",
    size: 18,
    stroke: 1.8
  })), /*#__PURE__*/React.createElement(IconButton, {
    ariaLabel: "More"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "dots",
    size: 18
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '8px 22px 24px'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      textAlign: 'center',
      gap: 6,
      marginBottom: 28
    }
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: "Asha",
    fill: "striped",
    size: 92
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "'Manrope', system-ui, sans-serif",
      fontSize: 30,
      lineHeight: 1.1,
      color: 'var(--ink)',
      marginTop: 10,
      whiteSpace: 'nowrap'
    }
  }, "Asha Sharma"), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 14,
      lineHeight: 1.4,
      color: 'var(--ink-soft)',
      marginTop: 4
    }
  }, "72 years \xB7 Pune \xB7 with Sahayak ", /*#__PURE__*/React.createElement("em", {
    style: {
      fontFamily: "'Manrope', system-ui, sans-serif",
      fontStyle: 'italic'
    }
  }, "8 months"))), /*#__PURE__*/React.createElement(Eyebrow, {
    style: {
      marginBottom: 10
    }
  }, "Sahayak so far"), /*#__PURE__*/React.createElement(Card, {
    variant: "soft",
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      textAlign: 'center',
      padding: '18px 14px'
    }
  }, [['243', 'morning reports'], ['12', 'home visits'], ['7', 'doctor calls'], ['1', 'SOS resolved']].map(([n, l], i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      flex: 1,
      borderLeft: i ? '0.5px solid var(--line)' : 'none'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "'Manrope', system-ui, sans-serif",
      fontSize: 26,
      color: 'var(--sage)'
    }
  }, n), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 11,
      color: 'var(--ink-mute)',
      lineHeight: 1.3,
      marginTop: 2
    }
  }, l)))), /*#__PURE__*/React.createElement("div", {
    style: {
      margin: '24px 0 10px'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, null, "Conditions")), /*#__PURE__*/React.createElement(Card, {
    variant: "clinical"
  }, ['Mild hypertension', 'Type 2 diabetes (controlled)', 'Recovering from R-knee replacement'].map((c, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 10,
      padding: '8px 0',
      borderTop: i ? '0.5px solid var(--line)' : 'none'
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      width: 6,
      height: 6,
      borderRadius: '50%',
      background: 'var(--leaf)'
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 14,
      color: 'var(--ink)'
    }
  }, c)))), /*#__PURE__*/React.createElement("div", {
    style: {
      margin: '24px 0 10px'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, null, "Alerts to you")), /*#__PURE__*/React.createElement(Card, {
    variant: "soft",
    style: {
      padding: '6px 18px'
    }
  }, /*#__PURE__*/React.createElement(SettingRow, {
    label: "Fall detection",
    sub: "Auto-SOS if a fall is detected",
    on: fall,
    set: setFall
  }), /*#__PURE__*/React.createElement(SettingRow, {
    label: "Quiet hours",
    sub: "Hold non-urgent alerts 10 PM\u20137 AM",
    on: quiet,
    set: setQuiet,
    border: true
  }))));
}
function SettingRow({
  label,
  sub,
  on,
  set,
  border
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 12,
      padding: '14px 0',
      borderTop: border ? '0.5px solid var(--line)' : 'none'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 14,
      fontWeight: 600,
      color: 'var(--ink)'
    }
  }, label), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 12,
      color: 'var(--ink-mute)',
      marginTop: 2
    }
  }, sub)), /*#__PURE__*/React.createElement(Toggle, {
    checked: on,
    onChange: set,
    ariaLabel: label
  }));
}
Object.assign(window, {
  HealthScreen,
  AshaScreen
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/sahayak/screens-health-asha.jsx", error: String((e && e.message) || e) }); }

// ui_kits/sahayak/screens-today-care.jsx
try { (() => {
// Sahayak UI kit — Today & Care screens.
const {
  useState: useStateA
} = React;

// ── data ──────────────────────────────────────────────────────────────────
const INSIGHTS = [{
  id: 'bp',
  tone: 'watch',
  label: 'Blood pressure',
  chip: ['BP', '142/88'],
  headline: /*#__PURE__*/React.createElement(React.Fragment, null, "Her morning BP nudged a little higher than usual. This is the ", /*#__PURE__*/React.createElement("em", null, "third time"), " in a row."),
  bars: [{
    v: .5,
    t: 'ok'
  }, {
    v: .55,
    t: 'ok'
  }, {
    v: .6,
    t: 'ok'
  }, {
    v: .65,
    t: 'ok'
  }, {
    v: .6,
    t: 'ok'
  }, {
    v: .8,
    t: 'watch'
  }, {
    v: .9,
    t: 'watch'
  }]
}, {
  id: 'sleep',
  tone: 'ok',
  label: 'Sleep',
  chip: ['Sleep', '7h 20m'],
  headline: /*#__PURE__*/React.createElement(React.Fragment, null, "She slept gently \u2014 ", /*#__PURE__*/React.createElement("em", null, "7h 20m"), ", her steadiest week yet."),
  bars: [{
    v: .5,
    t: 'ok'
  }, {
    v: .55,
    t: 'ok'
  }, {
    v: .65,
    t: 'ok'
  }, {
    v: .7,
    t: 'ok'
  }, {
    v: .75,
    t: 'ok'
  }, {
    v: .78,
    t: 'ok'
  }, {
    v: .85,
    t: 'on'
  }]
}, {
  id: 'meds',
  tone: 'ok',
  label: 'Medication',
  chip: ['Pills', '2 / 2'],
  headline: /*#__PURE__*/React.createElement(React.Fragment, null, "Both pills, on time. ", /*#__PURE__*/React.createElement("em", null, "The evening one too.")),
  bars: [{
    v: 1,
    t: 'on'
  }, {
    v: 1,
    t: 'on'
  }, {
    v: .5,
    t: 'ok'
  }, {
    v: 1,
    t: 'on'
  }, {
    v: 1,
    t: 'on'
  }, {
    v: .7,
    t: 'ok'
  }, {
    v: 1,
    t: 'on'
  }]
}];
const NEW_DOCS = [{
  id: 'lipid',
  glyph: 'Lp',
  tone: 'clay',
  title: 'Lipid panel',
  when: 'Arrived today',
  teaser: '4 values · 1 slightly above range.'
}, {
  id: 'rx',
  glyph: 'Rx',
  tone: 'sage',
  title: 'Telmisartan refill',
  when: 'Yesterday',
  teaser: 'Same dose · 30-day supply.'
}];
function TodayScreen({
  onOpenCare,
  onBell,
  onMessage
}) {
  return /*#__PURE__*/React.createElement("div", {
    className: "screen"
  }, /*#__PURE__*/React.createElement(TopBar, {
    greet: /*#__PURE__*/React.createElement(React.Fragment, null, "Good morning, ", /*#__PURE__*/React.createElement("b", {
      style: {
        color: 'var(--ink-soft)',
        fontWeight: 600,
        textTransform: 'none',
        letterSpacing: 0
      }
    }, "Aarav")),
    onBell: onBell,
    onMessage: onMessage,
    hasUnread: true
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '8px 22px 24px',
      display: 'flex',
      flexDirection: 'column',
      gap: 14
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "glass-hero"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: 16
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    style: {
      color: 'rgba(246,243,238,.6)'
    }
  }, "Sunday \xB7 18 May \xB7 Pune"), /*#__PURE__*/React.createElement(Badge, {
    tone: "watch",
    onAccent: true
  }, "1 to watch")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 8,
      marginBottom: 14,
      color: 'var(--sage-ink)'
    }
  }, /*#__PURE__*/React.createElement(Sun, null), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 15,
      fontWeight: 700
    }
  }, "34\xB0"), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10,
      letterSpacing: '.06em',
      textTransform: 'uppercase',
      color: 'rgba(246,243,238,.6)'
    }
  }, "Sunny")), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "'Manrope', system-ui, sans-serif",
      fontSize: 31,
      lineHeight: 1.12,
      color: 'var(--sage-ink)',
      textWrap: 'pretty'
    }
  }, "A calm night for Mom. ", /*#__PURE__*/React.createElement("em", {
    style: {
      fontStyle: 'italic',
      textDecoration: 'underline',
      textDecorationColor: 'rgba(246,243,238,.42)',
      textUnderlineOffset: 5,
      textDecorationThickness: 1
    }
  }, "One small thing"), " worth a look \u2014 her morning BP."), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginTop: 20,
      paddingTop: 14,
      borderTop: '0.75px solid rgba(246,243,238,.16)'
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10,
      letterSpacing: '.05em',
      textTransform: 'uppercase',
      color: 'rgba(246,243,238,.55)'
    }
  }, "Updated 9:30 AM"), /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: 4,
      fontFamily: 'var(--font-sans)',
      fontSize: 13,
      fontWeight: 600,
      color: 'var(--sage-ink)'
    }
  }, "See the morning ", /*#__PURE__*/React.createElement(Icon, {
    name: "chev",
    size: 14,
    color: "var(--sage-ink)"
  })))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 8,
      overflowX: 'auto',
      paddingBottom: 2
    },
    className: "noscroll"
  }, /*#__PURE__*/React.createElement(Chip, {
    label: "Heart rate",
    value: "72",
    unit: "bpm"
  }), /*#__PURE__*/React.createElement(Chip, {
    label: "Oxygen",
    value: "97",
    unit: "%"
  }), /*#__PURE__*/React.createElement(Chip, {
    label: "Falls",
    value: "None"
  }), /*#__PURE__*/React.createElement(Chip, {
    label: "BP",
    value: "142/88",
    tone: "watch"
  })), INSIGHTS.map(ins => /*#__PURE__*/React.createElement(Card, {
    key: ins.id,
    variant: "soft",
    as: "button",
    style: {
      textAlign: 'left',
      cursor: 'pointer',
      width: '100%'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: 10
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    style: ins.tone === 'watch' ? {
      color: 'var(--amber)'
    } : null
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-block',
      width: 6,
      height: 6,
      borderRadius: '50%',
      background: ins.tone === 'watch' ? 'var(--amber)' : 'var(--leaf)',
      marginRight: 7,
      verticalAlign: 'middle'
    }
  }), ins.label), /*#__PURE__*/React.createElement(Icon, {
    name: "chev",
    size: 16,
    color: "var(--ink-mute)"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "'Manrope', system-ui, sans-serif",
      fontStyle: 'italic',
      fontSize: 22,
      lineHeight: 1.15,
      color: 'var(--ink)',
      textWrap: 'pretty'
    }
  }, React.cloneElement(/*#__PURE__*/React.createElement("span", null, ins.headline))), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 16
    }
  }, /*#__PURE__*/React.createElement(Sparkline, {
    bars: ins.bars,
    height: 42
  })))), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 6
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'baseline',
      justifyContent: 'space-between',
      marginBottom: 10
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, null, "Just in for Mom"), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10,
      color: 'var(--ink-mute)'
    }
  }, "2 NEW")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 12
    }
  }, NEW_DOCS.map(d => /*#__PURE__*/React.createElement(Card, {
    key: d.id,
    variant: "clinical"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 12
    }
  }, /*#__PURE__*/React.createElement(GlyphBubble, {
    glyph: d.glyph,
    tone: d.tone,
    size: 38
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 8
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 14.5,
      fontWeight: 600,
      color: 'var(--ink)'
    }
  }, d.title), d.tone === 'clay' && /*#__PURE__*/React.createElement(Badge, {
    tone: "watch"
  }, "Look")), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 12.5,
      color: 'var(--ink-mute)',
      marginTop: 2
    }
  }, d.when, " \xB7 ", d.teaser))))))), /*#__PURE__*/React.createElement(Button, {
    variant: "ghost",
    full: true,
    onClick: onOpenCare,
    style: {
      marginTop: 4
    }
  }, "See what we can do for Mom")));
}
function Sun() {
  return /*#__PURE__*/React.createElement("svg", {
    width: "20",
    height: "20",
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "1.6",
    strokeLinecap: "round"
  }, /*#__PURE__*/React.createElement("circle", {
    cx: "12",
    cy: "12",
    r: "4"
  }), /*#__PURE__*/React.createElement("path", {
    d: "M12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6l1.4 1.4M17 17l1.4 1.4M5.6 18.4l1.4-1.4M17 7l1.4-1.4"
  }));
}

// ── Care ────────────────────────────────────────────────────────────────────
const SERVICES = [{
  id: 'nursing',
  glyph: 'Ns',
  tone: 'sage',
  title: 'Home nursing',
  sub: 'Wound care, injections, daily help'
}, {
  id: 'physio',
  glyph: 'Pt',
  tone: 'clay',
  title: 'Physiotherapy',
  sub: 'Recovery, mobility, strength'
}, {
  id: 'doctor',
  glyph: 'Dr',
  tone: 'leaf',
  title: 'Doctor visit',
  sub: 'Video or at-home, same day'
}, {
  id: 'lab',
  glyph: 'Lb',
  tone: 'amber',
  title: 'Home lab tests',
  sub: 'Collection in 90 minutes'
}, {
  id: 'companion',
  glyph: 'Co',
  tone: 'muted',
  title: 'Companion visits',
  sub: 'A warm, regular friend'
}, {
  id: 'postop',
  glyph: 'Rx',
  tone: 'sage',
  title: 'Post-op recovery',
  sub: 'Structured, tapering plans'
}];
function CareScreen({
  onBack,
  onBook
}) {
  return /*#__PURE__*/React.createElement("div", {
    className: "screen"
  }, /*#__PURE__*/React.createElement(TopBar, {
    onBack: onBack
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '8px 22px 24px'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    style: {
      paddingLeft: 2
    }
  }, "Care for Mom"), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "'Manrope', system-ui, sans-serif",
      fontSize: 28,
      lineHeight: 1.1,
      color: 'var(--ink)',
      margin: '8px 0 0'
    }
  }, "Small steps that ", /*#__PURE__*/React.createElement("em", {
    style: {
      fontStyle: 'italic',
      color: 'var(--accent)'
    }
  }, "help Mom"), "."), /*#__PURE__*/React.createElement("p", {
    style: {
      fontFamily: 'var(--font-sans)',
      fontSize: 15,
      lineHeight: 1.45,
      color: 'var(--ink-soft)',
      margin: '8px 0 0'
    }
  }, "Today's nudges, and the home-care services we trust for the long run."), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'baseline',
      justifyContent: 'space-between',
      margin: '24px 0 10px'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, null, "Upcoming for Mom"), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-mono)',
      fontSize: 10,
      color: 'var(--accent)',
      fontWeight: 500
    }
  }, "SEE ALL 4 \u203A")), /*#__PURE__*/React.createElement(ActionRow, {
    glyph: "AM",
    glyphTone: "sage",
    title: "Dr. Anjali Mehta",
    sub: "BP review video call \xB7 Today \xB7 4:00 PM"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      margin: '24px 0 10px'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, null, "Suggested today")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 12
    }
  }, /*#__PURE__*/React.createElement(ActionRow, {
    glyph: "Rx",
    glyphTone: "sage",
    title: "Schedule a BP review",
    sub: "Dr. Mehta \xB7 15 min video \xB7 today or tomorrow",
    onClick: onBook
  }), /*#__PURE__*/React.createElement(ActionRow, {
    glyph: "Pt",
    glyphTone: "clay",
    title: "Book a physio home visit",
    sub: "Knee recovery \xB7 routine check \xB7 this week",
    onClick: onBook
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      margin: '24px 0 10px'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, null, "Home care services")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'grid',
      gridTemplateColumns: '1fr 1fr',
      gap: 12
    }
  }, SERVICES.map(s => /*#__PURE__*/React.createElement(Card, {
    key: s.id,
    variant: "soft",
    as: "button",
    onClick: onBook,
    style: {
      textAlign: 'left',
      cursor: 'pointer',
      padding: '16px 16px 14px'
    }
  }, /*#__PURE__*/React.createElement(GlyphBubble, {
    glyph: s.glyph,
    tone: s.tone,
    size: 40
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 14.5,
      fontWeight: 600,
      color: 'var(--ink)',
      marginTop: 12
    }
  }, s.title), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 12,
      color: 'var(--ink-mute)',
      marginTop: 3,
      lineHeight: 1.35
    }
  }, s.sub))))));
}
Object.assign(window, {
  TodayScreen,
  CareScreen
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/sahayak/screens-today-care.jsx", error: String((e && e.message) || e) }); }

__ds_ns.Button = __ds_scope.Button;

__ds_ns.IconButton = __ds_scope.IconButton;

__ds_ns.Avatar = __ds_scope.Avatar;

__ds_ns.Badge = __ds_scope.Badge;

__ds_ns.Chip = __ds_scope.Chip;

__ds_ns.GlyphBubble = __ds_scope.GlyphBubble;

__ds_ns.Sparkline = __ds_scope.Sparkline;

__ds_ns.SegmentedControl = __ds_scope.SegmentedControl;

__ds_ns.Toggle = __ds_scope.Toggle;

__ds_ns.Icon = __ds_scope.Icon;

__ds_ns.ICON_NAMES = __ds_scope.ICON_NAMES;

__ds_ns.BottomNav = __ds_scope.BottomNav;

__ds_ns.ActionRow = __ds_scope.ActionRow;

__ds_ns.Card = __ds_scope.Card;

})();
