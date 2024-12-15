package im.eg.practice.util;

import cn.hutool.core.io.file.FileReader;
import im.eg.practice.pojo.Goods;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class HtmlParseUtil {

    public static List<Goods> parseJDGoods(String keyword) {
        List<Goods> goodsList = new LinkedList<>();
        try {
//        String url = "https://search.jd.com/Search?keyword=";
//        url += URLEncoder.encode(keyword, "utf-8");
//        Document document = Jsoup.parse(new URL(url), 1000 * 10);

            Document document = getDocument(keyword);
            Element element = document.getElementById("J_goodsList");
            if (element == null) {
                log.warn("failed to obtain document");
                return goodsList;
            }
            Elements liSet = element.getElementsByTag("li");
            for (Element ele : liSet) {

                String img = ele.getElementsByTag("img").eq(0).attr("src");
                String price = ele.getElementsByClass("p-price").eq(0).text();
                String title = ele.getElementsByClass("p-name").eq(0).text();
                String shop = ele.getElementsByClass("curr-shop hd-shopname").eq(0).text();

                goodsList.add(new Goods(title, price, img, shop));
            }
        } catch (Exception ex) {
            log.error("failed to parse page", ex);
        }

        return goodsList;
    }

    /**
     * simulation
     */
    private static Document getDocument(String keyword) throws IOException {
        FileReader fileReader;
        if (keyword.contains("java")) {
            fileReader = new FileReader("jd-java.html");
        } else {
            fileReader = new FileReader("jd-kindle.html");
        }
        return Jsoup.parse(fileReader.getInputStream(), "UTF-8", "https://search.jd.com/");
    }
}
