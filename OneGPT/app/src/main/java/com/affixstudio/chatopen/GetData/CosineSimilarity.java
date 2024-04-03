package com.affixstudio.chatopen.GetData;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CosineSimilarity {
    public String run(double[] searchTermEmbedding, ArrayList<PdfData> pdfData) {
        ArrayList<double[]> pageEmbeddings=new ArrayList<>();

        for (PdfData p:pdfData)
        {
            pageEmbeddings.add(p.pdfEmbedding);
        }


        // Example list of double arrays representing PDF page embeddings

        // Populate pageEmbeddings with actual embeddings

        // Example double array representing the search term or topic
       // double[] searchTermEmbedding = /* Your search term embedding */;

        // Calculate cosine similarity between the search term and each page embedding
        List<SimilarityResult> results = new ArrayList<>();
        for (int i = 0; i < pageEmbeddings.size(); i++) {
            double[] pageEmbedding = pageEmbeddings.get(i);
            double similarity = calculateCosineSimilarity(searchTermEmbedding, pageEmbedding);
            results.add(new SimilarityResult(i, similarity));
        }

        // Sort the results in descending order of similarity
        Collections.sort(results, Comparator.reverseOrder());


        StringBuilder topResult=new StringBuilder();
        int i=0;
        // Display the sorted results
        for (SimilarityResult result : results)
        {
            int pageIndex = result.getPageIndex();
            topResult.append("Page Number ").append(pageIndex).append("\n\n").append(pdfData.get(pageIndex).pdfText).append("\n\n");

            double similarity = result.getSimilarity();
            System.out.println("Page " + pageIndex + ": Cosine Similarity = " + similarity);
            i+=1;
            if (i>1)
            {
                break;
            }

        }
        return topResult.toString();
    }

    // Calculate cosine similarity between two vectors
    public double calculateCosineSimilarity(double[] vector1, double[] vector2)
    {
        RealVector v1 = new ArrayRealVector(vector1);
        RealVector v2 = new ArrayRealVector(vector2);

        double cosineSimilarity = (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());

        // Round the similarity score to a reasonable number of decimal places
        return Precision.round(cosineSimilarity, 4);
    }
}

class SimilarityResult implements Comparable<SimilarityResult> {
    private final int pageIndex;
    private final double similarity;

    public SimilarityResult(int pageIndex, double similarity) {
        this.pageIndex = pageIndex;
        this.similarity = similarity;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public double getSimilarity() {
        return similarity;
    }

    @Override
    public int compareTo(SimilarityResult other) {
        // Compare results based on similarity score
        return Double.compare(this.similarity, other.similarity);
    }
}
