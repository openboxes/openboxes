import React, { useState } from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

import { LOAD_DATA_STEPS } from './LoadDemoDataPage';

const LoadDemoDataWelcome = ({ skipConfiguration, goToStep }) => {
  const [loadDataOption, setLoadDataOption] = useState(null);

  return (
    <React.Fragment>
      <h3 className="font-weight-bold my-3">
        <Translate id="react.loadData.welcomeHeader.label" defaultMessage="Welcome to OpenBoxes!" />
      </h3>
      <p className="my-3">
        <Translate
          id="react.loadData.welcomeDescription.label"
          options={{ renderInnerHtml: true }}
        />
      </p>
      <div onChange={e => setLoadDataOption(e.target.value)}>
        <div>
          <input
            name="location-data"
            type="radio"
            id={LOAD_DATA_STEPS.loadDemoData}
            value={LOAD_DATA_STEPS.loadDemoData}
            checked={loadDataOption === LOAD_DATA_STEPS.loadDemoData}
          />
          <label htmlFor={LOAD_DATA_STEPS.loadDemoData} className="font-weight-bold ml-1">
            <Translate id="react.loadData.loadDemoData.label" defaultMessage="Load Demo Data" />
          </label>
          <p className="ml-4">
            <Translate id="react.loadData.loadDemoData.description.label" />
          </p>
        </div>
        <div>
          <input
            name="location-data"
            type="radio"
            id={LOAD_DATA_STEPS.createFirstLocation}
            value={LOAD_DATA_STEPS.createFirstLocation}
            checked={loadDataOption === LOAD_DATA_STEPS.createFirstLocation}
          />
          <label htmlFor={LOAD_DATA_STEPS.createFirstLocation} className="font-weight-bold ml-1">
            <Translate
              id="react.loadData.createFirstLocation.label"
              defaultMessage="Create your First Location"
            />
          </label>
          <p className="ml-4">
            <Translate id="react.loadData.createFirstLocation.description.label" />
          </p>
        </div>
      </div>
      <div className="d-flex justify-content-between align-items-center m-3">
        <a href="#" onClick={skipConfiguration} className="btn btn-link">
          <Translate id="react.loadData.skipForNow.label" defaultMessage="Skip for now" />
        </a>
        <button
          disabled={!loadDataOption}
          onClick={() => goToStep(loadDataOption)}
          type="button"
          className="btn btn-outline-primary"
        >
          <Translate id="default.button.next.label" defaultMessage="Next" />
        </button>
      </div>
    </React.Fragment>
  );
};

LoadDemoDataWelcome.propTypes = {
  skipConfiguration: PropTypes.string.isRequired,
  goToStep: PropTypes.string.isRequired,
};

export default LoadDemoDataWelcome;
