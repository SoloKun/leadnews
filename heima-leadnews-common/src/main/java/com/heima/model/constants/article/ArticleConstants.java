package com.heima.model.constants.article;

/**
 * ClassName: ArticleConstants
 * Package: com.heima.model.constants.article
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 16:54
 * @Version 1.0
 */
public class ArticleConstants {
    public static final Short LOADTYPE_LOAD_MORE = 0;  // 加载更多
    public static final Short LOADTYPE_LOAD_NEW = 1; // 加载最新
    public static final String DEFAULT_TAG = "__all__";



    // 文章行为分值
    public static final Integer HOT_ARTICLE_VIEW_WEIGHT = 1;
    public static final Integer HOT_ARTICLE_LIKE_WEIGHT = 3;
    public static final Integer HOT_ARTICLE_COMMENT_WEIGHT = 5;
    public static final Integer HOT_ARTICLE_COLLECTION_WEIGHT = 8;
    // 存到redis热文章前缀
    public static final String HOT_ARTICLE_FIRST_PAGE = "hot_articles_first_page";



}
