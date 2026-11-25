import React, { useState } from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';

import Button from 'components/form-elements/Button';
import useTranslate from 'hooks/useTranslate';
import Textarea from 'utils/Textarea';
import Translate from 'utils/Translate';

// Mirrors the ReceiptItem.comment domain constraint (maxSize: 255).
const COMMENT_MAX_LENGTH = 255;

/**
 * A single-field comment editor rendered as a popover anchored under the element that opened it
 * (see `anchor`). The caller mounts it only while open, so the draft is seeded from `initialValue`
 * on each open. Presentational only: saving is delegated to `onSave`, which also closes it.
 */
const CommentModal = ({
  onClose, anchor, initialValue, onSave,
}) => {
  const translate = useTranslate();
  const [value, setValue] = useState(initialValue);

  // `className`/`overlayClassName` drop react-modal's default inline styles, so the popover is
  // fully styled in main.scss - only the anchor coordinates need to be applied inline.
  const contentStyle = anchor ? { top: anchor.top, right: anchor.right } : {};

  return (
    <Modal
      isOpen
      onRequestClose={onClose}
      className="comment-modal"
      overlayClassName="comment-modal__overlay"
      style={{ content: contentStyle, overlay: { backgroundColor: 'red' } }}
    >
      <div className="comment-modal__body" data-testid="comment-modal">
        <span className="comment-modal__title">
          <Translate id="react.default.comment.label" defaultMessage="Comment" />
        </span>
        <Textarea
          value={value}
          onChange={setValue}
          rows={4}
          maxLength={COMMENT_MAX_LENGTH}
          isResizable={false}
          placeholder={translate('react.default.comment.placeholder.label', 'Add a comment...')}
        />
        <div className="comment-modal__actions">
          <Button
            label="react.default.button.cancel.label"
            defaultLabel="Cancel"
            variant="transparent"
            onClick={onClose}
          />
          <Button
            label="react.default.button.save.label"
            defaultLabel="Save"
            variant="primary"
            onClick={() => onSave(value.trim())}
          />
        </div>
      </div>
    </Modal>
  );
};

CommentModal.propTypes = {
  onClose: PropTypes.func.isRequired,
  anchor: PropTypes.shape({
    top: PropTypes.number,
    right: PropTypes.number,
  }),
  initialValue: PropTypes.string,
  onSave: PropTypes.func.isRequired,
};

CommentModal.defaultProps = {
  anchor: null,
  initialValue: '',
};

export default CommentModal;
