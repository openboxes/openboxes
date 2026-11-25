import { isChunkLoadError } from 'components/ChunkLoadErrorBoundary';

describe('isChunkLoadError', () => {
  it.each([
    ['Chromium', new TypeError('Failed to fetch dynamically imported module: http://localhost/openboxes/dist/Dashboard-abc.js')],
    ['Firefox', new TypeError('error loading dynamically imported module: http://localhost/openboxes/dist/Dashboard-abc.js')],
    ['Safari', new TypeError('Importing a module script failed.')],
    ['Vite CSS preload', new Error('Unable to preload CSS for http://localhost/openboxes/dist/Dashboard-abc.css')],
  ])('should detect a chunk load error (%s)', (_, error) => {
    expect(isChunkLoadError(error)).toBe(true);
  });

  it.each([
    ['generic error', new Error('Something went wrong')],
    ['type error', new TypeError("Cannot read properties of undefined (reading 'id')")],
    ['undefined', undefined],
    ['null', null],
  ])('should not detect a chunk load error (%s)', (_, error) => {
    expect(isChunkLoadError(error)).toBe(false);
  });
});
