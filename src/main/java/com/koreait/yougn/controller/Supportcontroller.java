package com.koreait.yougn.controller;

import com.koreait.yougn.beans.vo.*;
import com.koreait.yougn.services.ClassService;
import com.koreait.yougn.services.HallService;
import com.koreait.yougn.services.InfoService;
import com.koreait.yougn.services.ReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/support/*")
@RequiredArgsConstructor
public class Supportcontroller {

    private final ReturnService returnService;
    private final HallService hallService;
    private final ClassService classService;
    private final InfoService infoService;

    @GetMapping("testPage1")
    public String testPage1(){return "/testPage1";}
    @GetMapping("testPage2")
    public String testPage2(){return "/testPage2";}
    @GetMapping("test")
    public String getJsp(){return "/test.jsp";}


    @GetMapping("hallList")
    public String hallList(HallCri hallCri, Model model){
        if(hallCri.getKeyword() != null){
            String temp =hallCri.getKeyword();
            hallCri.setKeyword(temp.replace(" ","").length() == 0? null :hallCri.getKeyword());
        }
        if(hallCri.getSido() != null){
            hallCri.setSido(hallCri.getSido().equals("시/도 선택")?null:hallCri.getSido());
        }
        if(hallCri.getSido() != null){
            hallCri.setSido(hallCri.getSido().equals("")?null:hallCri.getSido());
        }
        if(hallCri.getGugun() != null) {
            hallCri.setGugun(hallCri.getGugun().equals("전체")?null:hallCri.getGugun());
        }
        if(hallCri.getGugun() != null){
            hallCri.setGugun(hallCri.getGugun().equals("")?null:hallCri.getGugun());
        }
        model.addAttribute("list",hallService.getList(hallCri));
        model.addAttribute("pageMaker",new PageDTO(hallService.getTotal(hallCri),10,hallCri));
        return "support/hallList";
    }

    @GetMapping(value = "returnList")
    public String returnList(ReturnCri returnCri, Model model){
        if(returnCri.getKeyword() != null){
            String temp =returnCri.getKeyword();
            returnCri.setKeyword(temp.replace(" ","").length() == 0? null :returnCri.getKeyword());
        }
        if(returnCri.getLocal() != null){
            returnCri.setLocal(returnCri.getLocal().equals("시/도 선택")?null:returnCri.getLocal());
        }
        if(returnCri.getItem() != null){
            returnCri.setItem(returnCri.getItem().equals("전체")?null:returnCri.getItem());
        }
        model.addAttribute("list",returnService.searchList(returnCri));
        model.addAttribute("pageMaker",new PageDTO(returnService.getTotal(returnCri),10,returnCri));
        return "support/returnList";
    }

    @RequestMapping(value = "classList",method = {RequestMethod.GET,RequestMethod.POST})
    public String classList(ClassCri classCri, HttpServletRequest r, Model model){
        String id = (String)r.getSession().getAttribute("sessionId");
        if(classCri.getKeyword() != null){
            String temp = classCri.getKeyword();
            classCri.setKeyword(temp.replace(" ","").length() == 0? null : classCri.getKeyword());
        }
        List<ClassVO> list = classService.getList(classCri);
        ArrayList<Boolean> checkList = new ArrayList<>();
        String today = getToday();
        for (ClassVO vo : list) {
            checkList.add(vo.getRecruitCloseDate().compareTo(today) != -1);
        }
        model.addAttribute("id",id==null?"":id);
        model.addAttribute("list", list);
        model.addAttribute("srcList",classService.getSrcList(list));
        model.addAttribute("checkList",checkList);
        model.addAttribute("pageMaker",new PageDTO(classService.getTotal(classCri),10,classCri));
        return "support/classList";
    }

    private String getToday(){
        Calendar c =Calendar.getInstance();
        String month = (c.get(Calendar.MONTH)+1) < 10? "0"+(c.get(Calendar.MONTH)+1) : (c.get(Calendar.MONTH)+1) + "";
        String day = c.get(Calendar.DAY_OF_MONTH) < 10? "0"+c.get(Calendar.DAY_OF_MONTH) : c.get(Calendar.DAY_OF_MONTH)+"";
        String today = "" + c.get(Calendar.YEAR) + "-" + month + "-" + day;
        return today;
    }

    @GetMapping("classView")
    public void classView(Long num, HttpServletRequest r,Model model){
        String id = (String)r.getSession().getAttribute("sessionId");
        log.info("id : " + id);
        ClassVO classVO = classService.getClass(num);

        ApplyVO applyVO = new ApplyVO();
        applyVO.setId(id);
        applyVO.setClassNum(num);

        model.addAttribute("merchant_uid",classService.getMerchant_uid(applyVO));
        model.addAttribute("imp_uid",classService.getImp_uid(applyVO));
        model.addAttribute("applyCheck",classService.checkApply(applyVO));
        model.addAttribute("check" , classVO.getRecruitDate().compareTo(getToday()) <= 0);
        model.addAttribute("class",classVO);
        model.addAttribute("src",classService.getSrc(classVO.getNum()));
    }

    // 결제 완료 후 merchant_uid를 받아서 요청을 보냄
    // 클래스 신청
    @Transactional(rollbackFor = Exception.class)
    @PostMapping(value = "apply")
    public RedirectView apply( ApplyVO applyVO, HttpServletRequest r,RedirectAttributes rtts){
        String id = (String)r.getSession().getAttribute("sessionId");
        applyVO.setId(id);
        classService.apply(applyVO);
        rtts.addAttribute("num",applyVO.getClassNum());
        return new RedirectView("classView");
    }

    // 클래스 취소
    @Transactional(rollbackFor = Exception.class)
    @PostMapping(value = "cancel", consumes = "application/json; charset=utf-8", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HashMap<String,String> cancel(@RequestBody ApplyVO applyVO, HttpServletRequest r){
        HashMap<String,String> map = new HashMap<>();
        String id = (String)r.getSession().getAttribute("sessionId");
        applyVO.setId(id);
        if(classService.cancel(applyVO)){
            map.put("result","성공");
        }else{
            map.put("result","실패");
        }
        return map;
    }

    @GetMapping("classRegister")
    public void classRegister(){}

    @PostMapping("classRegister")
    public RedirectView classRegister(ClassVO classVO, @RequestParam("addressDetail") String addressDetail, Model model){
        addressDetail = addressDetail == null? "" : addressDetail;
        classVO.setAddress(classVO.getAddress() + " " + addressDetail);
        classService.register(classVO);
        model.addAttribute("pageNum", 1);
        model.addAttribute("amount",10);
        return new RedirectView("classList");
    }

    @GetMapping("classModify")
    public void classModify(Long num, Model model){
        ClassVO classVO = classService.getClass(num);
        model.addAttribute("class",classVO);
        model.addAttribute("thumbList",classService.getThumbList(classVO.getNum()));
    }

    @PostMapping("classModify")
    public RedirectView classModify(ClassVO classVO, RedirectAttributes rttr){
        if(classService.modify(classVO)){
            rttr.addAttribute("num",classVO.getNum());
        }
        return new RedirectView("classView");
    }

    @GetMapping(value = "getAttachList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ArrayList<ThumbVO> getAttachList(Long classNum){
        return classService.getThumbList(classNum);
    }

    @GetMapping("infoList")
    public String infoList(Criteria criteria,HttpServletRequest r, Model model){
        String id = (String)r.getSession().getAttribute("sessionId");
        List<InfoVO> list = infoService.getInfoList(criteria);

        model.addAttribute("id",id==null?"":id);
        model.addAttribute("list",list);
        model.addAttribute("pageMaker",new PageDTO(infoService.getTotal(),10,criteria));
        return "support/infoList";
    }

}
