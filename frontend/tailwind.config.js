/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        primary: '#1e40af',
        accent: '#f97316'
      }
    }
  },
  plugins: []
}
