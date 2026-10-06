import { useLayoutEffect } from 'react';

/**
 * Width of the ellipsis that the clamp draws at the end of cut text.
 *
 * @param {HTMLElement} element - element whose text ends with the ellipsis
 * @returns {number} width of the ellipsis in pixels
 */
const ellipsisWidth = (element) => parseFloat(getComputedStyle(element).fontSize) || 0;

/**
 * Last line of the element's text that its line clamp still shows.
 *
 * @param {HTMLElement} element - element with the clamped text
 * @returns {DOMRect|undefined} the line, or nothing when the text cannot be measured
 */
const lastVisibleLine = (element) => {
  const box = element.getBoundingClientRect();
  const range = document.createRange();
  range.selectNodeContents(element);
  const visibleLines = Array.from(range.getClientRects?.() ?? [])
    .filter((line) => line.top - box.top < element.clientHeight - 1);
  return visibleLines[visibleLines.length - 1];
};

/**
 * Tells whether the line clamp cuts the element's text, which is the case when the text is taller
 * than what the element shows.
 *
 * @param {HTMLElement} element - element with the clamped text
 * @returns {boolean} true when the text is cut and therefore ends with an ellipsis
 */
const isClamped = (element) => element.scrollHeight - element.clientHeight > 1;

/**
 * Style that puts the icons right after the given line, relative to the element.
 *
 * The icons get the height of the line, so that the stylesheet can centre them on it.
 *
 * @param {DOMRect} line - line to follow, in viewport coordinates
 * @param {HTMLElement} options.element - element with the text
 * @param {number} options.iconsWidth - width of the icons
 * @param {number} options.gap - space to leave between the text and the icons
 * @returns {Object} style for the icons, replacing the position they get from the stylesheet
 */
const iconsStyleAfterLine = (line, { element, iconsWidth, gap }) => {
  const box = element.getBoundingClientRect();
  const lineEnd = line.right - box.left + (isClamped(element) ? ellipsisWidth(element) : 0) + gap;
  return {
    left: `${Math.min(lineEnd, element.clientWidth - iconsWidth)}px`,
    top: `${line.top - box.top}px`,
    height: `${line.height}px`,
    right: 'auto',
    bottom: 'auto',
  };
};

/**
 * Keeps the icons at the end of the last visible line of clamped text. CSS cannot do that: inside
 * the text flow the clamp cuts the icons off along with the text, and outside of it they can only
 * be put at an edge of the box.
 *
 * The icons are moved and the text is left alone, so the only ellipsis is the one the clamp draws.
 * When the measurement gets old, or cannot be taken at all, the icons just end up further from the
 * text than they should be, in the space kept for them at the end of every line.
 *
 * Moving them writes their style instead of rendering them again, so following the text costs no
 * re-render.
 *
 * @param {Object} options.textRef - ref to the element with the clamped text
 * @param {Object} options.iconsRef - ref to the element with the icons
 * @param {boolean} options.enabled - when false, the icons are left where they are rendered
 * @param {string} options.content - the text the icons follow; another one breaks into other lines
 * @param {number} options.iconsCount - how many icons are placed; more of them take more space
 * @param {number} [options.gap] - space between the end of the text and the icons, in pixels
 */
const useIconsAfterLastLine = ({
  textRef, iconsRef, enabled, content, iconsCount, gap = 4,
}) => {
  useLayoutEffect(() => {
    const text = textRef.current;
    const icons = iconsRef.current;
    if (!enabled || !text || !icons) {
      return undefined;
    }

    // Keeps space for the icons at the end of the text and moves them there
    const placeIcons = () => {
      const iconsWidth = icons.offsetWidth;
      // Space for the icons at the end of every line, so that neither the text nor the ellipsis
      // closing it ends up under them.
      text.style.paddingRight = `${iconsWidth + gap}px`;
      const line = lastVisibleLine(text);
      Object.assign(
        icons.style,
        line ? iconsStyleAfterLine(line, { element: text, iconsWidth, gap }) : {},
      );
    };

    placeIcons();

    // The text breaks into different lines when its column is resized.
    const observer = new ResizeObserver(placeIcons);
    observer.observe(text);

    document.fonts?.ready?.then(placeIcons);

    return () => observer.disconnect();
  }, [enabled, content, iconsCount, gap]);
};

export default useIconsAfterLastLine;
