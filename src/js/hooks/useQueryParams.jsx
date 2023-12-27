import queryString from 'query-string';
import { useLocation } from 'react-router-dom';

// Hook to return current query params in object form
const useQueryParams = () => {
  const { search } = useLocation();
  return queryString.parse(search);
};

export default useQueryParams;
