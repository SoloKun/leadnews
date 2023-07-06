package com.heima.model.constants.search;

/**
 * ClassName: SearchConstants
 * Package: com.heima.model.constants.search
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/5 15:48
 * @Version 1.0
 */
public class SearchConstants {
    public static final String ARTICLE_INDEX_NAME = "app_info_article";
    public static final String ARTICLE_INDEX_MAPPING = "{\n" +
            "    \"mappings\":{\n" +
            "        \"properties\":{\n" +
            "            \"id\":{\n" +
            "                \"type\":\"long\"\n" +
            "            },\n" +
            "            \"publishTime\":{\n" +
            "                \"type\":\"date\"\n" +
            "            },\n" +
            "            \"layout\":{\n" +
            "                \"type\":\"integer\"\n" +
            "            },\n" +
            "            \"images\":{\n" +
            "                \"type\":\"keyword\",\n" +
            "                \"index\": false\n" +
            "            },\n" +
            "           \"staticUrl\":{\n" +
            "                \"type\":\"keyword\",\n" +
            "                \"index\": false\n" +
            "            },\n" +
            "            \"authorId\": {\n" +
            "          \t\t\"type\": \"long\"\n" +
            "       \t\t},\n" +
            "          \"title\":{\n" +
            "            \"type\":\"text\",\n" +
            "            \"analyzer\":\"ik_smart\"\n" +
            "          }\n" +
            "        }\n" +
            "    }\n" +
            "}";

}
