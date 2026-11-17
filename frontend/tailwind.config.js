/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        // Primario (azul de marca) y secundario (verde azulado) del prototipo.
        brand: {
          50: "#eef7ff",
          100: "#d8edff",
          200: "#b9dfff",
          300: "#88caff",
          400: "#4fadff",
          500: "#258cff",
          600: "#0d6ef0",
          700: "#1057d4",
          800: "#1548ab",
          900: "#173f86",
          950: "#11284f",
        },
        accent: {
          50: "#ecfdf7",
          100: "#d1fae8",
          200: "#a7f3d6",
          300: "#6ee7bf",
          400: "#34d3a4",
          500: "#14b88b",
          600: "#0f9f7a",
          700: "#0b7f62",
          800: "#0b6550",
          900: "#0a5343",
        },
        ink: {
          50: "#f7f8fa",
          100: "#eceef2",
          200: "#d5dae3",
          300: "#b0b9c9",
          400: "#8692a8",
          500: "#66738b",
          600: "#505b70",
          700: "#41495b",
          800: "#383e4d",
          900: "#232733",
          950: "#151821",
        },
        // Semáforo semántico con escala para fondos y texto.
        success: {
          50: "#ecfdf3",
          100: "#d1fadf",
          200: "#a6f4c5",
          600: "#079455",
          700: "#067647",
          900: "#054f31",
        },
        warning: {
          50: "#fffaeb",
          100: "#fef0c7",
          200: "#fedf89",
          600: "#dc6803",
          700: "#b54708",
          900: "#7a2e0e",
        },
        danger: {
          50: "#fef3f2",
          100: "#fee4e2",
          200: "#fecdca",
          600: "#d92d20",
          700: "#b42318",
          900: "#7a271a",
        },
        info: {
          50: "#eff8ff",
          100: "#d1e9ff",
          200: "#b2ddff",
          600: "#1570ef",
          700: "#175cd3",
          900: "#194185",
        },
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "Segoe UI", "sans-serif"],
        display: ["Inter", "ui-sans-serif", "system-ui", "Segoe UI", "sans-serif"],
      },
      fontSize: {
        "2xs": ["0.6875rem", { lineHeight: "1rem" }],
      },
      boxShadow: {
        soft: "0 10px 30px -18px rgba(17, 40, 79, 0.45)",
        card: "0 1px 2px rgba(16, 24, 40, 0.04), 0 12px 32px -20px rgba(16, 24, 40, 0.28)",
        float: "0 24px 64px -28px rgba(17, 40, 79, 0.45)",
        glow: "0 0 0 1px rgba(13, 110, 240, 0.12), 0 18px 40px -24px rgba(13, 110, 240, 0.55)",
      },
      borderRadius: {
        "4xl": "2rem",
      },
      transitionTimingFunction: {
        smooth: "cubic-bezier(0.22, 1, 0.36, 1)",
      },
      keyframes: {
        "fade-in": {
          from: { opacity: "0" },
          to: { opacity: "1" },
        },
        "fade-up": {
          from: { opacity: "0", transform: "translateY(10px)" },
          to: { opacity: "1", transform: "translateY(0)" },
        },
        "scale-in": {
          from: { opacity: "0", transform: "scale(0.97)" },
          to: { opacity: "1", transform: "scale(1)" },
        },
        shimmer: {
          "100%": { transform: "translateX(100%)" },
        },
        "slide-in-right": {
          from: { opacity: "0", transform: "translateX(16px)" },
          to: { opacity: "1", transform: "translateX(0)" },
        },
      },
      animation: {
        "fade-in": "fade-in 200ms ease-out both",
        "fade-up": "fade-up 240ms cubic-bezier(0.22, 1, 0.36, 1) both",
        "scale-in": "scale-in 180ms cubic-bezier(0.22, 1, 0.36, 1) both",
        shimmer: "shimmer 1.6s infinite",
        "slide-in-right": "slide-in-right 220ms cubic-bezier(0.22, 1, 0.36, 1) both",
      },
      backgroundImage: {
        "brand-gradient": "linear-gradient(135deg, #1057d4 0%, #0d6ef0 45%, #14b88b 120%)",
        "ink-gradient": "linear-gradient(160deg, #151821 0%, #11284f 60%, #173f86 130%)",
        "grid-soft":
          "linear-gradient(to right, rgba(102,115,139,0.09) 1px, transparent 1px), linear-gradient(to bottom, rgba(102,115,139,0.09) 1px, transparent 1px)",
      },
      backgroundSize: {
        grid: "32px 32px",
      },
    },
  },
  plugins: [],
};
