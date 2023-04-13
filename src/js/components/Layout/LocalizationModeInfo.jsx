import React from 'react';

import { RiGlobalLine, RiLogoutBoxRLine } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const LocalizationModeInfo = () => (
  <div className="header-indicator-box d-flex justify-content-between align-items-center">
    <div className="info d-flex align-items-center">
      <RiGlobalLine />
      <span>
        <Translate id="react.default.localizationModeActive.label" defaultMessage="The localization mode is active" />
      </span>
    </div>
    <a href="/openboxes/user/disableLocalizationMode">
      <Button
        defaultLabel="Disable Localization Mode"
        label="react.default.disableLocalizationMode.label"
        StartIcon={<RiLogoutBoxRLine />}
      />
    </a>
  </div>
);


export default LocalizationModeInfo;
