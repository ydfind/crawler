package dal.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AppDO implements Serializable {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 1551320249544135007L;

    /**
     * 应用名称
     */
    private String appName;

    private String appType;

    /**
     * 应用包名
     */
    private String packageName;

    /**
     * 应用大类
     */
    private String industryType;

    /**
     * 应用细类
     */
    private String subIndustryType;

    /**
     * 可用标识 默认为TRUE
     */
    private String usableFlag;


    /**
     * 数据库主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 最后更新人
     */
    private String updatedBy;

}