import splitTranslation from 'utils/translation-utils';

describe('splitTranslation()', () => {
  it('should return english', () => {
    const splittedTranslation = splitTranslation('testEn|fr:testFr', 'en');
    expect(splittedTranslation).toBe('testEn');
  });
  it('should return french', () => {
    const splittedTranslation = splitTranslation('testEn|fr:testFr', 'fr');
    expect(splittedTranslation).toBe('testFr');
  });
  it('should return english when french is not present', () => {
    const splittedTranslation = splitTranslation('testEn|fr:', 'fr');
    expect(splittedTranslation).toBe('testEn');
  });
  it('should return empty string when english is not present', () => {
    const splittedTranslation = splitTranslation('|fr:testFr', 'en');
    expect(splittedTranslation).toBe('');
  });
});
