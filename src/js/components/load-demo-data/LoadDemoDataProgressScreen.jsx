import React, { useEffect, useState } from 'react';

import { ClimbingBoxLoader } from 'react-spinners';

import LoadDemoDataErrorMessage from 'components/load-demo-data/LoadDemoDataErrorMessage';
import LoadDemoDataSuccessMessage from 'components/load-demo-data/LoadDemoDataSuccessMessage';
import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';

const LoadDemoDataProgressScreen = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [supportLinks, setSupportLinks] = useState({});

  useEffect(() => {
    apiClient.get('/openboxes/api/config/data/demo')
      .then(() => {
        setIsLoading(false);
      })
      .catch((err) => {
        setError(err);
        setIsLoading(false);
      });

    apiClient.get('/openboxes/api/supportLinks').then((response) => {
      const links = response.data.data;
      setSupportLinks(links);
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
      <LoadDemoDataErrorMessage supportLinks={supportLinks} />
    );
  }
  return (
    <LoadDemoDataSuccessMessage supportLinks={supportLinks} />
  );
};


export default LoadDemoDataProgressScreen;
