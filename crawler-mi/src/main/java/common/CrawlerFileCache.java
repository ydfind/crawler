package common;

import lombok.extern.log4j.Log4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.FileUtil;
import util.RestTemplateRequest;

import java.io.File;
import java.io.IOException;


@Log4j
public class CrawlerFileCache {

    /**
     * 发送url请求，返回html的字符串
     * @param url
     * @return
     * @throws IOException
     */
    public static Document getHtmlTextByUrl(String url, String name) throws Exception {
        return Jsoup.parse(getHtmlStrByUrl(url, name));
    }

    /**
     * 发送url请求，返回html的字符串
     * @param url
     * @return
     * @throws IOException
     */
    public static String getHtmlStrByUrl(String url, String name) throws Exception {
        String filename = getCacheName(url, name);
        File file = new File(filename);
        if(file.exists()){
            return FileUtil.readFile(filename);
        }
        // 发送请求得到结果
        Connection connection = Jsoup.connect(url);
        connection.userAgent(ConstantStr.USER_AGENT);
        Document document = connection.get();
        // 缓存结果
        FileUtil.writeFile(filename, document.toString());

        return document.toString();
    }

    /**
     * 发送url请求，返回html的字符串
     * @param url
     * @return
     * @throws IOException
     */
    public static String httpByUrl(String url, String name) throws Exception {
        String filename = getCacheName(url, name);
        File file = new File(filename);
        if(file.exists()){
            return FileUtil.readFile(filename);
        }
        // 发送请求得到结果
        String response = RestTemplateRequest.requestGet(url);
        // 缓存结果
        FileUtil.writeFile(filename, response);

        return response;
    }

    private static String getCacheName(String url, String name){
        // 判断本地缓存是否存在
        int lastSeparate = url.lastIndexOf("?");
        String subUrl = url;
        if(lastSeparate >= 0){
            subUrl = url.substring(lastSeparate + 1);
        }
        lastSeparate = subUrl.lastIndexOf("/");
        String filename = subUrl;
        if(lastSeparate >= 0){
            filename = subUrl.substring(lastSeparate + 1);
            subUrl = subUrl.substring(0, lastSeparate);
            int lastSeparate1 = subUrl.lastIndexOf("/");
            if(lastSeparate1 >= 0){
                filename = subUrl.substring(lastSeparate1 + 1) + "_" + filename;
            }
        }
        filename = "D:\\crawler-data\\" + name + "\\" + filename;
//        log.info("文件名 = " + filename);
        return filename;
    }
}
