package com.eroc.bmw.dao;

import com.eroc.bmw.pojo.DataBean;
import com.eroc.bmw.service.OnBAService;

public class OnBADao {


    public void setStatus(DataBean.Data data, OnBAService onBAService) {
        //存入数据
        onBAService.BlockChainBack("存入成功");


    }


}
