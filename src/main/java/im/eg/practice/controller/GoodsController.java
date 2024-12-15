package im.eg.practice.controller;

import im.eg.practice.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /*
    解析商品
     */
    @ResponseBody
    @GetMapping("/parse/{keyword}")
    public Boolean parseGoods(@PathVariable("keyword") String keyword) {
        if (!StringUtils.hasLength(keyword)) {
            return false;
        }
        return goodsService.parseGoodsByKeyword(keyword);
    }

    /*
    分页搜索商品
     */
    @ResponseBody
    @GetMapping("/search/{keyword}/{pn}/{size}")
    public Object searchGoods(
            @PathVariable("keyword") String keyword,
            @PathVariable("pn") int pn,
            @PathVariable("size") int size) {

//        无高亮
//        return goodsService.searchGoods(keyword, pn, size);

        // 高亮版
        return goodsService.searchGoodsVersionHighLight(keyword, pn, size);
    }


}
