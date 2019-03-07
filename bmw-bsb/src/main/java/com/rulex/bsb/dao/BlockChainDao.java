package com.rulex.bsb.dao;


import com.google.gson.Gson;
import com.rulex.bsb.pojo.*;
import com.rulex.bsb.utils.DBUtils;
import com.rulex.bsb.utils.HttpHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChainDao {

    private static final String BYTOM_URL = "http://127.0.0.1:9888/";
    private static final String BYTOM_ASSET_ID = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
    private static final String BYTOM_ACCOUNT_ID = "0MVDL3J7G0A02";


    /**
     * Simulate write block chain
     *
     * @param key
     * @param payload
     * @return
     */
    public static Integer putStatus(byte[] key, byte[] payload) {
        String sql = "insert into bmw_chain (key_hash,payload)values (?,?);";
        Object[] objects = {key, payload};
        return DBUtils.edit(sql, objects);
    }


    /**
     * Perform data linking
     *
     * @param hexString
     * @return
     */
    public static String postData(String hexString) {

        String submit = null;
        try {
            String build = build(hexString);

            String sign = sign(build);

            submit = submit(sign);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return getSubmitData(submit);
    }


    /**
     * build transaction
     *
     * @param arbitrary data
     * @return
     * @throws Exception
     */
    public static String build(String arbitrary) throws Exception {
        Gson gson = new Gson();

        Map<String, Object> params = new HashMap<>();
        List<Map<String, Object>> actions = new ArrayList<>();

        Map<String, Object> spend1 = new HashMap<>();
        spend1.put("account_id", BYTOM_ACCOUNT_ID);
        spend1.put("asset_id", BYTOM_ASSET_ID);
        spend1.put("type", "spend_account");
        spend1.put("amount", 10000000);

        Map<String, Object> spend2 = new HashMap<>();
        spend2.put("account_id", BYTOM_ACCOUNT_ID);
        spend2.put("asset_id", BYTOM_ASSET_ID);
        spend2.put("type", "spend_account");
        spend2.put("amount", 20000000);

        Map<String, Object> retire = new HashMap<>();
        retire.put("account_id", BYTOM_ACCOUNT_ID);
        retire.put("asset_id", BYTOM_ASSET_ID);
        retire.put("type", "retire");
        retire.put("amount", 100);
        retire.put("arbitrary", arbitrary);

        actions.add(spend1);
        actions.add(spend2);
        actions.add(retire);

        params.put("actions", actions);
        params.put("base_transaction", null);
        params.put("ttl", 0);
        params.put("time_range", System.currentTimeMillis());

        return HttpHelper.post(null, gson.toJson(params), BYTOM_URL + "build-transaction");
    }


    /**
     * sign transaction
     *
     * @param build
     * @return
     */
    public static String sign(String build) throws Exception {
        Gson gson = new Gson();

        Template template = getdata(build);

        Transaction transaction = new Transaction();
        transaction.setPassword("Jjl@2018q3");
        transaction.setTransaction(template);

        return HttpHelper.post(null, gson.toJson(transaction), BYTOM_URL + "sign-transaction");
    }


    /**
     * submit transaction
     *
     * @param sign
     * @return
     */
    public static String submit(String sign) throws Exception {
        Gson gson = new Gson();

        Template getdata = getSignData(sign);
        String raw_transaction = getdata.getRaw_transaction();
        Map<String, String> params = new HashMap<>();
        params.put("raw_transaction", raw_transaction);

        return HttpHelper.post(null, gson.toJson(params), BYTOM_URL + "submit-transaction");
    }


    /**
     * get data from buildresponse
     *
     * @param response
     * @return
     */
    public static Template getdata(String response) {
        return new Gson().fromJson(response, BuildReturn.class).getData();
    }

    /**
     * get data from signresponse
     *
     * @param response
     * @return
     */
    public static Template getSignData(String response) {
        return new Gson().fromJson(response, SignReturn.class).getData().getTransaction();
    }

    /**
     * get data from submitresponse
     *
     * @param response
     * @return
     */
    public static String getSubmitData(String response) {
        return new Gson().fromJson(response, SubmitReturn.class).getData().getTx_id();
    }

}
