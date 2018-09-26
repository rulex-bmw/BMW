package com.eroc.bmw.service;

import com.eroc.bmw.pojo.DataBean;

public interface BSBService {

    void producer(DataBean.Data data);

    void customer();

}
