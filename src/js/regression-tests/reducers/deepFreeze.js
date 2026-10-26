/**
 * Freezes a value and everything reachable from it. Reducer modules run in strict mode, so any
 * attempt to mutate a frozen previous state throws a TypeError and fails the test.
 */
const deepFreeze = (value) => {
  if (value && typeof value === 'object' && !Object.isFrozen(value)) {
    Object.freeze(value);
    Object.values(value).forEach(deepFreeze);
  }
  return value;
};

export default deepFreeze;
