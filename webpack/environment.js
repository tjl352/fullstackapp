module.exports = {
  // APP_VERSION is passed as an environment variable from the Gradle / Maven build tasks.
  VERSION: process.env.APP_VERSION || 'DEV',
  // The root URL for API calls, ending with a '/' - for example: `"https://www.jhipster.tech:8081/myservice/"`.
  // If this URL is left empty (""), then it will be relative to the current context (monolith / webpack proxy).
  //
  // Phase 1 learning (separate UI): set when React runs on another origin than Spring, e.g.:
  //   SERVER_API_URL=http://localhost:8080/ npm start
  // Prod static host example:
  //   SERVER_API_URL=https://fullstackapp-qwj8.onrender.com/ npm run webapp:build:prod
  // You must enable CORS on the API (application-dev.yml / application-prod.yml).
  SERVER_API_URL: process.env.SERVER_API_URL || '',
};
