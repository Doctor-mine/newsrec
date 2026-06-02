package org.newsrec.recommendation;

import java.util.Map;

public class CosineSimilarity {

    public double calculate(

            Map<String, Double> a,

            Map<String, Double> b

    ) {

        double dot = 0;

        double magA = 0;

        double magB = 0;

        for(String term : a.keySet()){

            double x = a.get(term);

            double y = b.getOrDefault(term, 0.0);

            dot += x * y;

            magA += x * x;

            magB += y * y;
        }

        if(magA == 0 || magB == 0){

            return 0;
        }

        return dot
                /
                (
                        Math.sqrt(magA)
                                *
                                Math.sqrt(magB)
                );
    }
}