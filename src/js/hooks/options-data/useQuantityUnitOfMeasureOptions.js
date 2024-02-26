import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';

import { fetchQuantityUnitOfMeasure } from 'actions';

const useQuantityUnitOfMeasureOptions = () => {
  const dispatch = useDispatch();

  const { quantityUom } = useSelector((state) => ({
    quantityUom: state.unitOfMeasure.quantity,
  }));

  useEffect(() => {
    const controller = new AbortController();
    const config = {
      signal: controller.signal,
    };

    dispatch(fetchQuantityUnitOfMeasure(config));

    return () => {
      controller.abort();
    };
  }, []);

  return { quantityUom };
};

export default useQuantityUnitOfMeasureOptions;
