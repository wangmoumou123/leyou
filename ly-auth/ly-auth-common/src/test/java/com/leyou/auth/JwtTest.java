package com.leyou.auth;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "E:\\javastudy\\spring_cloud_stu\\leyou\\ly-auth\\ly-auth-common\\tmp\\rsa\\rsa.pub";

    private static final String priKeyPath = "E:\\javastudy\\spring_cloud_stu\\leyou\\ly-auth\\ly-auth-common\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTY0NzMyMTI0Mn0.MriBnJsNar7t9fw5PILrUkc7avsW4KkpDqn0PAYiWg0N_uXtvhnIms61XzPaUzY-SQHHSvWZcXJmxmbFNROKcyarmWbg11TXxbM3qRoEmETUoLmLUOp6nNQnAEy5eREBuxdkNF6G6QZDcnSW3c3tVr1uOWF84ckIM3tWvP-bfbA";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}