package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
//@RequestMapping("/user")
public class Usercontroller {
    @Autowired
    private UserService userService;

    /**
     * 校验数据
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type) {

        return ResponseEntity.ok(userService.checkData(data, type));
    }

    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/register")
    public  ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){
        Boolean boo = this.userService.register(user, code);
        if (boo == null || !boo) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

   @GetMapping("/query")
   public ResponseEntity<User> queryUser(@RequestParam("username")String username, @RequestParam("password") String password){
       User user = this.userService.queryUser(username, password);
       if (user == null) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
       }
       return ResponseEntity.ok(user);
   }

}
