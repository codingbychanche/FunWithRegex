/**
 *  Data model for each row in our file list
 *
 *  @author  Berthold Fritz 2017
 */

package berthold.funwithregex;

import android.graphics.Bitmap;

public class FileListOneEntry {

    // Meta Data
    // This vars contain data to determine the nature of the row (e.g. Folder, Headline etc...)
    public int entryType;
    public static final int IS_ACTIVE=1;

    // Data
    public String fileName;
    public String filePath;
    public Bitmap fileSymbol;
    public boolean isReadable;
    public String lastModified;

    /**
     * Constructor, assign properties
     */

    FileListOneEntry(int entryType, Bitmap fileSymbol,String fileName,String filePath,boolean isReadable,String lastModified) {

        this.entryType=entryType;
        this.fileSymbol=fileSymbol;
        this.fileName=fileName;
        this.filePath=filePath;
        this.isReadable=isReadable;
        this.lastModified=lastModified;
    }

}
