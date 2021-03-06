package com.koreait.yougn.beans.dao;

import com.koreait.yougn.beans.vo.Criteria;
import com.koreait.yougn.beans.vo.ExpoVO;
import com.koreait.yougn.beans.vo.ItemVO;
import com.koreait.yougn.beans.vo.OrderVO;
import com.koreait.yougn.mappers.MarketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.thymeleaf.standard.expression.OrExpression;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MarketDAO {

    private final MarketMapper marketMapper;

    public void register(ItemVO itemVO){
        marketMapper.insertSelectKey_itemnum(itemVO);
    }

    public ItemVO get(Long itemnum){
        return marketMapper.read(itemnum);
    }

    public boolean modify(ItemVO itemVO){
        return marketMapper.update(itemVO) == 1;
    }

    public boolean remove(Long itemnum){
        return marketMapper.delete(itemnum) == 1;
    }

    public List<ItemVO> getList(Criteria criteria){
        criteria.setAmount(6);
        return marketMapper.getList(criteria);
    }

    public int getTotal(Criteria criteria){ return marketMapper.getTotal(criteria); }


    // 오더
    public void orderRegister(OrderVO orderVO){
        marketMapper.orderInsert(orderVO);
    }

    public void orderCountupdate(OrderVO orderVO) {
        marketMapper.orderCountupdate(orderVO);
    }

    public List<OrderVO> orderGetList(Criteria criteria){
        return marketMapper.orderGetList(criteria);
    }
    public List<OrderVO> orderMyList(Criteria criteria){
        return marketMapper.orderMyList(criteria);
    }
    public int orderGetTotal(Criteria criteria){ return marketMapper.orderGetTotal(criteria); }

    public boolean status(Long orderNum){return marketMapper.status(orderNum) == 1;}
}
