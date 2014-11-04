package com.hansight.kunlun.forwarder.collector.common.dao;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.forwarder.collector.utils.EsUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Author:chao_bai DateTime:2014/7/29 16:43.
 *
 * @param <T>
 */
public abstract class ESDao<T> implements BaseDao<String, T> {
    private static final Logger LOG = LoggerFactory.getLogger(ESDao.class);
    protected  TransportClient client;
    protected String index;
    protected String type;
    abstract Map<String, Object> toMap(T t);

    protected ESDao() {
        client = EsUtils.getEsClient();
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean save(T t) {
        //BeanUtils.describe(t);
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.add(client.prepareIndex(index, type)
                .setSource(toMap(t)));
        BulkResponse response = bulkRequest.execute().actionGet();
        if (response.hasFailures()) {
            LOG.debug("es save has error info :{}", response.buildFailureMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean save(List<T> list) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (T t : list) {
            bulkRequest.add(client.prepareIndex(index, type)
                    .setSource(toMap(t)));

        }
        bulkRequest.execute().actionGet();
        BulkResponse response = bulkRequest.execute().actionGet();
        if (response.hasFailures()) {
            LOG.debug("es save has error info :{}", response.buildFailureMessage());
            return false;
        }
        return true;
    }

    public synchronized BulkRequestBuilder makeBulkRequest(String index, T t, BulkRequestBuilder builder) {
        if (builder == null)
            builder = client.prepareBulk();
        return builder.add(client.prepareIndex(index, type)
                .setSource(toMap(t)));

    }

    public boolean execute(BulkRequestBuilder builder) {
        if (builder == null) {
            return false;
        }
        builder.execute().actionGet();
        BulkResponse response = builder.execute().actionGet();
        if (response.hasFailures()) {
            LOG.debug("es save has error info :{}", response.buildFailureMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean delete(String id) {
        DeleteResponse response = client.prepareDelete(index, type, id).execute().actionGet();
        if (!response.isFound()) {
            LOG.debug("es delete not found !");
            return false;
        }
        return true;

    }

    @Override
    public T query(String id) {
        Map<String, Object> source = client.prepareGet(index, type, id)
                .execute().actionGet().getSource();
        return toObject(source, id);
    }

    protected T toObject(Map<String, Object> source, String id) {
        return null;
    }

    @Override
    public List<T> list() {
        List<T> list = new ArrayList<T>();
        SearchResponse sr = client.prepareSearch(index).setTypes(type)
                .setSearchType(SearchType.DEFAULT)
                .setScroll(new TimeValue(60000)).setFrom(0).setSize(100)
                .execute().actionGet();

        for (SearchHit hit : sr.getHits()) {
            Map<String, Object> source = hit.getSource();
            list.add(toObject(source, hit.getId()));
        }
        return list;
    }

    protected T toObject(Field[] fields, T ast, Map<String, Object> map, String id) {
        for (Field field : fields) {
            if (!field.getName().equals("serialVersionUID")) {
                field.setAccessible(true);
                try {
                    if ("id".equals(field.getName())) {
                        field.set(ast, id);
                    } else {
                        String key = toESKey(field.getName());
                        field.set(ast, map.get(key));
                    }
                } catch (IllegalArgumentException e) {
                    LOG.error("", e);
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    LOG.error("", e);
                }
            }
        }
        return ast;
    }

    protected Map<String, Object> toMap(Field[] fields, T t) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Field field : fields) {
            if (!field.getName().equals("serialVersionUID")) {
                field.setAccessible(true);
                try {
                    Object value = field.get(t);
                    if (value != null) {
                        map.put(toESKey(field.getName()), value);
                    }
                } catch (IllegalArgumentException e) {
                    LOG.error("", e);
                } catch (IllegalAccessException e) {
                    LOG.error("", e);
                }
            }
        }
        return map;
    }

    public String toESKey(String name) {
        StringBuffer dst = new StringBuffer();
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 65 && c <= 90) {
                if (i != 0) {
                    dst.append("_");
                }
                dst.append(Character.toLowerCase(c));
            } else {
                dst.append(c);
            }
        }
        return dst.toString();
    }


}
