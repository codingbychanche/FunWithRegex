package berthold.funwithregex;

/**
 * Checks filename for extension jpg etc...
 *
 * Created by Berthold on 8/18/17.
 */

public class isPictureFile {

    /**
     * ChecK
     *
     * @param   fileName    File name
     * @return  true        If the filename contains 'jpg','gif' etc
     */

    public static boolean check(String fileName){

        String [] np=fileName.split("\\.");

        if (np.length>1) {
        }
            if      (   np[np.length-1].equals("jpg") ||
                        np[np.length-1].equals("JPG") ||
                        np[np.length-1].equals("png") ||
                        np[np.length-1].equals("jpeg")||
                        np[np.length-1].equals("gif")
                    ) {
                System.out.println("------"+np[np.length-1]);
                return true;
            }

            else return false;
    }
}
