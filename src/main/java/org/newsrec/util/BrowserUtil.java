package org.newsrec.util;

import java.awt.Desktop;
import java.net.URI;

public class BrowserUtil {

    public static void open(
            String url
    ) {

        try {

            Desktop.getDesktop()
                    .browse(
                            new URI(url)
                    );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}