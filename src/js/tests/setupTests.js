import { TextDecoder, TextEncoder } from 'util';

global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;

// Vitest copies jsdom globals including the unimplemented `CSS` as undefined,
// which breaks libraries that only check `'CSS' in window` (e.g. react-tooltip)
if (window.CSS === undefined) {
  delete window.CSS;
}
