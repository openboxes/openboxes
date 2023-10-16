import React, { useCallback } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { connect } from 'react-redux';

import SelectField from 'components/form-elements/SelectField';
import TextareaField from 'components/form-elements/TextareaField';
import RejectRequestModalButtons from 'components/stock-movement/modals/RejectRequestModalButtons';
import { renderFormField } from 'utils/form-utils';
import { debouncePeopleFetch } from 'utils/option-utils';

const FIELDS = {
  recipient: {
    type: SelectField,
    label: 'react.rejectRequestModal.to.label',
    defaultMessage: 'To',
    attributes: {
      async: true,
      valueKey: 'id',
      labelKey: 'name',
      className: 'request-modal-select',
      options: [],
      filterOptions: options => options,
    },
    getDynamicAttr: ({ debouncedPeopleFetch }) => ({
      loadOptions: debouncedPeopleFetch,
    }),
  },
  sender: {
    type: SelectField,
    label: 'react.rejectRequestModal.from.label',
    defaultMessage: 'From',
    attributes: {
      className: 'request-modal-select',
      disabled: true,
    },
  },
  comment: {
    type: TextareaField,
    label: 'react.rejectRequestModal.comment.label',
    defaultMessage: 'Comment',
    attributes: {
      className: 'request-modal-textarea',
      required: true,
    },
  },
};

const RejectRequestModalContent = ({
  currentUser,
  requestor,
  debounceTime,
  minSearchLength,
  closeRejectionModal,
  rejectRequest,
}) => {
  const initialValues = {
    sender: {
      id: currentUser?.id,
      label: currentUser?.name,
    },
    recipient: {
      id: requestor?.id,
      label: requestor?.name,
    },
  };

  const submitRejection = (value) => {
    rejectRequest(value);
    closeRejectionModal();
  };

  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const validate = values => (!values?.comment ? { comment: 'react.default.error.requiredField.label' } : {});

  return (
    <Form
      onSubmit={submitRejection}
      validate={validate}
      initialValues={initialValues}
      render={({ handleSubmit, values }) =>
      (
        <form id="modalForm" onSubmit={handleSubmit}>
          <div className="classic-form location-field-rows">
            {_.map(
              FIELDS,
              (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                values, debouncedPeopleFetch,
              }),
            )}
          </div>
          <RejectRequestModalButtons closeRejectionModal={closeRejectionModal} />
        </form>
      )
    }
    />
  );
};

const mapStateToProps = state => ({
  currentUser: state.session.user,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

export default connect(mapStateToProps)(RejectRequestModalContent);

RejectRequestModalContent.propTypes = {
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  requestor: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  closeRejectionModal: PropTypes.func.isRequired,
  rejectRequest: PropTypes.func.isRequired,
};
