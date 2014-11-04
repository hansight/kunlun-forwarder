package com.hansight.kunlun.forwarder.collector.common.dao;

import java.util.Map;

/**
 * Author:chao_bai
 * DateTime:2014/7/29 16:43.
 */
public class DefaultESDao extends ESDao<Map<String, Object>> {
    @Override
    public Map toMap(Map<String, Object> t) {
        return t;
    }
}