import React from 'react';

import PropTypes from 'prop-types';

const WizardPage = ({
  pageList,
  nextPage,
  prevPage,
  goToPage,
  currentPage,
  initialValues = {},
  additionalProps = {},
}) => {
  const Component = pageList[currentPage - 1];
  return (
    <div className="panel-body m-4" data-testid="wizardPage">
      <Component
        initialValues={initialValues}
        nextPage={nextPage}
        previousPage={prevPage}
        goToPage={goToPage}
        {...additionalProps}
      />
    </div>
  );
};

export default WizardPage;

WizardPage.propTypes = {
  pageList: PropTypes.arrayOf(PropTypes.func).isRequired,
  nextPage: PropTypes.func.isRequired,
  prevPage: PropTypes.func.isRequired,
  goToPage: PropTypes.func.isRequired,
  currentPage: PropTypes.number.isRequired,
  initialValues: PropTypes.shape({}),
  additionalProps: PropTypes.shape({}),
};
