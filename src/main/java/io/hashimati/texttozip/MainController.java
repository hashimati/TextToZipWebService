package io.hashimati.texttozip;

import io.hashimati.texttozip.entity.ZipRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class MainController

{

    @Autowired
    ApplicationContext context;

    @GetMapping("/zipIt")
    public Mono<ResponseEntity<Resource>> zipIt(ZipRequest zipRequest) throws Exception {

        File file = zipIt(zipRequest.getT());
        Resource resource = context.getResource("file:"+file.getAbsolutePath());
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" +"demo.zip")
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource))
                .doFinally(x->{
                    file.delete();
                });
    }

    @GetMapping("/zipMul")
    public Mono<ResponseEntity<Resource>> zipMult(ZipRequest zipRequest) throws Exception {

        File file = zipItToMultiple(zipRequest.getT());
        Resource resource = context.getResource("file:"+file.getAbsolutePath());
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" +"demo.zip")
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource))
                .doFinally(x->{
                    file.delete();
                });
    }

    private File zipIt(String t) throws Exception{

        File result = File.createTempFile("demo", ".zip");
        FileOutputStream fos = new FileOutputStream(result);
        ZipOutputStream zipOut = new ZipOutputStream(fos);


        File temp1 = File.createTempFile("text", "txt");
        FileOutputStream outT1 = new FileOutputStream(temp1);
        outT1.write(t.getBytes(),0, t.getBytes().length);
        outT1.close();

        ZipEntry zipEntry = new ZipEntry("toZip.txt");
        zipOut.putNextEntry(zipEntry);
        FileInputStream fis= new FileInputStream(temp1);

        byte[] bytes = new byte[1024];
        int length = 0;

        while((length = fis.read(bytes))> 0)
        {

            zipOut.write(bytes, 0, length);
        }
        fis.close();
        zipOut.close();

        temp1.delete();
        return result;
    }



    private File zipItToMultiple(String t) throws Exception{

        File result = File.createTempFile("demo", ".zip");
        FileOutputStream fos = new FileOutputStream(result);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        List<String> chopItToList = Arrays.asList(t.split(" "));

        for(String s: chopItToList) {
            File temp1 = File.createTempFile("myText", "txt");
            FileOutputStream outT1 = new FileOutputStream(temp1);
            outT1.write(s.getBytes(), 0, s.getBytes().length);
            outT1.close();

            ZipEntry zipEntry = new ZipEntry(s+"/toZip.txt");
            zipOut.putNextEntry(zipEntry);
            FileInputStream fis = new FileInputStream(temp1);

            byte[] bytes = new byte[1024];
            int length = 0;

            while ((length = fis.read(bytes)) > 0) {

                zipOut.write(bytes, 0, length);
            }
            temp1.delete();
            fis.close();
        }

        zipOut.close();


        return result;
    }
}
