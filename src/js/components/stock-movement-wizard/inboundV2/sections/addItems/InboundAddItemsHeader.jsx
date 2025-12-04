import React, { memo } from 'react';

import PropTypes from 'prop-types';
import {
  RiAddLine,
  RiCloseCircleLine,
  RiPictureInPictureExitLine,
  RiRefreshLine,
  RiSave2Line,
  RiUpload2Line,
} from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import ButtonFileSelect from 'components/form-elements/v2/ButtonFileSelect';
import FileFormat from 'consts/fileFormat';

const InboundAddItemsHeader = ({
  addNewLine,
  importTemplate,
  exportTemplate,
  refresh,
  save,
  saveAndExit,
  removeAllRows,
  hasErrors,
}) => {
  const actionButtons = [
    {
      onClick: exportTemplate,
      StartIcon: <RiUpload2Line className="icon" />,
      defaultLabel: 'Export template',
      label: 'react.default.button.exportTemplate.label',
      variant: 'primary-outline',
    },
    {
      onClick: refresh,
      StartIcon: <RiRefreshLine className="icon" />,
      defaultLabel: 'Reload',
      label: 'react.default.button.refresh.label',
      variant: 'primary-outline',
    },
    {
      onClick: save,
      StartIcon: <RiSave2Line className="icon" />,
      defaultLabel: 'Save',
      label: 'react.default.button.save.label',
      variant: 'primary-outline',
      disabled: hasErrors,
    },
    {
      onClick: saveAndExit,
      StartIcon: <RiPictureInPictureExitLine className="icon" />,
      defaultLabel: 'Save And Exit',
      label: 'react.default.button.saveAndExit.label',
      variant: 'primary-outline',
      disabled: hasErrors,
    },
    {
      onClick: removeAllRows,
      StartIcon: <RiCloseCircleLine className="icon" />,
      defaultLabel: 'Delete All',
      label: 'react.default.button.deleteAll.label',
      variant: 'primary-outline',
      disabled: hasErrors,
    },
  ];

  return (
    <div className="d-flex justify-content-between align-items-center mb-3">
      <Button
        onClick={addNewLine}
        StartIcon={<RiAddLine className="icon" />}
        defaultLabel="Add line"
        label="react.default.button.addLine.label"
      />

      <div className="buttons-container">
        <ButtonFileSelect
          onFileUpload={importTemplate}
          defaultLabel="Import template"
          label="react.default.button.importTemplate.label"
          allowedExtensions={[FileFormat.CSV]}
          variant="primary-outline"
          className="no-transition"
        />

        {actionButtons.map((button) => (
          <Button
            key={button.label}
            onClick={button.onClick}
            StartIcon={button.StartIcon}
            defaultLabel={button.defaultLabel}
            label={button.label}
            variant={button.variant}
            disabled={button.disabled}
          />
        ))}
      </div>
    </div>
  );
};

InboundAddItemsHeader.propTypes = {
  addNewLine: PropTypes.func.isRequired,
  importTemplate: PropTypes.func.isRequired,
  exportTemplate: PropTypes.func.isRequired,
  refresh: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
  saveAndExit: PropTypes.func.isRequired,
  removeAllRows: PropTypes.func.isRequired,
  hasErrors: PropTypes.bool.isRequired,
};

export default memo(InboundAddItemsHeader);
