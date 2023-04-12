import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import { useDispatch } from 'react-redux';

import { closeNewFeatureBar } from 'actions';
import NewFeatureBarTitle from 'components/newFeaturesInfo/NewFeatureBarTitle';
import NewFeatureBarVersionBox from 'components/newFeaturesInfo/NewFeatureBarVersionBox';
import useTranslation from 'hooks/useTranslation';

const NewFeatureBar = ({
  name,
  versionLabel,
  title,
}) => {
  useTranslation('newFeature');
  const dispatch = useDispatch();

  return (
    <div className="new-feature-bar">
      <div className="d-flex justify-content-center gap-8 align-items-center">
        <NewFeatureBarVersionBox versionLabel={versionLabel} />
        <NewFeatureBarTitle title={title} />
      </div>
      <RiCloseFill onClick={() => dispatch(closeNewFeatureBar(name))} cursor="pointer" />
    </div>
  );
};

export default NewFeatureBar;

NewFeatureBar.propTypes = {
  name: PropTypes.string.isRequired,
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
  versionLabel: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
};
