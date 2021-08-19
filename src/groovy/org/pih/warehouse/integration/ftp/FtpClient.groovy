package org.pih.warehouse.integration.ftp

import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient

class FtpClient {

    private String server
    private int port
    private String user
    private String password
    private FTPClient ftp

    FtpClient(String server, int port, String user, String password) {
        this.server = server
        this.port = port
        this.user = user
        this.password = password
    }

    void connect() throws IOException {
        ftp = new FTPClient()

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)))

        ftp.connect(server, port)
        int reply = ftp.getReplyCode()
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server")
        }

        ftp.login(user, password)
    }

    void disconnect() throws IOException {
        ftp.disconnect()
    }

    Collection<String> listFiles(String path) {
        FTPFile[] files = ftp.listFiles(path);
        def filenames = files.collect { FTPFile ftpFile ->
            return ftpFile.name
        }
        return filenames
    }

    boolean retrieveFile(String source, String destination) {
        FileOutputStream out = new FileOutputStream(destination)
        return ftp.retrieveFile(source, out)
    }

    boolean storeFile(File file, String path) {
       return ftp.storeFile(path, new FileInputStream(file));
    }

}
