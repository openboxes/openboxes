import React from 'react';

import AutosaveFeatureModalStatusParagraph
  from 'components/infoBar/modals/autosave/AutosaveFeatureModalStatusParagraph';
import { AutosaveStatus } from 'consts/autosaveStatuses';
import ImageUrl from 'consts/imagesUrls';
import Translate from 'utils/Translate';

const AutosaveFeatureModalContent = () => (
  <div className="content">
    <div>
      <p>
        <Translate
          id="react.autosaveFeatureModal.content.paragraph2.label"
          defaultMessage="Every completed item will be saved automatically. Each row has now an indicator helping you track the autosaved items:"
        />
      </p>
    </div>
    <div>
      {Object.values(AutosaveStatus).map(status => (
        <AutosaveFeatureModalStatusParagraph status={status} />
      ))}
    </div>
    <img src={ImageUrl.AUTOSAVE_FEATURE_BAR_CONTENT} alt="Outbound table" />
  </div>
);

export default AutosaveFeatureModalContent;
