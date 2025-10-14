import React, { useMemo, useState } from 'react';

import { useSelector } from 'react-redux';
import { getBinLocations } from 'selectors';

import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';
import { getBinLocationToDisplay, groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';

const BinLocationCell = ({
  initialValue,
  row,
  showBinLocation,
}) => {
  const translate = useTranslate();
  const binLocations = useSelector(getBinLocations);

  const [value, setValue] = useState(initialValue ? { name: initialValue } : undefined);
  const { id } = row.original;

  const onChange = (selected) => {
    setValue(selected);
  };

  if (!showBinLocation) {
    return null;
  }

  const selectOptions = useMemo(() =>
    groupBinLocationsByZone(binLocations, translate), []);

  const isDisabled = useMemo(() =>
    !id.includes('newRow'), []);

  return (
    <TableCell className="rt-td rt-td-count-step pb-0">
      <SelectField
        value={{ ...value, name: getBinLocationToDisplay(value) }}
        labelKey="name"
        options={selectOptions}
        onChange={onChange}
        disabled={isDisabled}
      />
    </TableCell>
  );
};

export default BinLocationCell;
