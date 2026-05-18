import React, { useState } from 'react';

import PropTypes from 'prop-types';

import DocumentButton from 'components/DocumentButton';
import Translate from 'utils/Translate';

const DocumentsDropdown = ({ documents }) => {
  const [isDropdownVisible, setIsDropdownVisible] = useState(false);

  const toggleDocumentDropdown = () => setIsDropdownVisible((prev) => !prev);

  return (
    <div className="dropdown">
      <button
        type="button"
        onClick={toggleDocumentDropdown}
        className="dropdown-button float-right mb-1 btn btn-outline-secondary align-self-end btn-xs mr-1"
      >
        <span>
          <i className="fa fa-sign-out pr-2" />
          <Translate id="react.default.button.download.label" defaultMessage="Download" />
        </span>
      </button>
      <div className={`dropdown-content print-buttons-container col-md-3 flex-grow-1
        ${isDropdownVisible ? 'visible' : ''}`}
      >
        {documents.length ? documents.map((document) => {
          if (document.hidden) {
            return null;
          }
          return (
            <DocumentButton
              link={document.uri}
              buttonTitle={document.name}
              {...document}
              key={document.name}
            />
          );
        }) : null}
      </div>
    </div>
  );
};

export default DocumentsDropdown;

DocumentsDropdown.propTypes = {
  documents: PropTypes.arrayOf(PropTypes.shape({
    name: PropTypes.string,
    uri: PropTypes.string,
    hidden: PropTypes.bool,
  })),
};

DocumentsDropdown.defaultProps = {
  documents: [],
};
