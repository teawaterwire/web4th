const defaultTheme = require("tailwindcss/defaultTheme");

module.exports = {
  mode: "jit",
  content:
    process.env.NODE_ENV == "production"
      ? ["./resources/public/js/compiled/app.*.js"]
      : ["./resources/public/js/compiled/cljs-runtime/*.js"],
  variants: {},
  plugins: [require("@tailwindcss/forms")],
};
