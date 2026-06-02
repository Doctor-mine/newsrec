package org.newsrec.service;

import org.newsrec.dao.VectorDAO;

import java.util.Map;

public class VectorStorageService {

    private final VectorDAO dao =
            new VectorDAO();

    public void saveVector(

            int articleId,

            Map<String, Double> vector

    ) {

        for(
                Map.Entry<String, Double> entry
                :
                vector.entrySet()
        ){

            dao.saveVector(

                    articleId,

                    entry.getKey(),

                    entry.getValue()

            );
        }
    }
}