package com.rulex.bmw.service;

import com.rulex.bmw.pojo.DataBean;

import java.io.IOException;

public interface BSBService {

    void producer(DataBean.Data data);

    Integer customer() throws IOException;

}
