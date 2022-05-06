package com.leyou.auth.controller;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableConfigurationProperties(JwtProperties.class)
@RestController
public class AuthController {


    @Autowired
    private JwtProperties prop;

    @Autowired
    private AuthService authService;


    @PostMapping("/accredit")
    public ResponseEntity<Void> authentication(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request,
                                               HttpServletResponse response) {

        String token = this.authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CookieUtils.setCookie(request, response, prop.getCookieName(), token, prop.getCookieMaxAge(), null, true);
        return ResponseEntity.ok().build();


    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN") String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            UserInfo userinfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 解析成功要重新刷新token
            token = JwtUtils.generateToken(userinfo, this.prop.getPrivateKey(), this.prop.getExpire());
            // 更新cookie中的token
            CookieUtils.setCookie(request,response , this.prop.getCookieName(), token, this.prop.getCookieMaxAge());

            return ResponseEntity.ok(userinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
