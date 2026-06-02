package org.newsrec.service;

import org.newsrec.dao.ArticleDAO;
import org.newsrec.model.Article;

public class ArticleStorageService {

    private final ArticleDAO dao =
            new ArticleDAO();

    public void save(
            Article article
    ) {

        dao.saveArticle(
                article.getTitle(),
                article.getContent(),
                article.getSource(),
                article.getUrl()
        );
    }
}