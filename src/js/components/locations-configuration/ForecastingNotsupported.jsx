import React from 'react';

import PropTypes from 'prop-types';
import Alert from 'react-s-alert';

import Translate from 'utils/Translate';

const PAGE_ID = 'forecasting';

const ForecastingNotsupported = ({ previousPage, supportLinks }) => (
  <React.Fragment>
    <div className="classic-form with-description forecasting">
      <div className="submit-buttons">
        <button type="button" onClick={() => Alert.info(supportLinks[PAGE_ID])} className="btn btn-outline-primary float-right btn-xs">
          <i className="fa fa-question-circle-o" aria-hidden="true" />
          &nbsp;
          <Translate id="react.default.button.support.label" defaultMessage="Support" />
        </button>
      </div>
      <div className="form-title">
        <Translate id="react.locationsConfiguration.forecasting.label" defaultMessage="Forecasting" />
      </div>
      <div className="form-subtitle">
        <Translate
          id="react.locationsConfiguration.forecast.notsupported.label"
          defaultMessage="This section is not supported for the chosen Location Type."
        />
      </div>
    </div>
    <div className="submit-buttons">
      <button type="button" onClick={previousPage} className="btn btn-outline-primary float-left btn-xs">
        <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
      </button>
      <button type="submit" className="btn btn-outline-primary float-right btn-xs">
        <Translate id="react.default.button.next.label" defaultMessage="Next" />
      </button>
    </div>
  </React.Fragment>
);

export default ForecastingNotsupported;

ForecastingNotsupported.propTypes = {
  previousPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
};
