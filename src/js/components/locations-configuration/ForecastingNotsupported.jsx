import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';


const ForecastingNotsupported = ({ previousPage }) => (
  <React.Fragment>
    <div className="classic-form with-description forecasting">
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
        <Translate id="react.default.button.finish.label" defaultMessage="Finish" />
      </button>
    </div>
  </React.Fragment>
);

export default ForecastingNotsupported;

ForecastingNotsupported.propTypes = {
  previousPage: PropTypes.func.isRequired,
};
