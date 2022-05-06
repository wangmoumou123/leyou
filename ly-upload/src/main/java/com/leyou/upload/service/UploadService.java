package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.FastClientImporter;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties({UploadProperties.class})
public class UploadService {
    //注入storage
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private UploadProperties uploadProperties;
//    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg", "image/bmp", "image/png");


    public String uploadFile(MultipartFile file) {
        // 检验文件类型
        String contentType = file.getContentType();
        if (!uploadProperties.getAllowTypes().contains(contentType)) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //检验文件内容
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 准备目标路径
//        File desk = new File("C:\\Users\\My\\Desktop\\imagetest", file.getOriginalFilename());

            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

//            //保存文件到本地
//            file.transferTo(desk);
            // 返回路径
//            return "http://image.leyou.com/" + file.getOriginalFilename();
            return uploadProperties.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            log.error("[文件上传] 上传文件失败", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

        // 返回路径

    }
}
