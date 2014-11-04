package com.hansight.kunlun.forwarder.collector.common.dao;

import org.junit.Assert;
import org.junit.Test;

import com.hansight.kunlun.forwarder.collector.common.model.DataSource;
import com.hansight.kunlun.forwarder.collector.common.utils.ForwarderConstants;

/**
 * @author tao_zhang
 */
public class AssetTest {
    @Test
    public void testQuery() {
        AssetDao dao = new AssetDao();
        dao.setIndex(ForwarderConstants.ASSET_INDEX_NAME);
        dao.setType(ForwarderConstants.ASSET_TYPE_NAME);
        DataSource ast = dao.query("jzhozAeHR-Ox6DNqo_ugTQ");
        System.out.println(ast);
        Assert.assertNotNull("must be not null", ast);
    }

    @Test
    public void testSave() {
        AssetDao dao = new AssetDao();
        dao.setIndex(ForwarderConstants.ASSET_INDEX_NAME);
        dao.setType(ForwarderConstants.ASSET_TYPE_NAME);
       /* Asset conf = new Asset();
        conf.setDesc("syslog agent tcp");
        conf.setEncoding("utf-8");
        conf.setParser("regex");
        conf.setPattern("{IPORHOST:source_ip} - {USERNAME:remote_user} [{HTTPDATE:timestamp}] {QS:request} {INT:status} {INT:body_bytes_sent} {QS:http_referer} {QS:http_user_agent}");
        conf.setSrcIp("172.16.219.121");
        conf.setIndex("apache_access_tcp_");
        conf.setType("log");
        conf.setDateField("timestamp");
        dao.save(conf);
        System.out.println(conf.getId());*/
        
        DataSource asset = new DataSource();
        asset.setAgentHost("172.16.219.121");
        asset.setHost("172.16.219.121");
        asset.setParser("delimit");
        asset.setProtocol("file");
        asset.setCategory("default");
        asset.setType("event");
        dao.save(asset);
        System.out.println(asset.getId());
      
    
    }
}
