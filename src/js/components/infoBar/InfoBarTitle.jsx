import React from 'react';

import PropTypes from 'prop-types';
import { useDispatch } from 'react-redux';

import { showInfoBarModal } from 'actions';
import Translate from 'utils/Translate';

const InfoBarTitle = ({ title, name }) => {
  const dispatch = useDispatch();
  return (
    <>
      <Translate id={title?.label} defaultMessage={title?.defaultLabel} />
      <span
        className="read-more-label"
        onClick={() => dispatch(showInfoBarModal(name))}
        role="button"
        onKeyDown={() => dispatch(showInfoBarModal(name))}
        tabIndex={0}
      >
        <Translate id="react.infoBar.readMore.label" defaultMessage="Read more" />
      </span>
    </>
  );
};


export default InfoBarTitle;


InfoBarTitle.propTypes = {
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
  name: PropTypes.string.isRequired,
};
