package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by yangfan on 2017/12/21.
 */
public class FTPUtil {
    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");


    // 构造方法
    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * 上传文件到ftp服务器
     * @param fileList 文件数组
     * @return 返回是否成功
     * @throws IOException 异常
     */
    public static  boolean uploadFile(List<File> fileList) throws IOException {
        // 根据ftp服务器ip, 用户，密码创建ftputil实例
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21,ftpUser,ftpPass);
        logger.info("开始链接FTP服务器");
        // img 表示把文件传到ftp服务器的img文件夹下
        boolean result = ftpUtil.uploadFile("img",fileList);
        logger.info("结束上传，上传结果:{}", result);
        return  result;
    }

    /**
     * 上传文件到ftp服务器
     * @param remotePath 远程地址
     * @param fileList 文件数组
     * @return 是否成功
     * @throws IOException 异常
     */
    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        //链接FTP服务器
        if (connectServer(this.getIp(),this.getPort(),this.getUser(),this.getPwd())){
            try {
                // 改变工作目录
                ftpClient.changeWorkingDirectory(remotePath);
                // 设置缓冲区
                ftpClient.setBufferSize(1024);
                // 设置编码类型 统一
                ftpClient.setControlEncoding("UTF-8");
                // 设置文件类型为二进制文件类型，防止乱码
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // todo 打开本地被动模式（前几章未学习)
                ftpClient.enterLocalPassiveMode();
                for (File fileItem: fileList) {
                    fis = new FileInputStream(fileItem);
                    // 通过ftpclient 存储文件
                    ftpClient.storeFile(fileItem.getName(),fis);
                }
            } catch (IOException e) {
                logger.error("上传文件异常",e);
                e.printStackTrace();
                uploaded = false;
            } finally {
                // 释放资源
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    /**
     * 链接ftp服务器
     * @param ip ip
     * @param port 端口
     * @param user 用户
     * @param pwd 密码
     * @return 是否成功
     */
    private boolean connectServer(String ip,int port,String user,String pwd) {
        // 创建ftp客户端实例
        ftpClient = new FTPClient();
        boolean isSuccess = false;
        try {
            // 链接
            ftpClient.connect(ip);
            // 登录
            isSuccess = ftpClient.login(user,pwd);
        } catch (IOException e) {
            logger.error("联接FTP服务器错误",e);
        }
        return isSuccess;

    }

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
