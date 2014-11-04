
package com.hansight.kunlun.forwarder.collector.common.dao;

import java.util.List;

/**
 * Author:chao_bai
 * DateTime:2014/7/29 16:44.
 */
public interface BaseDao<ID, T> {
    boolean save(T t);

    boolean save(List<T> list);

    boolean delete(ID id);

    T query(ID id);

    List<T> list();
}
