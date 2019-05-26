package util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class FileUtil {

    public static void writeFile(String path, String str) throws Exception {
        try
        {
            File file = new File(path);
            if(!file.exists()) {
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            StringBuffer sb = new StringBuffer();
            sb.append(str + "\r\n");
            out.write(sb.toString().getBytes("utf-8"));
            out.close();
        } catch(IOException ex) {
            log.error("写入文件报错。" + path, ex);
            throw new Exception(path);
        }
    }

    public static String readFile(String path) {
        StringBuffer sb=new StringBuffer();
        String tempstr=null;
        try {
            File file=new File(path);
            if(!file.exists()) {
                throw new FileNotFoundException();
            }
            FileInputStream fis=new FileInputStream(file);
            BufferedReader br=new BufferedReader(new InputStreamReader(fis, "utf-8"));
            while((tempstr=br.readLine())!=null) {
                sb.append(tempstr + "\r\n");
            }
        } catch(IOException ex) {
            System.out.println(ex.getStackTrace());
        }
        return sb.toString();
    }

}