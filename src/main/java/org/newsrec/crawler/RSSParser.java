package org.newsrec.crawler;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSParser {

    public List<RSSArticle> parse(
            String rssUrl,
            String source
    ) {

        List<RSSArticle> articles =
                new ArrayList<>();

        try {

            URL url =
                    new URL(rssUrl);

            DocumentBuilder builder =
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder();

            Document document =
                    builder.parse(
                            url.openStream()
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

            e.printStackTrace();
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