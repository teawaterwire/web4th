const defaultTheme = require("tailwindcss/defaultTheme");

module.exports = {
  mode: "jit",
  purge: {
    // in prod look at shadow-cljs output file in dev look at runtime, which will change files that are actually compiled; postcss watch should be a whole lot faster
    content:
      process.env.NODE_ENV == "production"
        ? ["./resources/public/js/compiled/app.*.js"]
        : ["./resources/public/js/compiled/cljs-runtime/*.js"],
  },
  darkMode: false, // or 'media' or 'class'
  variants: {},
  plugins: [require("@tailwindcss/forms")],
};
