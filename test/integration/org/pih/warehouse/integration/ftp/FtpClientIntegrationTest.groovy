package org.pih.warehouse.integration.ftp

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockftpserver.fake.FakeFtpServer
import org.mockftpserver.fake.UserAccount
import org.mockftpserver.fake.filesystem.DirectoryEntry
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem

class FtpClientIntegrationTest extends GroovyTestCase {

    FakeFtpServer fakeFtpServer

    FtpClient ftpClient

    @Before
    public void setup() {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/messages"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/messages"));
        fileSystem.add(new FileEntry("/messages/status.xml", "abcdef 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);
        fakeFtpServer.start();

        ftpClient = new FtpClient("localhost", fakeFtpServer.serverControlPort, "user", "password");
        ftpClient.connect();
    }

    @After
    public void teardown() throws IOException {
        ftpClient.disconnect();
        fakeFtpServer.stop();
    }

    @Test
    public void shouldListFilesOnRemoteServer() throws IOException {
        Collection<String> files = ftpClient.listFiles("")
        assert files.contains("status.xml")
    }

    @Test
    public void shouldRetrieveFileFromRemoteServer() throws IOException {
        ftpClient.retrieveFile("/RemoteFile.txt", "DownloadedFile.txt")
        assertTrue new File("DownloadedFile.txt").exists()
        new File("DownloadedFile.txt").delete()
    }

    @Test
    public void shouldReturnFalseWhenFileDoesNotExist() {
        assertFalse ftpClient.retrieveFile("NoSuchFile.txt", "NoPlaceToGo.txt");
    }

    @Test
    public void givenLocalFile_whenUploadingIt_thenItExistsOnRemoteLocation() {
        File file = new File(this.class.classLoader.getResource("resources/LocalFile.txt").toURI());
        ftpClient.storeFile(file, "/RemoteFile.txt");
        assertTrue fakeFtpServer.fileSystem.exists("/RemoteFile.txt")
    }

}
