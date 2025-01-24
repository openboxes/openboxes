import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import { DASHBOARD_URL } from 'consts/applicationUrls';
import useTranslation from 'hooks/useTranslation';
import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';

// eslint-disable-next-line no-shadow
const ResettingInstanceInfoPage = ({ history }) => {
  const [resettingInstanceCommand, setResettingInstanceCommand] = useState('');

  useTranslation('resetInstance');

  useEffect(() => {
    apiClient.get('/api/resettingInstance/command').then((response) => {
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
          onClick={() => history.push(DASHBOARD_URL.base)}
          type="button"
          className="btn btn-outline-primary w-25 align-self-end"
        >
          <Translate id="react.resetInstance.backToDashboard.button.label" defaultMessage="Back to dashboard" />
        </button>
      </div>
    </div>
  );
};

ResettingInstanceInfoPage.propTypes = {
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};

export default withRouter(ResettingInstanceInfoPage);
