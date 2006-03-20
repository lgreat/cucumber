package gs.web.jsp;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * This class is used to test JSP tag handlers and other classes that use
 * JspWriters.  The content writen to the writer is accessed using the
 * getOutputBuffer() method.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MockJspWriter extends JspWriter {

    private StringWriter _buffer;

    public MockJspWriter () {
        super(0, false);
        _buffer = new StringWriter(1024);
    }

    /**
     * The method used to access the information writen to this writer.
     * @return a <code>StringBuffer</code> object.
     */
    public StringBuffer getOutputBuffer() {
        _buffer.flush();
        return _buffer.getBuffer();
    }
    public void newLine() throws IOException {
        _buffer.write("\n");
    }

    public void print(boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void print(char c) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void print(int i) throws IOException {
        _buffer.write(i);
    }

    public void print(long l) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void print(float v) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void print(double v) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void print(char[] chars) throws IOException {
        _buffer.write(chars);
    }

    public void print(String string) throws IOException {
        _buffer.write(string);
    }

    public void print(Object object) throws IOException {
        _buffer.write(object.toString());
    }

    public void println() throws IOException {
        _buffer.write("\n");
    }

    public void println(boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void println(char c) throws IOException {
        _buffer.write(c);
        _buffer.write("\n");
    }

    public void println(int i) throws IOException {
        _buffer.write(i);
        _buffer.write("\n");
    }

    public void println(long l) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void println(float v) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void println(double v) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void println(char[] chars) throws IOException {
        _buffer.write(chars);
        _buffer.write("\n");
    }

    public void println(String string) throws IOException {
        _buffer.write(string);
        _buffer.write("\n");
    }

    public void println(Object object) throws IOException {
        _buffer.write(object.toString());
        _buffer.write("\n");
    }

    public void clear() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void clearBuffer() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void flush() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        _buffer.write(cbuf, off, len);
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int getRemaining() {
        throw new UnsupportedOperationException();
    }
}
