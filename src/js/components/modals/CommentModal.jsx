import React from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';

import Button from 'components/form-elements/Button';

// TODO: for now this only opens the modal. Real implementation will be done in OBPIH-7849.
const CommentModal = ({ isOpen, onClose }) => {
  if (!isOpen) {
    return null;
  }

  return (
    <Modal isOpen={isOpen} className="modal-content">
      <div data-testid="receiving-comment-modal">
        {/* Comment dialog content (editable/deletable comments) - OBPIH-7849 */}
        <Button
          label="react.default.button.close.label"
          defaultLabel="Close"
          variant="secondary"
          onClick={onClose}
        />
      </div>
    </Modal>
  );
};

CommentModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default CommentModal;
