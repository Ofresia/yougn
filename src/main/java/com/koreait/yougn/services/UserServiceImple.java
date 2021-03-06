package com.koreait.yougn.services;

import com.koreait.yougn.beans.dao.UserDAO;
import com.koreait.yougn.beans.vo.MailSenderRunner;
import com.koreait.yougn.beans.vo.UserVO;
import lombok.RequiredArgsConstructor;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImple implements UserService {

    private final UserDAO userDAO;
    private final MailSenderRunner msr;

    @Override
    public boolean join(UserVO userVO) {
        return userDAO.join(userVO);
    }

    @Override
    public boolean login(UserVO userVO) {
        UserVO vo = userDAO.getUser(userVO.getId());
        boolean check = false;
        if(vo != null) {
            if (vo.getPw().equals(userVO.getPw())) {
                check = true;
            }
        }
        return check;
    }

    @Override
    public boolean checkId(String id) {
        return userDAO.getUser(id) == null;
    }

    @Override
    public UserVO getUser(String id) {
        return userDAO.getUser(id);
    }

    @Override
    public boolean modifyUserInfo(UserVO userVO) {
        return userDAO.modifyInfo(userVO);
    }

    @Override
    public boolean modifyPw(UserVO userVO) {
        return userDAO.modifyPw(userVO);
    }

    @Override
    public boolean singOut(UserVO userVO) {
        userVO.setStatus(1);
        return userDAO.signOut(userVO);
    }

    @Override
    public List<String> findId(UserVO userVO) {
        return userDAO.findId(userVO);
    }

    @Override
    public HashSet<String> getEmailList(UserVO userVO) {
        HashSet<String> emailList = new HashSet<>();
        if(userVO.getId() == null){
            List<String> idList = userDAO.findId(userVO);
            for (int i = 0; i < idList.size(); i++) {
                emailList.add(userDAO.getUser(idList.get(i)).getEmail());
            }
        }else{
            emailList.add(userDAO.getUser(userVO.getId()).getEmail());
        }
        return emailList;
    }

    @Override
    public boolean sendEmail(HashSet<String> emailList, String title, String content) {
        for (String email:emailList) {
            try {
                msr.send(email,title,content);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean findPw(UserVO userVO) {
        return userDAO.findPw(userVO);
    }

    @Override
    public void certified(String phoneNumber, String cerNum) {

        String api_key = "NCS8OXFJTOAAXJJ3";
        String api_secret = "W0IFTCK2IVRCNGFPU2UCPVB6UAXVKMOU";
        Message coolsms = new Message(api_key, api_secret);

        // 4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phoneNumber);
        params.put("from", "01043772616");
        params.put("type", "SMS");
        params.put("text", "????????? ??????????????? ????????? : ???????????????" + "["+cerNum+"]" + "?????????.");
        params.put("app_version", "test app 1.2");

        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
        } catch (CoolsmsException e) {
            e.getMessage();
            e.getCode();
        }

    }
}