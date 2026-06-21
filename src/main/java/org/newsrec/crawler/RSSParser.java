package org.newsrec.crawler;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RSSParser {

    private static final Logger logger = LogManager.getLogger(RSSParser.class);

    public List<RSSArticle> parse(
            String rssUrl,
            String source
    ) {

        List<RSSArticle> articles =
                new ArrayList<>();

        try {

            URL url =
                    new URL(rssUrl);

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            Document document =
                    builder.parse(
                            conn.getInputStream()
                    );

            NodeList items =
                    document.getElementsByTagName(
                            "item"
                    );

            for(int i=0;i<items.getLength();i++){

                Element item =
                        (Element) items.item(i);

                String title =
                        getValue(
                                item,
                                "title"
                        );

                String link =
                        getValue(
                                item,
                                "link"
                        );

                String description =
                        getValue(
                                item,
                                "description"
                        );

                articles.add(
                        new RSSArticle(
                                title,
                                link,
                                description,
                                source
                        )
                );
            }

        } catch (Exception e) {

            logger.error("Failed to parse RSS feed from {}", rssUrl, e);
        }

        return articles;
    }

    private String getValue(
            Element parent,
            String tag
    ){

        NodeList nodes =
                parent.getElementsByTagName(
                        tag
                );

        if(nodes.getLength()==0)
            return "";

        return nodes.item(0)
                .getTextContent();
    }
}