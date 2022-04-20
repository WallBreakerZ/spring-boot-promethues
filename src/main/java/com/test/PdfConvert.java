package com.test;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class PdfConvert {
    private static final Log LOG = LogFactory.getLog(PdfConvert.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("threadName" + "-%d").build();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 10, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100), namedThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        new File("src/main/resources/image/").mkdir();
        for (int k = 0; k < 20; k++) {
            try (PDDocument document = PDDocument.load(new File("src/main/resources/test.pdf"));) {
                long convertStart = System.currentTimeMillis();
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int size = document.getNumberOfPages();
                boolean isInit = false;
                for (int i = 0; i < size; i++) {
                    int finalI = i;
                    String imageFileName = UUID.randomUUID().toString().replace("-", "") + "-" + finalI + ".jpg";
                    try (FileOutputStream fileOutputStream =
                                 new FileOutputStream("src/main/resources/image/" + imageFileName);
                         ByteArrayOutputStream out = new ByteArrayOutputStream();) {
                        BufferedImage image = pdfRenderer.renderImageWithDPI(finalI, 130, ImageType.RGB);
                        ImageIOUtil.writeImage(image, "jpg", out);
                        // 保存图片
                        out.writeTo(fileOutputStream);
                    } catch (IOException e) {
                        throw e;
                    }
                }
//                List<CompletableFuture<Void>> futureList = new ArrayList<>();
//                for (int i = 0; i < size; i++) {
//                    int finalI = i;
//                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                        String imageFileName = UUID.randomUUID().toString().replace("-", "") + "-" + finalI + ".jpg";
//                        try (FileOutputStream fileOutputStream =
//                                     new FileOutputStream("src/main/resources/image/" + imageFileName);
//                             ByteArrayOutputStream out = new ByteArrayOutputStream();) {
//                            BufferedImage image = pdfRenderer.renderImageWithDPI(finalI, 130, ImageType.RGB);
//                            ImageIOUtil.writeImage(image, "jpg", out);
//                            // 保存图片
//                            out.writeTo(fileOutputStream);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }, threadPoolExecutor).exceptionally(e -> {
//                        LOG.error("pdf转image失败，失败原因:" + e.getMessage(), e);
//                        throw new RuntimeException();
//                    });
//                    futureList.add(future);
//                }
//                for (CompletableFuture<Void> future : futureList) {
//                    future.get();
//                }
                LOG.info("pdfConvertImgs ,convert-cost time:" + (System.currentTimeMillis() - convertStart));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
