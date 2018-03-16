package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by yangfan on 2017/12/11.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path) {
        //获取文件的原始名字
        String fileName = file.getOriginalFilename();
        // 获取文件的后缀 .jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 给文件重新命名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传文件的文件名:{},上传的路径:{},新文件名:{}", fileName, path, uploadFileName);
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();// 如果该文件不存在，则修改权限，并创建该文件
        }
        // 创建一个fileDir下的子文件，文件名称就是重新命名的uploadfileName
        File targetFile = new File(path, uploadFileName);
        try {
            // springMVC 将文件上传
            file.transferTo(targetFile);
            // todo 将targetFile上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // todo 上传完之后，删除upload文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
        }
        return targetFile.getName();// 返回由此抽象路径表示的文件或目录的名称
    }
}
