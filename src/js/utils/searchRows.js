import _ from 'lodash';

/**
 * Case-insensitive substring match: returns true when the search term is contained in the
 * value at any of the given lodash paths on the row. Missing / null values are treated as
 * empty strings.
 */
const rowMatchesSearch = ({ row, search, paths }) => {
  const query = search.toLowerCase();
  return paths.some((path) => (_.get(row, path) ?? '').toLowerCase().includes(query));
};

export default rowMatchesSearch;
