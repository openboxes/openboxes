package org.pih.warehouse.integration.ftp

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.xfer.FileSystemFile


class SecureFtpClient {

    private String server
    private int port
    private String user
    private String password
    private SSHClient sshClient
    private SFTPClient sftpClient

    SecureFtpClient(String server, int port, String user, String password) {
        this.server = server
        this.port = port
        this.user = user
        this.password = password
    }

    void connect() throws IOException {
        sshClient = new SSHClient()
        sshClient.loadKnownHosts()
        sshClient.connect(server)
        sshClient.authPassword(user, password)
        sftpClient = sshClient.newSFTPClient();
    }

    void disconnect() throws IOException {
        sftpClient.close()
    }

    Collection<String> listFiles(String path) {
        List<RemoteResourceInfo> files = sftpClient.ls(path)
        def filenames = files.collect { RemoteResourceInfo remoteResourceInfo ->
            return remoteResourceInfo.name
        }
        return filenames
    }

    void retrieveFile(String source, String destination) {
        sftpClient.get(source, new FileSystemFile(destination))
    }

    InputStream retrieveFileAsInputStream(String source) {
        RemoteFile remoteFile = sftpClient.SFTPEngine.open(source)
        return new RemoteFile.RemoteFileInputStream(remoteFile)
    }


    void storeFile(File file, String path) {
       sftpClient.put(new FileSystemFile(file.path), path);
    }

}
