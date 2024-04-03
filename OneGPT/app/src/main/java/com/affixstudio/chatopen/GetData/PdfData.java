package com.affixstudio.chatopen.GetData;

public class PdfData {


    String pdfName;
    int PageNo;
    String pdfText;
    double[] pdfEmbedding;

    public String getPdfName() {
        return pdfName;
    }

    public int getPageNo() {
        return PageNo;
    }

    public String getPdfText() {
        return pdfText;
    }

    public double[] getPdfEmbedding() {
        return pdfEmbedding;
    }

    public PdfData(String pdfName, int pageNo, String pdfText, double[] pdfEmbedding) {
        this.pdfName = pdfName;
        PageNo = pageNo;
        this.pdfText = pdfText;
        this.pdfEmbedding = pdfEmbedding;
    }
}
