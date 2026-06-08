module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-empty': [2, 'never'], // exige escopo (o ticket DDE)
    'scope-case': [2, 'always', 'upper-case'], // DDE em maiusculo
  },
};
