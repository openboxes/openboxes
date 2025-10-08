import { useState } from 'react';

import _ from 'lodash';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import useWindowOpen from 'hooks/useWindowOpen';

/**
 * Custom hook that manages file upload, download, removal and export actions.
 *
 * @param {string} entityId - ID of the entity the files belong to.
 * @param {Function} onSave - Function called before exporting a document.
 * @param {boolean} isValid - React Hook Form's formState.isValid flag which determines
 * if the form is valid before export.
 * @param {Function} uploadDocuments - Async function for uploading documents.
 *
 */
const useFileActions = ({
  entityId,
  onSave,
  isValid,
  uploadDocuments,
}) => {
  // List of user selected File objects.
  // These files are stored locally until uploaded via 'sendFiles()'.
  const [files, setFiles] = useState([]);
  const spinner = useSpinner();
  const translate = useTranslate();
  const { openWindow } = useWindowOpen();

  const handleDownloadFiles = (newFiles) => {
    setFiles((prevFiles) =>
      _.unionBy([...newFiles, ...prevFiles], 'name'));
  };

  const handleRemoveFile = (fileToRemove) => {
    setFiles((prevFiles) => prevFiles.filter((file) => file.name !== fileToRemove.name));
  };

  const sendFiles = async () => {
    const data = new FormData();

    files.forEach((file, idx) => {
      data.append(`filesContents[${idx}]`, file);
    });
    await uploadDocuments(entityId, data);
    if (files.length > 1) {
      notification(NotificationType.SUCCESS)({
        message: translate(
          'react.default.alert.filesSuccess.label',
          'Files uploaded successfully!',
        ),
      });
      return;
    }
    notification(NotificationType.SUCCESS)({
      message: translate(
        'react.default.alert.fileSuccess.label',
        'File uploaded successfully!',
      ),
    });
  };

  const handleExportFile = async (document) => {
    if (!isValid || !document?.uri) {
      return;
    }

    try {
      spinner.show();
      await onSave({ showSuccessNotification: false });
      openWindow(document.uri, '_blank');
    } finally {
      spinner.hide();
    }
  };

  return {
    files,
    setFiles,
    handleDownloadFiles,
    handleRemoveFile,
    sendFiles,
    handleExportFile,
  };
};

export default useFileActions;
