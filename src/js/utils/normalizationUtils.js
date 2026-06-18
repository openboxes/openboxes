import _ from 'lodash';

/**
 * Generic helpers for working with normalized state, shaped as { entities, ids }
 *
 * - entities: a map of id -> item
 * - ids: an ordered list of ids that preserves the source order
 */

/**
 * Separator used when combining multiple fields into a single composite key.
 */
export const COMPOSITE_KEY_SEPARATOR = ':';

/**
 * Resolves the key of an entity. Supports:
 * - a string: uses `item[keyField]` (e.g. 'id', 'shipmentItemId')
 * - an array of strings: combines several fields into one composite key, e.g.
 *   `['shipmentItemId', 'receiptItemId']` -> `'<shipmentItemId>:<receiptItemId>'`
 * - a function: `(item) => key` for full control over the key shape
 * @param {Object} item - entity to read the key from
 * @param {string|Array<string>|Function} [keyField = 'id'] - key descriptor
 * @returns {string|number}
 */
export const resolveKey = (item, keyField = 'id') => {
  if (typeof keyField === 'function') {
    return keyField(item);
  }

  if (Array.isArray(keyField)) {
    return keyField.map((field) => item[field]).join(COMPOSITE_KEY_SEPARATOR);
  }

  return item[keyField];
};

/**
 * Creates an empty normalized state.
 * @returns {{ entities: Object, ids: Array }}
 */
export const createNormalizedState = () => ({ entities: {}, ids: [] });

/**
 * Converts an array to normalized state { entities, ids }.
 * @param {Array} array - data to normalize
 * @param {string|Array<string>|Function} [keyField = 'id'] - key descriptor,
 *   see {@link resolveKey} (supports composite keys)
 * @returns {{ entities: Object, ids: Array }}
 */
export const normalizeData = (array, keyField = 'id') => {
  if (!Array.isArray(array)) {
    return createNormalizedState();
  }

  return array.reduce((acc, item) => {
    const id = resolveKey(item, keyField);
    return {
      entities: { ...acc.entities, [id]: item },
      ids: [...acc.ids, id],
    };
  }, createNormalizedState());
};

/**
 * Converts normalized state back to an array, preserving the order from `ids`.
 * @param {{ entities: Object, ids: Array }} state - normalized state
 * @returns {Array}
 */
export const denormalizeData = (state) =>
  (state?.ids || []).map((id) => state.entities[id]);

/**
 * Reads a single entity from normalized state.
 * @param {{ entities: Object, ids: Array }} state - normalized state
 * @param {string|number} id - id of the entity
 * @returns {Object|undefined}
 */
export const getNormalizedItem = (state, id) => state?.entities?.[id];

/**
 * Shallow-merges new data into one of the normalized entities.
 * Returns the same state reference when the id does not exist.
 * @param {{ entities: Object, ids: Array }} state - normalized state
 * @param {string|number} id - id of the entity to update
 * @param {Object} newData - data merged into the entity under the given id
 * @returns {{ entities: Object, ids: Array }}
 */
export const updateNormalizedItem = (state, id, newData) => {
  if (!state?.entities?.[id]) {
    return state;
  }

  return {
    ...state,
    entities: {
      ...state.entities,
      [id]: {
        ...state.entities[id],
        ...newData,
      },
    },
  };
};

/**
 * Inserts a new entity or shallow-merges it into an existing one.
 * @param {{ entities: Object, ids: Array }} state - normalized state
 * @param {Object} item - entity to insert or merge
 * @param {string|Array<string>|Function} [keyField = 'id'] - key descriptor,
 *   see {@link resolveKey} (supports composite keys)
 * @returns {{ entities: Object, ids: Array }}
 */
export const upsertNormalizedItem = (state, item, keyField = 'id') => {
  const id = resolveKey(item, keyField);
  const exists = Boolean(state?.entities?.[id]);

  return {
    ids: exists ? state.ids : [...(state?.ids || []), id],
    entities: {
      ...(state?.entities || {}),
      [id]: exists ? { ...state.entities[id], ...item } : item,
    },
  };
};

/**
 * Removes an entity from normalized state.
 * Returns the same state reference when the id does not exist.
 * @param {{ entities: Object, ids: Array }} state - normalized state
 * @param {string|number} id - id of the entity to remove
 * @returns {{ entities: Object, ids: Array }}
 */
export const removeNormalizedItem = (state, id) => {
  if (!state?.entities?.[id]) {
    return state;
  }

  return {
    ids: state.ids.filter((currentId) => currentId !== id),
    entities: _.omit(state.entities, id),
  };
};
