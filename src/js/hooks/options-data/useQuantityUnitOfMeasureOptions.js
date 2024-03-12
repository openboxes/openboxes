import { useSelector } from 'react-redux';

import { fetchQuantityUnitOfMeasure } from 'actions';
import useOptionsFetch from 'hooks/options-data/useOptionsFetch';

const useQuantityUnitOfMeasureOptions = () => {
  const { quantityUom } = useSelector((state) => ({
    quantityUom: state.unitOfMeasure.quantity,
  }));

  useOptionsFetch(
    [fetchQuantityUnitOfMeasure],
    { refetchOnLocationChange: false },
  );

  return { quantityUom };
};

export default useQuantityUnitOfMeasureOptions;
