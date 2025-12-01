import React, { memo } from 'react';

import PropTypes from 'prop-types';
import {
  RiAddLine,
  RiCloseCircleLine,
  RiDownload2Line,
  RiPictureInPictureExitLine,
  RiRefreshLine,
  RiSave2Line,
  RiUpload2Line,
} from 'react-icons/ri';

import Button from 'components/form-elements/Button';

const InboundAddItemsHeader = ({
  addNewLine,
  importTemplate,
  exportTemplate,
  refresh,
  save,
  saveAndExit,
  removeAllRows,
  hasErrors,
}) => (
  <div className="d-flex justify-content-between align-items-center mb-3">
    <Button
      onClick={addNewLine}
      StartIcon={<RiAddLine className="icon" />}
      defaultLabel="Add line"
      label="react.default.button.addLine.label"
    />

    <div className="buttons-container">
      <Button
        onClick={() => {
          const fileInput = document.getElementById('csvInput');
          // Reset value to null to ensure onChange triggers even if the same file is selected again
          fileInput.value = null;
          fileInput?.click();
        }}
        StartIcon={<RiDownload2Line className="icon" />}
        defaultLabel="Import template"
        label="react.default.button.importTemplate.label"
        variant="primary-outline"
      />

      <input
        id="csvInput"
        type="file"
        className="d-none"
        onChange={importTemplate}
        accept=".csv"
      />

      <Button
        onClick={exportTemplate}
        StartIcon={<RiUpload2Line className="icon" />}
        defaultLabel="Export template"
        label="react.default.button.exportTemplate.label"
        variant="primary-outline"
      />

      <Button
        onClick={refresh}
        StartIcon={<RiRefreshLine className="icon" />}
        defaultLabel="Reload"
        label="react.default.button.refresh.label"
        variant="primary-outline"
      />

      <Button
        onClick={save}
        StartIcon={<RiSave2Line className="icon" />}
        defaultLabel="Save"
        label="react.default.button.save.label"
        variant="primary-outline"
        disabled={hasErrors}
      />

      <Button
        onClick={saveAndExit}
        StartIcon={<RiPictureInPictureExitLine className="icon" />}
        defaultLabel="Save And Exit"
        label="react.default.button.saveAndExit.label"
        variant="primary-outline"
        disabled={hasErrors}
      />

      <Button
        onClick={removeAllRows}
        StartIcon={<RiCloseCircleLine className="icon" />}
        defaultLabel="Delete All"
        label="react.default.button.deleteAll.label"
        variant="primary-outline"
        disabled={hasErrors}
      />
    </div>
  </div>
);

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
