package org.cfi;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by pritam_mungse on 10/17/14.
 */
public class Json {


    private static final String fromDir = "/Users/pritam_mungse/Documents/workspace/code-for-india/extracted/json1" ;
    private static final String toDir = "/Users/pritam_mungse/Documents/workspace/code-for-india/extracted/json3" ;

    public static void main(String[] args) {
        try {
            System.out.println("Hello");

            File sourceDir = new File(fromDir);
            Collection<File> sourceFiles = FileUtils.listFiles(sourceDir, null, true);
            JSONParser parser = new JSONParser();

            for (File source : sourceFiles) {
                File toFile = new File(toDir + File.separator + source.getName() + ".2" ) ;
                try {
                    //System.out.println("Json file being worked on :" + source.getName() + "writing to:" + toFile.getAbsolutePath() );
                    long start = System.currentTimeMillis() ;
                    int lineCounter = 0 ;
                    List<String> lines = FileUtils.readLines(source, "UTF-8");
                    List<String> newLines = new ArrayList<String>() ;
                    for(String line : lines) {
                        try {
                            JSONObject data = (JSONObject) parser.parse(line);
                            String school_code = (String) data.get("school_code");
                            if (!"".equals(school_code)) {
                                Long code = getSchoolCode(school_code);
                                data.remove("school_code");
                                data.put("school_id", code);
                                //FileUtils.writeStringToFile(toFile,data.toJSONString() + "\n","UTF-8", true) ;
                                newLines.add(data.toJSONString());
                                //FileUtils.writeStringToFile(toFile,"","UTF-8", true) ;
                                lineCounter++;
                            }
                        }catch(Exception e) {
                            System.out.println("Error in parsing line:[" + line + "] , exception:" + e.getMessage() );
                            e.printStackTrace();
                        }
                    }
                    FileUtils.writeLines(toFile, newLines) ;
                    long end = System.currentTimeMillis() ;
                    System.out.println("Processed:" + source.getName() + " lines:" + lineCounter + " in:" + (end-start) + " ms");
                }catch(Exception e) {
                    System.out.println("Exception in processing file: [" + source + "], exception:" + e.getMessage() );
                    //e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.out.println("Top level exception:" + e.getMessage()) ;
            e.printStackTrace();
        }
    }

    private static Long getSchoolCode(String code) {
        String code2 = code ;
       int len = code2.length() ;
        for(int i = len; i>0; i--) {
            try {
                Long dCode = Long.valueOf(code2) ;
                return dCode ;
            }catch(Exception e) {
                System.out.println("Exception parsing code:[" + code2 + "]");
                code2 = code.substring(0, code2.length() - 1);
            }
        }
        throw new RuntimeException("Could not convert school code to integer:" + code) ;
    }


}
