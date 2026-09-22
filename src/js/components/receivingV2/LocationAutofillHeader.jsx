import React from 'react';

import PropTypes from 'prop-types';

import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import SelectField from 'components/form-elements/v2/SelectField';
import receivingLocationOptions from 'consts/receivingLocationOptions';
import useTranslate from 'hooks/useTranslate';

const LocationAutofillHeader = ({ onSelect }) => {
  const translate = useTranslate();

  return (
    <TableHeaderCell
      className="location-header-cell"
      tooltip
      tooltipLabel={translate('react.receiving.location.label', 'Location')}
    >
      <SelectField
        options={receivingLocationOptions(translate)}
        labelKey="name"
        onChange={(option) => option && onSelect?.(option.id)}
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

LocationAutofillHeader.propTypes = {
  onSelect: PropTypes.func,
};

LocationAutofillHeader.defaultProps = {
  onSelect: undefined,
};

export default LocationAutofillHeader;
