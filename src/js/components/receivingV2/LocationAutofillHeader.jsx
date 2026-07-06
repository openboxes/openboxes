import React from 'react';

import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import SelectField from 'components/form-elements/v2/SelectField';
import receivingLocationOptions from 'consts/receivingLocationOptions';
import useTranslate from 'hooks/useTranslate';

// TODO: for now this is display only, selecting an option does nothing.
// Real implementation will be done in OBPIH-7913.
const LocationAutofillHeader = () => {
  const translate = useTranslate();

  return (
    <TableHeaderCell
      className="location-header-cell"
      tooltip
      tooltipLabel={translate('react.receiving.location.label', 'Location')}
    >
      <SelectField
        options={receivingLocationOptions}
        labelKey="name"
        placeholder={(
          <span className="location-header__placeholder">
            <span className="location-header__location">
              {translate('react.receiving.location.label', 'Location')}
            </span>
            <span className="location-header__autofill">
              {translate('react.receiving.autofill.label', 'Autofill')}
            </span>
            <span className="location-header__caret" aria-hidden="true" />
          </span>
        )}
        className="location-header"
        hideErrorMessageWrapper
        controlShouldRenderValue={false}
        isSearchable={false}
      />
    </TableHeaderCell>
  );
};

export default LocationAutofillHeader;
