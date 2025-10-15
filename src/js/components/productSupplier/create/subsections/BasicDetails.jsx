import React from 'react';

import PropTypes from 'prop-types';
import { Controller, useWatch } from 'react-hook-form';
import { useSelector } from 'react-redux';
import {
  getFormatLocalizedDate,
} from 'selectors';

import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import Switch from 'components/form-elements/v2/Switch';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import { debounceOrganizationsFetch, debounceProductsFetch } from 'utils/option-utils';
import { FormErrorPropType } from 'utils/propTypes';
import {DateFormat} from "consts/timeFormat";

const BasicDetails = ({ control, errors, getValues }) => {
  const {
    debounceTime,
    minSearchLength,
    formatLocalizedDate,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
    formatLocalizedDate: getFormatLocalizedDate(state),
  }));

  // Watch product's input changes live, in order to display a "View Product" link
  // with a proper product id
  const product = useWatch({ control, name: 'basicDetails.product' });

  const { basicDetails } = getValues();

  console.log(getValues())

  const dateCreated = formatLocalizedDate(getValues()?.basicDetails?.dateCreated, DateFormat.MMM_DD_YYYY);

  console.log(dateCreated);

  const auditField = `${basicDetails?.createdBy ?? ''} ${dateCreated ?? ''}`

  return (
    <Subsection
      title={{ label: 'react.productSupplier.form.subsection.basicDetails', defaultMessage: 'Basic Details' }}
      collapsable={false}
    >
      <div className="row">
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="basicDetails.code"
            control={control}
            render={({ field }) => (
              <TextInput
                title={{ id: 'react.productSupplier.form.code.title', defaultMessage: 'Source Code' }}
                errorMessage={errors.code?.message}
                tooltip={{
                  id: 'react.productSupplier.form.code.tooltip',
                  defaultMessage: 'Unique code that identifies this record. '
                    + 'Auto-generated based on the product and supplier codes, '
                    + 'but can be manually overwritten',
                }}
                placeholder="Leave blank to autogenerate"
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="basicDetails.product"
            control={control}
            render={({ field }) => (
              <SelectField
                title={{ id: 'react.productSupplier.form.product.title', defaultMessage: 'Product Name' }}
                productSelect
                placeholder="Search for a product"
                required
                async
                showSelectedOptionColor
                hasErrors={Boolean(errors.product?.message)}
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
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="basicDetails.productCode"
            control={control}
            render={({ field }) => (
              <TextInput
                title={{ id: 'react.productSupplier.form.legacyCode.title', defaultMessage: 'Legacy Code' }}
                errorMessage={errors.productCode?.message}
                tooltip={{
                  id: 'react.productSupplier.form.legacyCode.tooltip',
                  defaultMessage: 'Reference to this record in a previous or parallel purchasing system',
                }}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="basicDetails.supplier"
            control={control}
            render={({ field }) => (
              <SelectField
                title={{ id: 'react.productSupplier.form.supplier.title', defaultMessage: 'Supplier' }}
                placeholder="Select Supplier"
                required
                hasErrors={Boolean(errors.supplier?.message)}
                errorMessage={errors.supplier?.message}
                tooltip={{ id: 'react.productSupplier.form.supplier.tooltip', defaultMessage: 'The company that supplies the product' }}
                async
                loadOptions={debounceOrganizationsFetch(debounceTime, minSearchLength)}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="basicDetails.supplierCode"
            control={control}
            render={({ field }) => (
              <TextInput
                title={{ id: 'react.productSupplier.form.supplierCode.title', defaultMessage: 'Supplier Code' }}
                errorMessage={errors.supplierCode?.message}
                tooltip={{
                  id: 'react.productSupplier.form.supplierCode.tooltip',
                  defaultMessage: 'The SKU used by the vendor to identify the product',
                }}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <TextInput
            value={auditField}
            disabled
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="basicDetails.name"
            control={control}
            render={({ field }) => (
              <TextInput
                title={{ id: 'react.productSupplier.column.name.label', defaultMessage: '(Source) Name' }}
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
        </div>
        <div className="col-lg-2 col-md-3 px-2 pt-2">
          <Controller
            name="basicDetails.dateCreated"
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
        </div>
        <div className="col-lg-2 col-md-3 px-2 pt-2">
          <Controller
            name="basicDetails.createdBy"
            control={control}
            disabled
            render={({ field }) => (
              <TextInput
                {...field}
                title={{ id: 'react.productSupplier.form.createdBy.title', defaultMessage: 'Created By' }}
                errorMessage={errors.createdBy?.message}
              />
            )}
          />
        </div>
        <div className="col-lg-2 col-md-3 px-2 pt-2">
          <Controller
            name="basicDetails.lastUpdated"
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
        </div>
        <div className="col-lg-2 col-md-3 px-2 pt-2">
          <Controller
            name="basicDetails.updatedBy"
            control={control}
            disabled
            render={({ field }) => (
              <TextInput
                {...field}
                title={{ id: 'react.productSupplier.form.updatedBy.title', defaultMessage: 'Last Updated By' }}
                errorMessage={errors.updatedBy?.message}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="basicDetails.active"
            control={control}
            render={({ field }) => (
              <Switch
                className="basic-details-active-switch"
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
      </div>
    </Subsection>
  );
};

export default BasicDetails;

export const basicDetailsFormErrors = PropTypes.shape({
  code: FormErrorPropType,
  product: FormErrorPropType,
  productCode: FormErrorPropType,
  supplier: FormErrorPropType,
  supplierCode: FormErrorPropType,
  name: FormErrorPropType,
  active: FormErrorPropType,
  dateCreated: FormErrorPropType,
  lastUpdated: FormErrorPropType,
  createdBy: FormErrorPropType,
  updatedBy: FormErrorPropType,
});

BasicDetails.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: basicDetailsFormErrors,
  getValues: PropTypes.func.isRequired,
};

BasicDetails.defaultProps = {
  errors: {},
};
