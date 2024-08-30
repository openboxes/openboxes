import { useState } from 'react';

const useOutboundImportFiltering = () => {
  const [isFiltered, setIsFiltered] = useState(false);

  return {
    setIsFiltered,
    isFiltered,
  };
};

export default useOutboundImportFiltering;
