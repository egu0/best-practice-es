package im.eg.practice.service.impl;

import com.alibaba.fastjson.JSON;
import im.eg.practice.pojo.Goods;
import im.eg.practice.service.GoodsService;
import im.eg.practice.util.ESConstant;
import im.eg.practice.util.HtmlParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean parseGoodsByKeyword(String keyword) {

        try {
            // 解析页面, 获取商品数据
            List<Goods> goodsList = HtmlParseUtil.parseJDGoods(keyword);

            // 批量添加文档
            BulkRequest bulkRequest = new BulkRequest();

            for (int i = 0; i < goodsList.size(); i++) {
                // 文档使用随机ID
                bulkRequest.add(
                        new IndexRequest(ESConstant.JD_ES_GOODS_LIST)
                                .source(JSON.toJSONString(goodsList.get(i)), XContentType.JSON));
            }

            if (bulkRequest.numberOfActions() < 1) {
                log.warn("restHighLevelClient 发送请求终止：bulkRequest 对象包含 0 个请求");
                return false;
            }

            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return !bulkResponse.hasFailures();
        } catch (Exception ex) {
            log.error("parseGoodsByKeyword 方法执行时出现错误", ex);
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> searchGoods(String keyword, int pageNo, int pageSize) {

        // 存放搜索结果
        List<Map<String, Object>> goodsList = new LinkedList<>();

        if (!StringUtils.hasLength(keyword)) return goodsList;
        if (pageNo <= 0) pageNo = 1;
        if (pageSize < 0) pageSize = 0;

        try {

            SearchRequest searchRequest = new SearchRequest(ESConstant.JD_ES_GOODS_LIST);

            // 构建搜索条件
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 搜索超时
            searchSourceBuilder.timeout(TimeValue.timeValueSeconds(10));

//            // 精确匹配 --> title字段拆分时有时无
//            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
//            searchSourceBuilder.query(termQueryBuilder);

            // match 匹配
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
            searchSourceBuilder.query(matchQueryBuilder);
            // 分页数据
            searchSourceBuilder.from(pageNo);
            searchSourceBuilder.size(pageSize);
            // 高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<b style='color:red;'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);

            // 使用搜索条件
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> map = hit.getSourceAsMap();
                goodsList.add(map);
            }
        } catch (Exception ex) {
            // TODO
        }

        return goodsList;
    }

    /*
    搜索商品, 关键字高亮版
     */
    @Override
    public List<Map<String, Object>> searchGoodsVersionHighLight(String keyword, int pageNo, int pageSize) {

        // 存放搜索结果
        List<Map<String, Object>> goodsList = new LinkedList<>();

        if (!StringUtils.hasLength(keyword)) return goodsList;
        if (pageNo <= 0) pageNo = 1;
        if (pageSize < 0) pageSize = 0;

        try {

            SearchRequest searchRequest = new SearchRequest(ESConstant.JD_ES_GOODS_LIST);

            // 构建搜索条件
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 搜索超时
            searchSourceBuilder.timeout(TimeValue.timeValueSeconds(10));

//            // 精确匹配 --> title字段拆分时有时无
//            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
//            searchSourceBuilder.query(termQueryBuilder);

            // match 匹配
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
            searchSourceBuilder.query(matchQueryBuilder);
            // 分页数据
            searchSourceBuilder.from(pageNo);
            searchSourceBuilder.size(pageSize);
            // 高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<b style='color:red;'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);

            // 使用搜索条件
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> map = hit.getSourceAsMap();

                // 从 高亮map 中尝试获取 title 字段. 如果有则覆盖原 map 中的该字段
                HighlightField highlightField = hit.getHighlightFields().getOrDefault("title", null);
                if (highlightField != null) {
                    Text[] fragments = highlightField.fragments();
                    StringBuilder s = new StringBuilder();
                    for (Text text : fragments) {
                        s.append(text);
                    }
                    map.put("title", s.toString());
                }

                goodsList.add(map);
            }
        } catch (Exception ex) {
            // TODO
        }

        return goodsList;
    }
}
