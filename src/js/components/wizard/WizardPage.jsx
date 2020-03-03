import React from 'react';
import PropTypes from 'prop-types';

const WizardPage = (props) => {
  const Component = props.pageList[props.currentPage - 1];
  return (
    <div className="panelBody px-1">
      <Component
        initialValues={props.initialValues}
        nextPage={props.nextPage}
        previousPage={props.prevPage}
        goToPage={props.goToPage}
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
};

WizardPage.defaultProps = {
  initialValues: {},
};
