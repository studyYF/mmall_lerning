package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by yangfan on 2017/12/11.
 */
public interface IFileService {
    /**
     * 上传文件
     * @param file springMVC文件
     * @param path 路径
     * @return 返回上传结果路径
     */
    public String upload(MultipartFile file, String path);
}
