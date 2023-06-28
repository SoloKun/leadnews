package com.heima.model.wemedia.vos;

import com.heima.model.wemedia.pojos.WmNews;
import lombok.Data;

/**
 * ClassName: WmNewsVo
 * Package: com.heima.model.admin.vos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/20 23:00
 * @Version 1.0
 */
@Data
public class WmNewsVO extends WmNews{
        /**
         * 作者名称
         */
        private String authorName;
}
