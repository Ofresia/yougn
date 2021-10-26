package com.koreait.yougn.controller;

import com.koreait.yougn.beans.vo.MailSenderRunner;
import com.koreait.yougn.beans.vo.UserVO;
import com.koreait.yougn.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/user/*")
public class UserController {

    private final UserService userService;

    @GetMapping("join")
    public String join() {
        return "/user/join";
    }

    @GetMapping("login")
    public String login() {
        return "/user/login";
    }

    @RequestMapping(value = "logout", method = {RequestMethod.GET,RequestMethod.POST})
    public RedirectView logout(HttpServletRequest r){
        r.getSession().invalidate();;
        return new RedirectView("/");
    }

    @GetMapping("myPage")
    public String myPage(Model model, HttpServletRequest r) {
        String id = (String)r.getSession().getAttribute("sessionId");
        UserVO user = userService.getUser(id);
        model.addAttribute("user",user);
        return "/user/myPage";
    }

    @GetMapping("writeCollection")
    public String writeCollection() {
        return "/user/writeCollection";
    }

    @GetMapping("bye")
    public String bye() {
        return "/user/bye";
    }

    @GetMapping("userModify")
    public String userModify(Model model, HttpServletRequest r) {
        String id = (String)r.getSession().getAttribute("sessionId");
        UserVO user = userService.getUser(id);
        model.addAttribute("user",user);
        return "/user/userModify";
    }

    @GetMapping("changePw")
    public String changePw(@RequestParam("pin") String pin, UserVO userVO, Model model) {
        model.addAttribute("userVO",userVO);
        return "/user/changePw";
    }

    @GetMapping("inquiry")
    public String inquiry() {return "/user/inquiry";}

    @GetMapping("checkPw")
    public String checkPw() {
        return "/user/checkPw";
    }

    @PostMapping(value = "checkPw", consumes = "application/json",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HashMap<String,String> checkPw(String pw,HttpServletRequest r){
        String id = (String) r.getSession().getAttribute("sessionId");
        HashMap<String,String> map = new HashMap<>();

        if(userService.getUser(id).getPw().equals(pw)){
            map.put("id",id);
            map.put("result","변경 페이지로 이동합니다.");
            return map;
        }
        map.put("result","비밀번호가 일치하지 않습니다.");
        return map;
    }

    @GetMapping("findUser")
    public String findUser() {return "user/findUser";}

    //회원 가입
    @PostMapping("join")
    public String join(UserVO userVO){
        userService.join(userVO);
        return "/";
    }

//로그인
    @PostMapping("login")
    public String login(UserVO userVO, HttpServletRequest r, Model model){
        if(userService.login(userVO)){
            r.getSession().setAttribute("sessionId", userVO.getId());
            return "index";
        }
        model.addAttribute("result", false);
        return "user/login";
    }
//아이디 중복확인
    @PostMapping(value = "{id}", consumes = "application/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> checkId(@PathVariable("id") String id) throws UnsupportedEncodingException {
        return userService.checkId(id) ? new ResponseEntity<>(new String("사용 가능".getBytes(), "UTF-8"), HttpStatus.OK) :
        new ResponseEntity<>(new String("사용 불가".getBytes(),"UTF-8"),HttpStatus.OK);
    }


//회원정보 수정
    @PostMapping("userModify")
    public String userModify(UserVO user,Model model){
        if(userService.modifyUserInfo(user)){
            model.addAttribute("result","success");
            model.addAttribute("user",userService.getUser(user.getId()));
            return "user/userModify";
        }
        model.addAttribute("result","fail");
        model.addAttribute("user",user);
        return "user/userModify";
    }

//비밀번호 수정
    @PostMapping("changePw")
    public String changePw(String pw, String newPw,HttpServletRequest r,Model model){
        String id = (String)r.getSession().getAttribute("sessionId");
        UserVO user = userService.getUser(id);
        if(!user.getPw().equals(pw)){
            model.addAttribute("result","현재 비밀번호가 일치하지 않습니다.");
            return "user/changePw";
        }
        user.setPw(newPw);
        if(userService.modifyPw(user)){
            r.getSession().invalidate();
            return "/";
        }
        model.addAttribute("result","비밀번호 변경에 실패하였습니다.");
        return "user/changePw";
    }

//회원 탈퇴
    @PostMapping("bye")
    public String bye(HttpServletRequest r){
        String id = (String)r.getSession().getAttribute("sessionId");
        UserVO user = userService.getUser(id);
        userService.singOut(user);
        r.getSession().invalidate();
        return "/";
    }

//아이디 찾기(인증번호 보내기)
    @PostMapping(value = "findUser", consumes = "application/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HashMap<String,String> findUser(UserVO userVO) throws UnsupportedEncodingException{
        HashMap<String,String> map = new HashMap<>();
        List<String> idList = userService.findId(userVO);

        if (idList.size() == 0 || idList == null){
            map.put("result","일치하는 정보가 없습니다.");
            return  map;
        }

        String pin = makePin();
        String title = "유귀농 인증번호입니다.";
        String content = "유귀농 본인인증 인증번호 입니다. \n다른 사람에게 유출되지 않게 유의하시기 바랍니다.\n 인증번호 : " + pin;

        HashSet<String> emailList = userService.getEmailList(userVO);
        userService.sendEmail(emailList,title,content);

        map.put("result","인증번호 발송");
        map.put("idPin",pin);
        return map;
    }

    @GetMapping("showId")
    public void showId(UserVO userVO, @RequestParam("pin") String pin,Model model){
        model.addAttribute("idList",userService.findId(userVO));
    }

//    인증번호 만들기
    private String makePin(){
        String nums = "0123456789";
        String pin = "";
        Random random = new Random();
        for (int i=0; i <6; i++){
            pin += nums.charAt(random.nextInt(10));
        }
        return pin;
    }


//비밀번호 찾기
    //아이디 , 폰번호 입력
    //이메일로 인증번호 발송
    @PostMapping(value = "findPw", consumes = "application/json",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HashMap<String,String> findPw(UserVO userVO){
        HashMap<String,String> map = new HashMap<>();
        if(userService.findPw(userVO)){
            String pin = makePin();
            String title = "유귀농 인증번호입니다.";
            String content = "유귀농 본인인증 인증번호 입니다. \n다른 사람에게 유출되지 않게 유의하시기 바랍니다.\n 인증번호 : " + pin;
            userService.sendEmail(userService.getEmailList(userVO), title, content);
            map.put("result","인증번호 발송");
            map.put("pwPin",pin);
            return map;
        }
        map.put("result","일치하는 정보가 없습니다.");
        return  map;
    }

    //인증번호 입력
    //찾기 -> 비밀번호 변경
    @PatchMapping("changePw")
    public RedirectView changePw(UserVO userVO, @RequestParam("checkPw") String checkPw){
        userService.modifyPw(userVO);
        return new RedirectView("/");
    }

}
