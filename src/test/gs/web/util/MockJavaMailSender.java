package gs.web.util;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.ArrayList;

/**
 * This was copied (slightly modified) from the Spring test classes.
 */
public class MockJavaMailSender extends JavaMailSenderImpl {

    protected Transport getTransport(Session session) throws NoSuchProviderException {
        return new MockTransport(session, null);
    }

    private static class MockTransport extends Transport {

        private String connectedHost = null;
        private int connectedPort = -2;
        private String connectedUsername = null;
        private String connectedPassword = null;
        private boolean closeCalled = false;
        private List sentMessages = new ArrayList();

        public MockTransport(Session session, URLName urlName) {
            super(session, urlName);
        }

        public String getConnectedHost() {
            return connectedHost;
        }

        public int getConnectedPort() {
            return connectedPort;
        }

        public String getConnectedUsername() {
            return connectedUsername;
        }

        public String getConnectedPassword() {
            return connectedPassword;
        }

        public boolean isCloseCalled() {
            return closeCalled;
        }

        public List getSentMessages() {
            return sentMessages;
        }

        public MimeMessage getSentMessage(int index) {
            return (MimeMessage) this.sentMessages.get(index);
        }

        public void connect(String host, int port, String username, String password) throws MessagingException {
            if (host == null) {
                throw new MessagingException("no host");
            }
            this.connectedHost = host;
            this.connectedPort = port;
            this.connectedUsername = username;
            this.connectedPassword = password;
        }

        public synchronized void close() throws MessagingException {
            this.closeCalled = true;
        }

        public void sendMessage(Message message, Address[] addresses) throws MessagingException {
            this.sentMessages.add(message);
        }
    }
}