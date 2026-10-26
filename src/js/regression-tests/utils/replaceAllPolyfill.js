// OBAF's file-download-util uses String.prototype.replaceAll, which every supported browser has
// but the Node 14 that runs these tests does not: importing this file supplies it, so the tests
// see browser behaviour.
if (!String.prototype.replaceAll) {
  // eslint-disable-next-line no-extend-native
  Object.defineProperty(String.prototype, 'replaceAll', {
    configurable: true,
    writable: true,
    value: function replaceAll(search, replacement) {
      return String(this).split(search).join(replacement);
    },
  });
}
