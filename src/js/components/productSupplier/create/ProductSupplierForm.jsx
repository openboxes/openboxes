import React from 'react';

import ProductSupplierFormHeader
  from 'components/productSupplier/create/ProductSupplierFormHeader';
import ProductSupplierFormMain from 'components/productSupplier/create/ProductSupplierFormMain';
import useProductSupplierForm from 'hooks/productSupplier/form/useProductSupplierForm';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

const ProductSupplierForm = () => {
  useTranslation('productSupplier');

  const {
    isValid,
    control,
    handleSubmit,
    errors,
    ratingTypeCodes,
    onSubmit,
  } = useProductSupplierForm();

  return (
    <PageWrapper>
      <form onSubmit={handleSubmit(onSubmit)}>
        <ProductSupplierFormHeader
          isValid={isValid}
        />
        <ProductSupplierFormMain
          formProps={{
            control,
            handleSubmit,
            errors,
            ratingTypeCodes,
          }}
        />
      </form>
    </PageWrapper>
  );
};

export default ProductSupplierForm;
