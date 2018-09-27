package com.rulex.bmw.service;

import com.rulex.bmw.pojo.DataBean;

public interface BSBService {

    void producer(DataBean.Data data);

    void customer();

}
