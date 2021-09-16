package org.pih.warehouse.integration.ftp

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteResourceFilter
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.SFTPException
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.InMemorySourceFile
import org.apache.commons.io.IOUtils
import org.apache.commons.logging.LogFactory

import java.nio.charset.StandardCharsets


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

    final RemoteResourceFilter directoryFilter = new RemoteResourceFilter() {
            @Override
            boolean accept(RemoteResourceInfo resource) {
                return resource.isDirectory()
            }
        }

    final RemoteResourceFilter xmlFileFilter = new RemoteResourceFilter() {
        @Override
        boolean accept(RemoteResourceInfo resource) {
            return resource.name.endsWith(".xml") && resource.isRegularFile()
        }
    }

    final Closure remoteResourceInfoClosure = { RemoteResourceInfo remoteResourceInfo ->
        return [
                id           : remoteResourceInfo.name,
                uid          : remoteResourceInfo.attributes.UID,
                gid          : remoteResourceInfo.attributes.GID,
                name         : remoteResourceInfo.name,
                parent       : remoteResourceInfo.parent,
                path         : remoteResourceInfo.path,
                isDirectory  : remoteResourceInfo.isDirectory(),
                isRegularFile: remoteResourceInfo.isRegularFile(),
                atime        : new Date(remoteResourceInfo.attributes.atime * 1000),
                mtime        : new Date(remoteResourceInfo.attributes.mtime * 1000),
        ]
    }

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
            List<RemoteResourceInfo> files = sftpClient.ls(path, xmlFileFilter)
            LOG.info "Found ${files.size()} files"
            return files.collect(remoteResourceInfoClosure)
        } catch (SFTPException e) {
            LOG.error("SFTP exception while listing remote from ${path} due to error ${e.statusCode} ${e.message}", e)
        } catch(Exception e) {
            LOG.error("Exception while listing remote from ${path}: " + e.message, e)
        } finally {
            //sftpClient.close()
        }
    }

    Collection listFilesInSubdirectories(String path, List<String> subdirectories) {
        try {
            LOG.info "Listing directories from remote ${path}"
            List<RemoteResourceInfo> directories = sftpClient.ls(path, directoryFilter)
            LOG.info "Found ${directories.size()} directories: " + directories*.name
            LOG.info "Checking in ${subdirectories}"
            directories = directories.findAll { RemoteResourceInfo directoryInfo ->
                System.out.println "directoryInfo.name ${directoryInfo.name} in ${subdirectories}: " + (directoryInfo.name in subdirectories)
                return directoryInfo.name in subdirectories
            }
            LOG.info "Found ${directories.size()} directories"

            List<RemoteResourceInfo> files = directories.collect { RemoteResourceInfo remoteDirectory ->
                List<RemoteResourceInfo> files = sftpClient.ls("${path}/${remoteDirectory.name}")
                System.out.println "Found ${files.size()} files in ${remoteDirectory.name}"
                return files.collect(remoteResourceInfoClosure)
            }
            return files.flatten()

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

    String retrieveFileAsString(String source) {
        try {
            LOG.info "Retrieve remote ${source}"
            InputStream inputStream = retrieveFileAsInputStream(source)
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())
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
