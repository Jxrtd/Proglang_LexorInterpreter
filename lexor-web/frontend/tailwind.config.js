/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'lexor-dark': '#0d1117',
        'lexor-panel': '#161b22',
        'lexor-accent': '#58a6ff',
        'lexor-border': '#30363d',
      }
    },
  },
  plugins: [],
}
