import React from 'react';

import PropTypes from 'prop-types';
import { Controller, useWatch } from 'react-hook-form';
import { useSelector } from 'react-redux';

import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import Switch from 'components/form-elements/v2/Switch';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import { debounceOrganizationsFetch, debounceProductsFetch } from 'utils/option-utils';

const BasicDetails = ({ control, errors }) => {
  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));

  // Watch product's input changes live, in order to display a "View Product" link
  // with a proper product id
  const product = useWatch({ control, name: 'product' });

  return (
    <Subsection
      title={{ label: 'react.productSupplier.form.subsection.basicDetails', defaultMessage: 'Basic Details' }}
      collapsable={false}
    >
      <div className="form-grid-3">
        <Controller
          name="code"
          control={control}
          render={({ field }) => (
            <TextInput
              title={{ id: 'react.productSupplier.form.code.title', defaultMessage: 'Source Code' }}
              errorMessage={errors.code?.message}
              tooltip={{
                id: 'react.productSupplier.form.code.tooltip',
                defaultMessage: 'Unique code that identifies this record. ' +
                  'Auto-generated based on the product and supplier codes, ' +
                  'but can be manually overwritten',
              }}
              placeholder="Leave blank to autogenerate"
              {...field}
            />
          )}
        />
        <Controller
          name="product"
          control={control}
          render={({ field }) => (
            <SelectField
              title={{ id: 'react.productSupplier.form.product.title', defaultMessage: 'Product Name' }}
              placeholder="Search for a product"
              required
              async
              errorMessage={errors.product?.message}
              button={product
                ? {
                  id: 'react.productSupplier.form.viewProduct.title',
                  defaultMessage: 'View Product',
                  onClick: (id) => window.open(INVENTORY_ITEM_URL.showStockCard(id), '_blank'),
                }
                : null}
              loadOptions={debounceProductsFetch(debounceTime, minSearchLength)}
              {...field}
            />
          )}
        />
        <Controller
          name="legacyCode"
          control={control}
          render={({ field }) => (
            <TextInput
              title={{ id: 'react.productSupplier.form.legacyCode.title', defaultMessage: 'Legacy Code' }}
              errorMessage={errors.legacyCode?.message}
              tooltip={{
                id: 'react.productSupplier.form.legacyCode.tooltip',
                defaultMessage: 'Reference to this record in a previous or parallel purchasing system',
              }}
              {...field}
            />
          )}
        />
        <Controller
          name="supplier"
          control={control}
          render={({ field }) => (
            <SelectField
              title={{ id: 'react.productSupplier.form.supplier.title', defaultMessage: 'Supplier' }}
              placeholder="Select Supplier"
              required
              errorMessage={errors.supplier?.message}
              tooltip={{ id: 'react.productSupplier.form.supplier.tooltip', defaultMessage: 'The company that supplies the product' }}
              async
              loadOptions={debounceOrganizationsFetch(debounceTime, minSearchLength)}
              {...field}
            />
          )}
        />
        <Controller
          name="supplierCode"
          control={control}
          render={({ field }) => (
            <TextInput
              title={{ id: 'react.productSupplier.form.supplierCode.title', defaultMessage: 'Supplier Code' }}
              required
              errorMessage={errors.supplierCode?.message}
              tooltip={{
                id: 'react.productSupplier.form.supplierCode.tooltip',
                defaultMessage: 'The SKU used by the vendor to identify the product',
              }}
              {...field}
            />
          )}
        />
        <Controller
          name="name"
          control={control}
          render={({ field }) => (
            <TextInput
              title={{ id: 'react.productSupplier.form.name.title', defaultMessage: 'Supplier Product Name' }}
              required
              errorMessage={errors.name?.message}
              tooltip={{
                id: 'react.productSupplier.form.name.tooltip',
                defaultMessage: 'The name the supplier calls the product in their catalogue',
              }}
              {...field}
            />
          )}
        />
        <Controller
          name="dateCreated"
          control={control}
          render={({ field }) => (
            <DateField
              title={{ id: 'react.productSupplier.form.dateCreated.title', defaultMessage: 'Source Creation Date' }}
              errorMessage={errors.dateCreated?.message}
              {...field}
              disabled
            />
          )}
        />
        <Controller
          name="lastUpdated"
          control={control}
          render={({ field }) => (
            <DateField
              title={{ id: 'react.productSupplier.form.lastUpdated.title', defaultMessage: 'Last Update' }}
              errorMessage={errors.lastUpdated?.message}
              {...field}
              disabled
            />
          )}
        />
        <Controller
          name="active"
          control={control}
          render={({ field }) => (
            <Switch
              titles={{
                checked: {
                  id: 'react.productSupplier.form.active.title',
                  defaultMessage: 'Active',
                },
                unchecked: {
                  id: 'react.productSupplier.form.inactive.title',
                  defaultMessage: 'Inactive',
                },
              }}
              {...field}
            />
          )}
        />
      </div>
    </Subsection>
  );
};

export default BasicDetails;

BasicDetails.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    code: PropTypes.shape({
      message: PropTypes.string,
    }),
    legacyCode: PropTypes.shape({
      message: PropTypes.string,
    }),
    dateCreated: PropTypes.shape({
      message: PropTypes.string,
    }),
    lastUpdated: PropTypes.shape({
      message: PropTypes.string,
    }),
    supplier: PropTypes.shape({
      message: PropTypes.string,
    }),
    name: PropTypes.shape({
      message: PropTypes.string,
    }),
    supplierCode: PropTypes.shape({
      message: PropTypes.string,
    }),
    product: PropTypes.shape({
      message: PropTypes.string,
    }),
  }).isRequired,
};
