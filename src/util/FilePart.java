package util;

import java.io.File;
import java.io.FileOutputStream;

public class FilePart {
    String filename;
    byte[] bytes;

    public FilePart(String filename, byte[] bytes) {
        this.filename = filename;
        this.bytes = bytes;
    }

    public FilePart() {
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public void saveTo(String path) {
        try (FileOutputStream fos = new FileOutputStream(new File(path))) {
            fos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}