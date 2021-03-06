package com.koreait.yougn.controller;

import com.koreait.yougn.beans.vo.*;
import com.koreait.yougn.services.MarketService;
import com.koreait.yougn.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/market/*")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;
    private final UserService userService;

    /*리스트*/
    @RequestMapping(value = "marketList", method = {RequestMethod.GET,RequestMethod.POST})
    public String marketList(Criteria criteria, Model model,  HttpServletRequest r) {
        String id = (String) r.getSession().getAttribute("sessionId");
        model.addAttribute("id",id==null?"":id);
        model.addAttribute("list", marketService.getList(criteria));
        model.addAttribute("pageMaker", new PageDTO(marketService.getTotal(criteria), 10, criteria));
        log.info(id);
        return "/market/marketList";
    }

    /*글 수정*/
    //페이지 이동
    @GetMapping("marketModify")
    public void marketModify(@RequestParam("itemnum") Long itemnum, Criteria criteria, Model model) {
        model.addAttribute("vo",marketService.get(itemnum));
        model.addAttribute("criteria",criteria);
    }

    //메소드
    @PostMapping("marketModify")
    public RedirectView marketModify(Criteria criteria, ItemVO itemVO, RedirectAttributes rttr) {
        log.info("------------------------------------");
        log.info("------------------marketModify--------------------------------------------------");
        log.info("------------------------------------");

        if (marketService.modify(itemVO)) {
            rttr.addAttribute("result", "success");
            rttr.addAttribute("itemnum", itemVO.getItemnum());
        }
        return new RedirectView("marketView" + criteria.getListLink());
    }

    /*글 작성*/
    //페이지 이동
    @GetMapping("marketWrite")
    public void marketWrite(ItemVO itemVO, Criteria criteria, Model model, HttpServletRequest r) {
        String id = (String) r.getSession().getAttribute("sessionId");
        itemVO.setUserid(id);
        model.addAttribute("vo",itemVO);
        model.addAttribute("criteria",criteria);
    }

    //메소드
    @PostMapping("marketWrite")
    public RedirectView marketWrite(ItemVO itemVO, RedirectAttributes rttr, HttpServletRequest r) {
        String id = (String) r.getSession().getAttribute("sessionId");
        itemVO.setUserid(id);
        if(itemVO.getAttachList() != null){
            itemVO.getAttachList().forEach(attach -> log.info(attach.toString()));
        }

        if(itemVO.getAttachList() == null){
            log.info("마켓 컨트롤러 AttachListNUll");
        }

        marketService.register(itemVO);
        rttr.addFlashAttribute("itemnum", itemVO.getItemnum());
        return new RedirectView("/market/marketList");

    }


    /*상세보기*/
    @GetMapping({"marketView"})
    public void marketView(@RequestParam("itemnum") Long itemnum, Criteria criteria, Model model, HttpServletRequest r) {
        String id = (String)r.getSession().getAttribute("sessionId");
        log.info("들어옴");
        log.info("id : " + id);
        model.addAttribute("id",id);
        model.addAttribute("item", marketService.get(itemnum));
        model.addAttribute("criteria",criteria);

    }


    /*삭제*/
    @GetMapping("remove")
    public RedirectView remove(@RequestParam("itemnum") Long itemnum, RedirectAttributes rttr) {
        log.info("-------------------------------");
        log.info("remove + " + itemnum);
        log.info("-------------------------------");

        if (marketService.remove(itemnum)) {
            rttr.addFlashAttribute("result", "success");
        } else {
            rttr.addFlashAttribute("result", "fail");
        }
        return new RedirectView("/market/marketList");
    }

    private void deleteFiles(List<MarketThumbVO> attachList) {
        if (attachList == null || attachList.size() == 0) {
            return;
        }

        log.info("delete attach files...........");
        log.info(attachList.toString());

        attachList.forEach(attach -> {
            try {
                Path file = Paths.get("C:/upload/" + attach.getUploadPath() + "/" + attach.getUuid() + "_" + attach.getFileName());
                Files.delete(file);

                if (Files.probeContentType(file).startsWith("image")) {
                    Path thumbnail = Paths.get("C:/upload/" + attach.getUploadPath() + "/s_" + attach.getUuid() + "_" + attach.getFileName());
                    Files.delete(thumbnail);
                }
            } catch (Exception e) {
                log.error("delete file error " + e.getMessage());
            }
        });

    }

    @GetMapping(value = "getAttachList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<MarketThumbVO> getAttachList(Long itemnum){
        log.info("getAttachList " + itemnum);
        return marketService.getAttachList(itemnum);
    }


    @GetMapping("marketKart")
    public String marketKart() {
        return "/market/marketKart";
    }

    @GetMapping("marketMyorder")
    public String marketMyorder(Criteria criteria, Model model, HttpServletRequest r) {
        String id = (String) r.getSession().getAttribute("sessionId");
        criteria.setBuyerId(id);
        model.addAttribute("list", marketService.orderMyList(criteria));
        model.addAttribute("pageMaker", new PageDTO(marketService.orderGetTotal(criteria), 10, criteria));
        return "/market/marketMyorder";
    }

    @GetMapping("marketOrderList")
    public String marketOrderList(Criteria criteria, Model model, HttpServletRequest r){
        String id = (String) r.getSession().getAttribute("sessionId");
        log.info("-----------------------------------------------");
        log.info(id);
        log.info("-----------------------------------------------");
        criteria.setUserId(id);
        model.addAttribute("list", marketService.orderGetList(criteria));
        model.addAttribute("pageMaker", new PageDTO(marketService.orderGetTotal(criteria), 10, criteria));
        return "/market/marketOrderList";
    }

    @GetMapping("marketPayment")
    public String marketPayment(@RequestParam("count") String count, ItemVO itemVO, Model model, HttpServletRequest r) {
        String id = (String) r.getSession().getAttribute("sessionId");
        UserVO user = userService.getUser(id);

        model.addAttribute("user", user);
        model.addAttribute("count", count);
        model.addAttribute("order", itemVO);
        return "/market/marketPayment";
    }

    @GetMapping("marketPayDone")
    public String marketPayDone() {
        return "/market/marketPayDone";
    }

    @PostMapping("marketPayment")
    public String marketPayment(OrderVO orderVO){
        marketService.orderRegister(orderVO);
        return "/market/marketPayDone";
    }

    @GetMapping("marketStatus")
    public RedirectView status(@RequestParam("orderNum") Long orderNum){
        marketService.status(orderNum);
        return new RedirectView("marketMyorder");
    }
}