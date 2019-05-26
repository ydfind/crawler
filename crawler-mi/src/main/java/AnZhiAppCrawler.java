import com.alibaba.fastjson.JSON;
import common.ConstantStr;
import common.CrawlerFileCache;
import dal.AppManager;
import dal.model.AppDO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import util.HtmlUtil;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class AnZhiAppCrawler {

    /**
     * 应用分类首页
     */
    private static final String ANZHI_INDEX_URL = "http://www.anzhi.com";

    /**
     * 安卓应用分类url
     */
    private static final String ANZHI_ANDROID_APP_URL = "/widgetcatetag_1.html";

    /**
     * 安卓游戏分类url
     */
    private static final String ANZHI_ANDROID_GAME_URL = "/widgetcatetag_2.html";

    private static final String TEMPLATE_PAGE = "{$page}";



    /**
     * 大类名称
     */
    private static final String CLASS_NAME = "className";

    /**
     * 小类URL
     */
    private static final String SUB_CLASS_URL = "subClassUrl";

    /**
     * 小类名称
     */
    private static final String SUB_CLASS_NAME = "subClassName";

    public AppManager appManager = new AppManager() ;

    /**
     * 抓取并保存入库
     */
    public void crawlerProcess() {
        try{
            // 安卓应用分页
            log.info("{} 安卓应用 分页 应用获取开始", ConstantStr.ANZHI_NAME);
            String url = ANZHI_INDEX_URL + ANZHI_ANDROID_APP_URL;
            List<Map<String, String>> maps = parseAppClassification(url);
            log.info("{} fetchProcess分类 = {}", ConstantStr.ANZHI_NAME, JSON.toJSONString(maps));
//            //遍历获取应用分类下的app 信息
//            for (Map<String, String> map : maps){
//                log.info("{} fetchProcess当前爬取子类url:{}", ANZHI_NAME, map.get("subClassUrl"));
//                String subClassUrl = map.get("subClassUrl");
//                List<AppDO> list = doCrawlAppInfo(subClassUrl, map);
////                log.info("{} fetchProcess{}，size = {}， data= {}", ANZHI_NAME, subClassUrl, list.size(), JSON.toJSONString(list));
//
//                //分类信息入库
//                appManager.addAppClassification(list);
//                Thread.sleep(1000);
//            }

//            // 安卓游戏分页
//            log.info("{} 安卓游戏 分页 应用获取开始", ANZHI_NAME);
//            url = ANZHI_INDEX_URL + ANZHI_ANDROID_GAME_URL;
//            maps = parseAppClassification(url);
//            log.info("{} fetchProcess分类 = {}", ANZHI_NAME, JSON.toJSONString(maps));
//            //遍历获取分类下的app 信息
//            for (Map<String, String> map : maps){
//                log.info("{} fetchProcess当前爬取子类url:{}", ANZHI_NAME, map.get("subClassUrl"));
//                String subClassUrl = map.get("subClassUrl");
//                List<AppDO> list = doCrawlAppInfo(subClassUrl, map);
////                log.info("{} fetchProcess{}，size = {}， data= {}", ANZHI_NAME, subClassUrl, list.size(), JSON.toJSONString(list));
//
//                //分类信息入库
//                appManager.addAppClassification(list);
//                Thread.sleep(1000);
//            }
        }catch (Exception e){
            log.info("MiAppClassificationFetchProcessor.fetchProcess获取应用分类异常", e);
        }finally {
            //WebClientUtil.close(webClient);
        }
    }

    private List<AppDO> doCrawlAppInfo(String subClassUrl, Map<String, String> map) throws Exception {
        // 取得小类url,http://www.anzhi.com/tsort_7822_48_1_hot.html，分页即把“_1_”替换即可
        String baseUrl = subClassUrl.replace("_1_", "_" + TEMPLATE_PAGE + "_");
        String categoryName = map.get(SUB_CLASS_NAME);
        log.info("{} doCrawlAppInfo 小类NAME = {}", ConstantStr.ANZHI_NAME, categoryName);
        List<AppDO> appInfoList = new ArrayList<AppDO>();
        // 分页请求
        int pageNum = 1;
        int breakCount = 3;
        while(breakCount > 0) {
            // 计算新分页url
            String httpUrl = baseUrl.replace(TEMPLATE_PAGE, String.valueOf(pageNum));
            Document document = CrawlerFileCache.getHtmlTextByUrl(httpUrl, ConstantStr.ANZHI_NAME);
//            log.info("{} doCrawlAppInfo,page={}。data = {}", ANZHI_NAME, pageNum, document.toString());
            try {
                List<AppDO> list = parseAppInfo(document, map);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                appInfoList.addAll(list);
                pageNum++;
                breakCount = 3;
            } catch (Exception e) {
                log.warn("{} doCrawlAppInfo, can not parse this page = {}", ConstantStr.ANZHI_NAME, pageNum);
                // 防止死循环
                breakCount--;
            }
        }
        return appInfoList;
    }

    /**
     * 获取app信息
     */
    List<AppDO> parseAppInfo(Document document, Map<String, String> map){
        List<AppDO> list = new ArrayList<AppDO>();
        if(Objects.isNull(document)){
            return list;
        }
        // 取得 app_list下ul，下的li集合
        Element appList = document.getElementsByClass("app_list").get(0);
        Element ul = appList.getElementsByTag("ul").get(0);
        Elements lis = ul.getElementsByTag("li");
        if(CollectionUtils.isEmpty(lis)){
            return list;
        }
        // 大类名称
        String industryType = map.get(CLASS_NAME);
        // 子类名称
        String subIndustryType = map.get(SUB_CLASS_NAME);
        for(Element li: lis){
            // app_name下的a标签
            Element appName = li.getElementsByClass("app_name").get(0).getElementsByTag("a").get(0);
            AppDO appDO = new AppDO();
            appDO.setAppName(appName.text());
            // 处理package
            String href = appName.attr("href").trim();
            int separateIndex = href.indexOf("_");
            if(separateIndex >= 0){
                href = href.substring(separateIndex + 1);
            }
            String[] suffixs = {".html", ".htm"};
            String packageName = href;
            for(String suffix: suffixs){
                packageName = packageName.replace(suffix, "");
            }
            appDO.setPackageName(packageName);

            appDO.setSubIndustryType(subIndustryType);
            appDO.setAppType(ConstantStr.ANZHI_NAME);
            appDO.setIndustryType(industryType);
            appDO.setUsableFlag("TRUE");
            appDO.setCreatedBy(ConstantStr.ANZHI_NAME);
            list.add(appDO);

        }
        return list;
    }

    /**
     * 解析应用分类信息
     * @return
     * @throws IOException
     */
    private List<Map<String, String>> parseAppClassification(String url) throws Exception {
        Document document = CrawlerFileCache.getHtmlTextByUrl(url, ConstantStr.ANZHI_NAME);
        // document 为空不处理
        if (Objects.isNull(document)) {
            log.warn("{} parseAppClassification 应用分类首页解析异常", ConstantStr.ANZHI_NAME);
            return null;
        }
        // 按 class="itemlist"分大类
        List<Map<String, String>> classificationInfoList = new ArrayList<>();
        Elements classList = document.getElementsByClass("itemlist");
        for(Element classItem: classList){
            // 取得大类名称
            String[] industryTypePath = {"dt", "h2", "a"};
            Element industryTypeElement = HtmlUtil.getElementByTags(classItem, industryTypePath);
            String industryType = HtmlUtil.getEleText(industryTypeElement);
            // 取得每个小类: 都是dd标签
            Elements subClassList = classItem.getElementsByTag("dd");
            for(Element subClassItem: subClassList){
                try {
                    Element classEle = subClassItem.getElementsByTag("a").get(0);
                    String subClassUrl = classEle.attr("href").trim();
                    String subClassName = classEle.text().trim();
                    // 加入小类
                    Map<String, String> subClassInfo = new HashMap<>(8);
                    subClassInfo.put(CLASS_NAME, industryType);
                    subClassInfo.put(SUB_CLASS_URL, ANZHI_INDEX_URL + subClassUrl);
                    subClassInfo.put(SUB_CLASS_NAME, subClassName);
                    classificationInfoList.add(subClassInfo);
                }catch (Exception e){
                    log.info("{} 菜单小类无法识别 = {}", ConstantStr.ANZHI_NAME, subClassItem.toString());
                }
            }
        }
        return classificationInfoList;
    }

}
