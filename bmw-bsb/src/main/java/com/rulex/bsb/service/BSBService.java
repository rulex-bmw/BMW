package com.rulex.bsb.service;

import com.rulex.bsb.pojo.DataBean;

import java.io.IOException;

public interface BSBService {

    void producer(DataBean.Data data);

    Integer customer() throws IOException;

}
