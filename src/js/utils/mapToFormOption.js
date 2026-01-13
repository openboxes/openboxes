/**
 * Maps a data object to a form-compatible select option.
 *
 * @param {Object} item - Source entity containing at least `id` and `name`.
 * @param {Object} [options]
 * @param {string} [options.customLabel] - Optional label override.
 * @returns {Object|undefined} Form option object or undefined if item is falsy.
 */
const mapToFormOption = (item, { customLabel } = {}) => {
  if (!item) {
    return undefined;
  }

  return {
    id: item.id,
    name: item.name,
    label: customLabel || item.name,
  };
};

export default mapToFormOption;
