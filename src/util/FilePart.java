package util;

public class FilePart {
    String filename;
    int[] byte_read;

    public FilePart(String filename, int[] byte_read) {
        this.filename = filename;
        this.byte_read = byte_read;
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
     * @return the byte_read
     */
    public int[] getByte_read() {
        return byte_read;
    }

    /**
     * @param byte_read the byte_read to set
     */
    public void setByte_read(int[] byte_read) {
        this.byte_read = byte_read;
    }

}