import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';


// eslint-disable-next-line no-shadow
const ResettingInstanceInfoPage = ({ locale, history, fetchTranslations }) => {
  const [resettingInstanceCommand, setResettingInstanceCommand] = useState('');

  useEffect(() => {
    fetchTranslations(locale, 'resetInstance');
  }, [locale]);

  useEffect(() => {
    apiClient.get('/openboxes/api/resettingInstance/command').then((response) => {
      setResettingInstanceCommand(response.data.data);
    });
  }, []);

  return (
    <div className="modal-page">
      <div className="modal-page__content position-relative d-flex" style={{ minHeight: '200px' }}>
        <h3 className="font-weight-bold my-3">
          <Translate id="react.resetInstance.header.label" defaultMessage="Reset your instance" />
        </h3>
        <p>
          <Translate id="react.resetInstance.content.label" defaultMessage="To reset your instance request your system admin to run this script in terminal:" />
        </p>
        <p className="font-weight-bold">{resettingInstanceCommand}</p>
        <button
          onClick={() => history.push('/openboxes')}
          type="button"
          className="btn btn-outline-primary w-25 align-self-end"
        >
          <Translate id="react.resetInstance.backToDashboard.button.label" defaultMessage="Back to dashboard" />
        </button>
      </div>
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

const mapDispatchToProps = {
  fetchTranslations,
};

ResettingInstanceInfoPage.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(ResettingInstanceInfoPage));
