package dal;

import dal.mapper.AppMapper;
import dal.model.AppDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AppManager {

    private static ApplicationContext ctx;

    static {
        ctx = new ClassPathXmlApplicationContext(
                "config/applicationContext.xml");
    }

    private AppMapper appMapper;
    public AppManager(){
        appMapper = (AppMapper)ctx.getBean("appMaper");
    }


    public void addAppClassification(List<AppDO> appDOList) {
        for (AppDO appDO : appDOList){
            try{
                appMapper.insert(appDO);
            }catch(DuplicateKeyException e){
                log.warn("应用分类信息已存在appDO:", appDO);
            }
        }

    }
}
