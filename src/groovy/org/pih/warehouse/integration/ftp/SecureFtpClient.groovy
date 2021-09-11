package org.pih.warehouse.integration.ftp

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.SFTPException
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.InMemorySourceFile
import org.apache.commons.logging.LogFactory


class SecureFtpClient {

    static final LOG = LogFactory.getLog(this)

    private SSHClient sshClient

    private String server
    private int port
    private String user
    private String password
    private String knownHosts
    private String directory
    private String fingerprint
    private Integer heartbeatInterval = 60
    private Boolean debug = false

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
        this.heartbeatInterval = config.heartbeatInterval?:60
        this.fingerprint = config?.fingerprint?:null
        this.knownHosts = config.knownHosts?:null
        this.directory = config?.directory?:""
        this.debug = config.debug?:false
    }


    SFTPClient getSftpClient() throws IOException {
        try {
            sshClient = new SSHClient()

            if (fingerprint) {
                LOG.info "Using host key verifier ${fingerprint}"
                sshClient.addHostKeyVerifier(fingerprint)
            }
            else {
                if (debug) {
                    LOG.info "Using promiscuous host key verifier"
                    sshClient.addHostKeyVerifier(new PromiscuousVerifier())
                }
                else {
                    // Load known hosts from configuration if unable to use ~/.ssh/known_hosts
                    if (knownHosts) {
                        LOG.info "Adding host key verifier ${knownHosts}"
                        sshClient.addHostKeyVerifier(new OpenSSHKnownHosts(new InputStreamReader(new ByteArrayInputStream(knownHosts.bytes))))
                    }
                    // Or from default ~/.ssh/known_hosts file
                    else {
                        LOG.info "Adding default host key verifiers"
                        sshClient.loadKnownHosts()
                    }
                }
            }

            sshClient.connect(server)

            // Set heartbeat interval
            sshClient.transport.heartbeatInterval = heartbeatInterval

            if (user && password) {
                sshClient.authPassword(user, password)
            }
            return sshClient.newSFTPClient()

        } catch (TransportException e) {
            LOG.error("Unable to connect to sftp server due to error: " + e.message, e)
        }
    }

    void disconnect() throws IOException {
        if (sshClient) {
            sshClient.disconnect()
        }
    }

    Collection<String> listFiles(String path) {
        try {
            LOG.info "Listing files from remote ${path}"
            List<RemoteResourceInfo> files = sftpClient.ls(path)
            LOG.info "Found ${files.size()} files"
            def filenames = files.findAll { RemoteResourceInfo remoteResourceInfo ->
                remoteResourceInfo.attributes.type == FileMode.Type.REGULAR
            }.
                    collect { RemoteResourceInfo remoteResourceInfo ->
                        return remoteResourceInfo.name
                    }
            return filenames
        } catch (SFTPException e) {
            LOG.error("SFTP exception while listing remote from ${path} due to error ${e.statusCode} ${e.message}", e)
        } catch(Exception e) {
            LOG.error("Exception while listing remote from ${path}: " + e.message, e)
        } finally {
            //sftpClient.close()
        }
    }

    void retrieveFile(String source, String destination) {
        try {
            LOG.info "Retrieve remote ${source} to local ${destination}"
            sftpClient.get(source, new FileSystemFile(destination))
        } finally {
            //sftpClient.close();
        }
    }

    InputStream retrieveFileAsInputStream(String source) {
        try {
            LOG.info "Retrieve remote ${source}"
            RemoteFile remoteFile = sftpClient.SFTPEngine.open(source)
            return new RemoteFile.RemoteFileInputStream(remoteFile)
        } finally {
            //sftpClient.close()
        }
    }

    void storeFile(File file, String path) {
        try {
            LOG.info "Put local ${file.path} to remote ${path}"
            sftpClient.put(new FileSystemFile(file.path), path);
        } finally {
            //sfptClient.close()
        }
    }


    void storeFile(String filename, String contents, String path) {
        try {
            LOG.info "Put in memory file ${filename} to remote ${path}"
            sftpClient.put(new InMemorySourceFile() {
                @Override
                public String getName() { return filename; }

                @Override
                public long getLength() { return contents?.size()?:0 }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(contents.bytes)
                }
            }, path)

        } finally {
            //sfptClient.close()
        }
    }

    void deleteFile(String filename) {
        try {
            LOG.info "Delete remote file ${filename}"
            sftpClient.rm(filename)
        } finally {
            //sfptClient.close()
        }
    }

}
