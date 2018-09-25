package com.eroc.bmw;

import com.eroc.bmw.dao.OnBADao;
import com.eroc.bmw.pojo.DataBean;
import com.eroc.bmw.pojo.ParamBean;
import com.eroc.bmw.service.OnBAService;

public class PayLoad {


    public class setStatus implements OnBAService {
        @Override
        public void BlockChainBack(String msg) {
            System.out.println(msg);
        }
    }

    public void setData(ParamBean.Param param) {
        DataBean.Data build = DataBean.Data.newBuilder().build();
        new OnBADao().setStatus(build, new setStatus());
    }


}
