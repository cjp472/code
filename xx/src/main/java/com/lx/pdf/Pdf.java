package com.lx.pdf;

/**
 * Created by 游林夕 on 2019/9/10.
 */
public class Pdf {
    private static String itextPdf(byte[] bytes) {
        try {
            PdfReader pr = new PdfReader(bytes);
            PdfReaderContentParser prcp = new PdfReaderContentParser(pr);
            int pageNum = pr.getNumberOfPages();
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i < pageNum; i++) {
                
                SimpleTextExtractionStrategy strategy = prcp.processContent(i, new SimpleTextExtractionStrategy());
                sb.append(strategy.getResultantText());
                }
            return sb.toString();
            } catch (Exception e) {

            }
        return "";
        }

     

    private static String pdfBox(byte[] bytes) {
        try {
            PDDocument document = PDDocument.load(bytes);
            int pageNum = document.getNumberOfPages();
            PDFTextStripper ps = new PDFTextStripper();
            ps.setStartPage(1);
            ps.setEndPage(pageNum);
            String text = ps.getText(document);
            return text;
            } catch (Exception e) {

            }
        return "";
        }

}
