import dal.AppManager;
import dal.model.AppDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AppCrawler{

    private AppManager appManager = new AppManager() ;

    private MiAppCrawler miAppCrawler = new MiAppCrawler();

    private AnZhiAppCrawler anZhiAppCrawler = new AnZhiAppCrawler();

    /**
     * 测试数据库功能
     */
    @Test
    public void testAddAppDO(){
        List<AppDO> list = new ArrayList<AppDO>(8);
        AppDO appDO = new AppDO();
        appDO.setAppName("测试名称");
        appDO.setPackageName("com.android.test");
        appDO.setSubIndustryType("测试小类");
        appDO.setAppType("MI");
        appDO.setIndustryType("");
        appDO.setUsableFlag("TRUE");
        appDO.setCreatedBy("MI");
        list.add(appDO);
        //分类信息入库
        appManager.addAppClassification(list);
    }

    /**
     * 测试小米应用商城爬虫
     */
    @Test
    public void testCrawlelMI(){
        miAppCrawler.crawlerProcess();
    }

}
