package berthold.funwithregex;

/**
 * Sort files
 *
 * Just does, what it's name promises
 *
 * Created by Berthold on 10/24/17.
 */

import java.io.File;

public class SortFiles {

    static int targetIndex;

    /**
     * Sort entry's by kind
     *
     * First folders, then files.
     */

    public static File[] byKind(File fileObjects[]){

        File fileObjectsSorted[]=new File[fileObjects.length];

        targetIndex=0;
        for (int i=0;i<=fileObjects.length-1;i++)
            if (fileObjects[i].isDirectory()) fileObjectsSorted[targetIndex++] = fileObjects[i];

        for (int i=0;i<=fileObjects.length-1;i++)
            if (fileObjects[i].isFile()) fileObjectsSorted[targetIndex++] = fileObjects[i];

        return fileObjectsSorted;
    }

    /**
     * Remove all files and dir's which are either not readable or
     * for which the user has no permission.
     *
     * I dont like this solution. This might be an good example why
     * the use of 'Lists' might be an better approach.
     */

    public static File[] removeNotReadables(File fileObjects []){

        File fo[]=new File[fileObjects.length];

        // Get all readable files and copy them to a tomporary array
        targetIndex=0;
        for (int i=0;i<=fileObjects.length-1;i++)
            // todo What if, if there are no readable files? => null pointer exception!!!!
            if ((new File(fileObjects[i].getPath()).canRead())) fo[targetIndex++] = fileObjects[i];

        // Now get all elemets from temp. array and copy them to a new array
        // which size now equals to the number of readable elements found....
        File [] fileObjectsSorted=new File[targetIndex];
        for(int i=0;i<=fileObjectsSorted.length-1;i++){
            fileObjectsSorted[i]=fo[i];
        }
        return fileObjectsSorted;
    }
}
