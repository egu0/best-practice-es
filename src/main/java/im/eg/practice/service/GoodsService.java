package im.eg.practice.service;

/*
业务接口
 */

import java.util.List;
import java.util.Map;

public interface GoodsService {

    /*
    根据商品关键字解析网页数据并保存数据.
     */
    public Boolean parseGoodsByKeyword(String keyword);

    /*
    分页搜索商品
     */
    public List<Map<String, Object>> searchGoods(String keyword, int pageNo, int pageSize);

    public List<Map<String, Object>> searchGoodsVersionHighLight(String keyword, int pageNo, int pageSize);

}
