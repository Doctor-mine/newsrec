package org.newsrec.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.net.URI;

public class BrowserUtil {

    private static final Logger logger = LogManager.getLogger(BrowserUtil.class);

    public static void open(
            String url
    ) {

        try {

            Desktop.getDesktop()
                    .browse(
                            new URI(url)
                    );

        } catch (Exception e) {

            logger.error("Failed to open URL: {}", url, e);
        }
    }
}