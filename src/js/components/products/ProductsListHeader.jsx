import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const ProductsListHeader = ({ isUserAdmin }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.productsList.header.label" defaultMessage="Product List" />
    </span>
    {isUserAdmin &&
      <div className="d-flex justify-content-end buttons align-items-center">
        <a href="/openboxes/product/importAsCsv">
          <Button
            defaultLabel="Import products"
            label="react.productsList.importProducts.label"
            variant="primary-outline"
          />
        </a>
        <a href="/openboxes/product/create">
          <Button
            defaultLabel="Add product"
            label="react.productsList.addProduct.label"
          />
        </a>
      </div>
    }
  </div>
);

const mapStateToProps = state => ({
  isUserAdmin: state.session.isUserAdmin,
});

export default connect(mapStateToProps)(ProductsListHeader);

ProductsListHeader.propTypes = {
  isUserAdmin: PropTypes.bool.isRequired,
};

