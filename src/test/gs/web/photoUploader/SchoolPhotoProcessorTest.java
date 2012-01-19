package gs.web.photoUploader;

import gs.data.community.User;
import gs.web.util.ClientHttpRequest;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemStream;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.*;


public class SchoolPhotoProcessorTest {
    private ClientHttpRequest _clientHttpRequest;
    private SchoolPhotoProcessor _processor;
    private MockSchoolPhotoProcessor _mockSchoolPhotoProcessor;


    class MockSchoolPhotoProcessor extends SchoolPhotoProcessor {
        public MockSchoolPhotoProcessor(FileItemStream fileItemStream) throws IOException {
            super(fileItemStream);
        }

        public MockSchoolPhotoProcessor(FileItemStream fileItemStream, URL destinationUrl) {
            super(fileItemStream, destinationUrl);
        }

        @Override
        public URL getDestinationUrl() {
            try {
                return new URL("http://localhost:8080");
            } catch (MalformedURLException e) {
                throw new RuntimeException("test broken");
            }
        }
    }

    @Before
    public void setUp() throws IOException {
        _processor = new SchoolPhotoProcessor(new MockFileItemStream()) {
            @Override
            public URL getDestinationUrl() {
                try {
                    return new URL("http://localhost");
                } catch (MalformedURLException e) {
                    throw new RuntimeException("test broken");
                }
            }
        };

        _clientHttpRequest = org.easymock.classextension.EasyMock.createStrictMock(ClientHttpRequest.class);
    }

    @Test
    public void testFinish() throws Exception {
        final Field field = _processor.getClass().getSuperclass().getDeclaredField("_stream");
        field.setAccessible(true);
        BufferedInputStream stream = (BufferedInputStream) field.get(_processor);

        assertTrue(stream.available() > 0);

        _processor.finish();

        try {
            stream.available();
            fail("Exception should have been thrown since stream already closed");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("closed"));
            // good
        }
    }

    public void xtestBuildClientHttpRequest() throws IOException {
        User user = new User();
        user.setId(1);
        int schoolMediaId = 1;

        ClientHttpRequest request = _processor.createClientHttpRequestForEsp(user, schoolMediaId);
        assertNotNull(request);

        try {
            request = _processor.createClientHttpRequestForEsp(user, -1);
            fail("Exception should have been thrown");
        } catch (Exception e) {
            // good
        }

        try {
            request = _processor.createClientHttpRequestForEsp(null, schoolMediaId);
            fail("Exception should have been thrown");
        } catch (Exception e) {
            // good
        }

        try {
            request = _processor.createClientHttpRequestForEsp(user, schoolMediaId);
        } catch (Exception e) {
            fail("Exception should not have been thrown");
        }
    }

    protected class MockFileItemStream implements FileItemStream {
        public InputStream openStream() throws IOException { return new ByteArrayInputStream("blah".getBytes()); }
        public String getContentType() { return null;   }
        public String getName() { return "mock.jpg";}
        public String getFieldName() { return null;   }
        public boolean isFormField() { return false;   }
        public FileItemHeaders getHeaders() { return null;   }
        public void setHeaders(FileItemHeaders fileItemHeaders) {  }
    }
}
