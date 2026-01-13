/**
 * Maps a data object to a form-compatible select option.
 *
 * @param {Object} item - Data object containing at least `id` and `name`.
 * @param {Object} [options]
 * @param {string} [options.customLabel] - Optional label override.
 * @returns {Object|undefined} Select option object, or undefined if item is null/undefined
 * or lacks a truthy `id` or `name`.
 */
const mapToFormSelectOption = (item, { customLabel } = {}) => {
  if (!item?.id || !item?.name) {
    return undefined;
  }

  return {
    id: item.id,
    name: item.name,
    label: customLabel || item.name,
  };
};

export default mapToFormSelectOption;
