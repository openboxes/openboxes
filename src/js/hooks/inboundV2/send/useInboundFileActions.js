import { useState } from 'react';

import _ from 'lodash';

import stockMovementApi from 'api/services/StockMovementApi';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useTranslate from 'hooks/useTranslate';
import useWindowOpen from 'hooks/useWindowOpen';

const useInboundFileActions = ({
  stockMovementId,
  onSave,
  spinner,
  isValid,
}) => {
  // List of user selected File objects (documents to attach to the current stock movement).
  // These files are stored locally until uploaded via 'sendFiles()'.
  const [files, setFiles] = useState([]);
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
    await stockMovementApi.uploadDocuments(stockMovementId, data);
    if (files.length > 1) {
      notification(NotificationType.SUCCESS)({
        message: translate(
          'react.stockMovement.alert.filesSuccess.label',
          'Files uploaded successfully!',
        ),
      });
      return;
    }
    notification(NotificationType.SUCCESS)({
      message: translate(
        'react.stockMovement.alert.fileSuccess.label',
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

export default useInboundFileActions;
