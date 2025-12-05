package aliyunfc.FunctionsCreator;

import java.io.File;
import java.io.IOException;

public class Creator {
    private static String[] functionNames;
    public static void createFunctions(){
        File[] allFunctionsDirectory = getAllFunctionsCode();
        String[] packageNames = new String[allFunctionsDirectory.length];
        for (int i = 0; i < allFunctionsDirectory.length; i++) {
            packageNames[i] = allFunctionsDirectory[i].getName();
            Creator.functionNames[i] = allFunctionsDirectory[i].getName();
        }

    }
    private static File[] getAllFunctionsCode() {
        File directory = null;
        try {
            directory = new File(new File("").getCanonicalFile().getPath() + "/src/main/java/aliyunfc/Functions");
        } catch (IOException e) {
            e.printStackTrace();
        }
        File[] allFunctionsDirectory = directory.listFiles();
        return allFunctionsDirectory;
    }

}