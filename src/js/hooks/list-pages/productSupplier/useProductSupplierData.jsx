import { useEffect, useState } from 'react';

import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

import { hideSpinner, showSpinner } from 'actions';
import productSupplierApi from 'api/services/ProductSupplierApi';

const useProductSupplierData = () => {
  const { productSupplierId } = useParams();
  const dispatch = useDispatch();
  const [productSupplier, setProductSupplier] = useState(null);

  const fetchProductSupplier = async (id) => {
    try {
      dispatch(showSpinner());
      const fetchedProductSupplier = await productSupplierApi.getProductSupplier(id);
      setProductSupplier(fetchedProductSupplier?.data?.data);
    } finally {
      dispatch(hideSpinner());
    }
  };

  useEffect(() => {
    if (productSupplierId) {
      fetchProductSupplier(productSupplierId);
    }
  }, [productSupplierId]);

  return {
    productSupplier,
  };
};

export default useProductSupplierData;
