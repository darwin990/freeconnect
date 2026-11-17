/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
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
      },
      boxShadow: {
        soft: "0 10px 30px -18px rgba(17, 40, 79, 0.45)",
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "Segoe UI", "sans-serif"],
      },
    },
  },
  plugins: [],
};
