package com.livewave.ticket_api.sftp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class SftpService {

    private final SessionFactory<?> sessionFactory;

    @Value("${sftp.remote-dir}")
    private String remoteDir;

    public SftpService(SessionFactory<?> sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String upload(String filename, InputStream inputStream) throws Exception {
        try (Session<?> session = sessionFactory.getSession()) {
            String remotePath = remoteDir.endsWith("/")
                    ? remoteDir + filename
                    : remoteDir + "/" + filename;

            session.write(inputStream, remotePath);
            return remotePath;
        }
    }

    public Resource download(String remotePath) throws Exception {
        try (Session<?> session = sessionFactory.getSession()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            session.read(remotePath, out);
            return new ByteArrayResource(out.toByteArray());
        }
    }
}