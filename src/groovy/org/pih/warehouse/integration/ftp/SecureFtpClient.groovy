package org.pih.warehouse.integration.ftp

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.xfer.FileSystemFile


class SecureFtpClient {

    private String server
    private int port
    private String user
    private String password
    private String knownHosts
    private String directory
    private Integer heartbeatInterval = 30
    private SSHClient sshClient

    SecureFtpClient(String server, int port, String user, String password) {
        this.server = server
        this.port = port
        this.user = user
        this.password = password
    }

    SecureFtpClient(Map config) {
        this.server = config.server
        this.port = config.port?:22
        this.user = config.user?:null
        this.password = config.password?:null
        this.heartbeatInterval = config.heartbeatInterval?:30
        this.knownHosts = config.knownHosts
        this.directory = directory
    }



    void connect() throws IOException {
        sshClient = new SSHClient()

        // Load known hosts from configuration if unable to use ~/.ssh/known_hosts
        if (knownHosts) {
            sshClient.addHostKeyVerifier(new OpenSSHKnownHosts(new InputStreamReader(new ByteArrayInputStream(knownHosts.bytes))))
        }
        // Or from default ~/.ssh/known_hosts file
        else {
            sshClient.loadKnownHosts()
        }

        sshClient.connect(server)

        // Set heartbeat interval
        sshClient.transport.heartbeatInterval = heartbeatInterval

        if (user && password) {
            sshClient.authPassword(user, password)
        }
    }

    void disconnect() throws IOException {
        sshClient.close()
        sshClient.disconnect()
    }

    Collection<String> listFiles(String path) {
        SFTPClient sftpClient = sshClient.newSFTPClient()
        List<RemoteResourceInfo> files = sftpClient.ls(path)
        def filenames = files.collect { RemoteResourceInfo remoteResourceInfo ->
            return remoteResourceInfo.name
        }
        return filenames
    }

    void retrieveFile(String source, String destination) {
        SFTPClient sftpClient = sshClient.newSFTPClient()
        sftpClient.get(source, new FileSystemFile(destination))
        sftpClient.close()
    }

    InputStream retrieveFileAsInputStream(String source) {
        SFTPClient sftpClient = sshClient.newSFTPClient()
        RemoteFile remoteFile = sftpClient.SFTPEngine.open(source)
        return new RemoteFile.RemoteFileInputStream(remoteFile)
    }

    void storeFile(File file, String path) {
        SFTPClient sftpClient = sshClient.newSFTPClient()
        sftpClient.put(new FileSystemFile(file.path), path);
    }

}
