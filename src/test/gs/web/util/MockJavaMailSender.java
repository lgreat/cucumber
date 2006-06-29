package gs.web.util;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.ArrayList;

/**
 * This class is based on the eponymous class from the Spring test code.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MockJavaMailSender extends JavaMailSenderImpl {

    private MockTransport _transport = null;

    protected Transport getTransport(Session session) throws NoSuchProviderException {
        if (_transport == null) {
            _transport = new MockTransport(session, null);
        }
        return _transport;
    }

    public List getSentMessages() {
        if (_transport != null) {
            return _transport.getSentMessages();
        }
        return null;
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