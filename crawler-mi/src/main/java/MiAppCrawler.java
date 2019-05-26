import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

import static common.ConstantStr.MI_INDEX_URL;
import static common.ConstantStr.MI_NAME;

/**
 * 小米
 */
@Slf4j
@Component
public class MiAppCrawler {
    private static final String MI_CATEGOTY_API_URL =
            "http://app.mi.com/categotyAllListApi?page={$page}&categoryId={$categoryId}&pageSize=30";

    private static final String TEMPLATE_CATEGORY_ID = "{$categoryId}";

    private static final String TEMPLATE_PAGE = "{$page}";
    private static final String CLASS_NAME = "className";
    private static final String SUB_CLASS_URL = "subClassUrl";
    private static final String SUB_CLASS_NAME = "subClassName";

    private AppManager appManager = new AppManager() ;

    /**
     * 抓取并保存入库
     */
    public void crawlerProcess() {
        try{
            // 获取所有应用分类大类和子类
            List<Map<String, String>> maps = parseAppClassification();
//            log.info("{} MiAppClassificationFetchProcessor.fetchProcess分类 = {}", MI_NAME, JSON.toJSONString(maps));

            //遍历获取应用分类下的app 信息
            for(int i = maps.size() - 1; i >= 0; i--){
                Map<String, String> map = maps.get(i);
//                log.info("{} MiAppClassificationFetchProcessor.fetchProcess当前爬取子类url:{}", MI_NAME, map.get("subClassUrl"));
                String subClassUrl = map.get("subClassUrl");
                List<AppDO> list = doCrawlAppInfo(subClassUrl, map);
//                log.info("{} MiAppClassificationFetchProcessor.fetchProcess{} = {}", MI_NAME, subClassUrl, JSON.toJSONString(list));

                //分类信息入库
                appManager.addAppClassification(list);
                Thread.sleep(1000);
            }
        }catch (Exception e){
            log.info("MiAppClassificationFetchProcessor.fetchProcess获取应用分类异常", e);
        }finally {
            //WebClientUtil.close(webClient);
        }
    }

    private List<AppDO> doCrawlAppInfo(String subClassUrl, Map<String, String> map) throws Exception {
        // 取得小类ID
        String categoryId = subClassUrl.substring(subClassUrl.lastIndexOf("/") + 1);
        String categoryName = map.get(SUB_CLASS_NAME);
//        log.info("{} MiAppClassificationFetchProcessor.doCrawlAppInfo 小类id = {}", MI_NAME, categoryId);
        String baseUrl = MI_CATEGOTY_API_URL.replace(TEMPLATE_CATEGORY_ID, categoryId);
        List<AppDO> appInfoList = new ArrayList<AppDO>();
        // 分页请求
        int pageNum = 1;
        while(true){
            String httpUrl = baseUrl.replace(TEMPLATE_PAGE, String.valueOf(pageNum));
            String response = CrawlerFileCache.httpByUrl(httpUrl, MI_NAME);
//            log.info("{},MiAppClassificationFetchProcessor.doCrawlAppInfo,page={}。data = {}", categoryName, pageNum, response);
            List<AppDO> list = parseAppInfo(response, map);
            if(CollectionUtils.isEmpty(list)){
                break;
            }
            appInfoList.addAll(list);
            pageNum++;
        }
        return appInfoList;
    }

    /**
     * 获取app信息
     */
    List<AppDO> parseAppInfo(String response, Map<String, String> map){
        List<AppDO> list = new ArrayList<>();
        if(Objects.isNull(response)){
            return list;
        }
        JSONObject jsonObject = JSON.parseObject(response);
        JSONArray data = jsonObject.getJSONArray("data");
        if(Objects.isNull(data) || data.isEmpty()){
            return list;
        }
        // 大类名称
        String industryType = map.get(CLASS_NAME);
        Iterator<Object> iterator = data.iterator();
        while(iterator.hasNext()){
            JSONObject item = (JSONObject) iterator.next();
            // 解析每一项
            AppDO appDO = new AppDO();
            appDO.setAppName(item.getString("displayName"));
            appDO.setPackageName(item.getString("packageName"));
            appDO.setSubIndustryType(item.getString("level1CategoryName"));
            appDO.setAppType(MI_NAME);
            appDO.setIndustryType(industryType);
            appDO.setUsableFlag("TRUE");
            appDO.setCreatedBy(MI_NAME);
            list.add(appDO);
        }
        return list;
    }

    private List<Map<String, String>> parseAppClassification() throws Exception {
        Document document = CrawlerFileCache.getHtmlTextByUrl(ConstantStr.MI_INDEX_URL, MI_NAME);

        //document 为空不处理
        if (Objects.isNull(document)) {
//            log.warn("{} MiAppClassificationFetchProcessor.parseAppClassification 应用分类首页解析异常", MI_NAME);
            return null;
        }

        List<Map<String, String>> classificationInfoList = new ArrayList<>();

        Elements sidebarMods = document.getElementsByClass("sidebar-mod");
        for (Element sidebarMod: sidebarMods){
            Elements categoryList = sidebarMod.getElementsByClass("category-list");
            // 该链接下无详细分类，就继续
            if (CollectionUtils.isEmpty(categoryList)) {
//                log.info("{} MiAppClassificationFetchProcessor.parseAppClassification 该大类下无详细分类，继续", MI_NAME);
                continue;
            }
            // 大类名称
            Elements sidebarH = sidebarMod.getElementsByClass("sidebar-h");
            String industryType = HtmlUtil.getFirstText(sidebarH);
            // 除了游戏外，大类默认为空
            if(industryType.indexOf("游戏") < 0){
                industryType = "";
            }else{
                industryType = "游戏";
            }
            // 循环获取小类
            for(Element category: categoryList){
                Elements lis =  category.getElementsByTag("li");
                for (Element li: lis){
                    Element classEle = li.getElementsByTag("a").get(0);
                    String subClassUrl = classEle.attr("href").trim();
                    String subClassName = classEle.text().trim();
                    // 加入小类
                    Map<String, String> subClassInfo = new HashMap<>(8);
                    subClassInfo.put(CLASS_NAME, industryType);
                    subClassInfo.put(SUB_CLASS_URL, MI_INDEX_URL + subClassUrl);
                    subClassInfo.put(SUB_CLASS_NAME, subClassName);
                    classificationInfoList.add(subClassInfo);
                }

            }
        }
        return classificationInfoList;
    }

}
