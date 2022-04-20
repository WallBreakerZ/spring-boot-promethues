package com.test;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class PdfConvert {
    private static final Log LOG = LogFactory.getLog(PdfConvert.class);
    private static ThreadLocal<PDDocument> pdDocumentThreadLocal = new ThreadLocal<>();

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        long start = System.currentTimeMillis();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("threadName" + "-%d").build();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 10, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100), namedThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        new File("src/main/resources/image").mkdir();
        for (int k = 0; k < 100; k++) {

            try (FileInputStream fileInputStream = new FileInputStream("src/main/resources/test.pdf");) {
                int buffSize = fileInputStream.available();
                byte[] bytes = new byte[buffSize];
                fileInputStream.read(bytes);
                long convertStart = System.currentTimeMillis();
                ThreadLocal<PDDocument> pdDocumentThreadLocal = new ThreadLocal<PDDocument>() {
                    @SneakyThrows
                    protected PDDocument initialValue() {
                        return PDDocument.load(bytes);
                    }

                };
                PDDocument document = pdDocumentThreadLocal.get();
                pdDocumentThreadLocal.remove();
                int size = document.getNumberOfPages();
                boolean isInit = false;

                List<CompletableFuture<Void>> futureList = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    int finalI = i;
//                    func(finalI);
//                }
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> func(finalI, pdDocumentThreadLocal.get()), threadPoolExecutor)
                            .exceptionally(e -> {
                                LOG.error("pdf转image失败，失败原因:" + e.getMessage(), e);
                                throw new RuntimeException();
                            });
                    futureList.add(future);
                }
                for (CompletableFuture<Void> future : futureList) {
                    future.get();
                }
                LOG.info("pdfConvertImgs ,convert-cost time:" + (System.currentTimeMillis() - convertStart));
            } catch (Exception e) {
                throw e;
            }
        }
        System.out.println("总耗时：" + (System.currentTimeMillis() - start));
    }

    public static void func(int finalI, PDDocument document) {
//        try (PDDocument document = PDDocument.load(new File("src/main/resources/test.pdf"));) {
        System.out.println("线程名：" + Thread.currentThread().getName() + "；对象:" + document);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        String imageFileName = UUID.randomUUID().toString().replace("-", "") + "-" + finalI + ".jpg";
        try (FileOutputStream fileOutputStream =
                     new FileOutputStream("src/main/resources/image/" + imageFileName);
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(finalI, 130, ImageType.RGB);
            ImageIOUtil.writeImage(image, "jpg", out);
            // 保存图片
            out.writeTo(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdDocumentThreadLocal.remove();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
