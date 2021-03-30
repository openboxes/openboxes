import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import { showSpinner, hideSpinner } from '../../actions';
import ModalWrapper from '../form-elements/ModalWrapper';

/* eslint-disable */
class InvoiceItemsModal extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  onOpen() {
  }

  onSave() {
  }

  render() {
    const {
      btnOpenText, btnOpenDefaultText,
    } = this.props;
    return (
      <ModalWrapper
        onOpen={this.onOpen}
        onSave={this.onSave}
        title="react.invoice.addInvoiceItems.label"
        defaultTitleMessage="Add invoice items"
        btnSaveText="react.invoice.addInvoiceItems.label"
        btnSaveDefaultText="Add invocie items"
        btnOpenText={btnOpenText}
        btnOpenDefaultText={btnOpenDefaultText}
      >
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({});

export default (connect(mapStateToProps, { showSpinner, hideSpinner })(InvoiceItemsModal));

InvoiceItemsModal.propTypes = {
  btnOpenText: PropTypes.string,
  btnOpenDefaultText: PropTypes.string,
};

InvoiceItemsModal.defaultProps = {
  btnOpenText: '',
  btnOpenDefaultText: '',
};
