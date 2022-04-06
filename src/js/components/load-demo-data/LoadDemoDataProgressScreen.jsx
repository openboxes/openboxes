import React, { useEffect, useState } from 'react';

import { ClimbingBoxLoader } from 'react-spinners';

import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';

const LoadDemoDataProgressScreen = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    apiClient.get('/openboxes/api/config/data/demo')
      .then(() => {
        setIsLoading(false);
      })
      .catch((err) => {
        setError(err);
        setIsLoading(false);
      });
  }, []);

  if (isLoading) {
    return (
      <React.Fragment>
        <h3 className="font-weight-bold my-3">
          <Translate
            id="react.loadData.loadingData.label"
            defaultMessage="react.loadData.loadingData.label"
          />
        </h3>
        <p className="my-3">
          <Translate
            id="react.loadData.loadingDataMessage.label"
            defaultMessage="Please wait while we load your demo data. Do not close this window."
          />
        </p>
        <div className="d-flex flex-column align-items-center justify-content-center">
          <div className="my-3">
            <ClimbingBoxLoader />
          </div>
          <p className="font-weight-bold my-3">
            <Translate
              id="react.loadData.importingDemoFiles.label"
              defaultMessage="Importing demo files..."
            />
          </p>
        </div>
      </React.Fragment>
    );
  }

  if (error) {
    return (
      <div className="d-flex flex-column align-items-center justify-content-center">
        <h3>Error while loading data </h3>
      </div>
    );
  }
  // TODO: create a success message
  return (
    <div className="d-flex flex-column align-items-center justify-content-center">
      <h3>Data Successfully Loaded </h3>
    </div>
  );
};


export default LoadDemoDataProgressScreen;
