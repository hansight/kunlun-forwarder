package com.hansight.kunlun.forwarder.collector.utils;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

import java.io.*;

public class EsUtils {

    private static TransportClient client = null;

    public synchronized static TransportClient getEsClient() {
        if (null == client) {
        }
        String name = Common.get(Common.ES_CLUSTER_NAME);
        String address = Common.get(Common.ES_CLUSTER_HOST);
        //  client = getNewClient(name, address);
        // return client;
        return getNewClient(name, address);
    }

    public static TransportClient getNewClient(String name, String address) {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", name).put("refresh_interval", -1)
                .put("threadpool.bulk.size", 100)
                .put("threadpool.bulk.queue_size", 10000)
                .put("threadpool.bulk.reject_policy", "caller")
                .build();
        client = new TransportClient(settings);
        if (address != null) {
            String[] arr = address.split(",");
            for (String tmp : arr) {
                String[] tmpArr = tmp.split(":");
                String host = tmpArr[0];
                if (host == null || host.length() == 0) {
                    continue;
                }
                String portStr = tmpArr[1];
                if (portStr == null || portStr.length() == 0) {
                    continue;
                }
                try {
                    int port = Integer.parseInt(portStr);
                    client.addTransportAddress(new InetSocketTransportAddress(
                            host, port));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return client;
    }

    public synchronized static void closeEsClient(TransportClient client) {
        if (null != client) {
            client.close();
        }
    }

    public static void exportData(String index, String type, String file)
            throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        TransportClient client = getEsClient();
        SearchResponse scrollResp = client.prepareSearch(index).setTypes(type)
                // 加上这个据说可以提高性能，但第一次却不返回结果
                .setSearchType(SearchType.SCAN)
                        // 实际返回的数量为5*index的主分片格式
                .setSize(10)
                        // 这个游标维持多长时间
                .setScroll(TimeValue.timeValueMinutes(8)).execute().actionGet();

        // Scroll until no hits are returned
        while (true) {
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(600000)).execute().actionGet();
            for (SearchHit hit : scrollResp.getHits()) {
                out.write(hit.getId() + hit.getSourceAsString() + "\n");
            }
            // Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        IOUtils.close(out);
    }

    public static void importData(String index, String type, String file)
            throws IOException {
        //192.168.57.10
        //TransportClient client = getNewClient("esdev", "127.0.0.1:9300");
        TransportClient client = getEsClient();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line, id, json;
        int idx, batch = 500, total = 0;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        IndexRequestBuilder indexRequest = null;
        BulkResponse bulkResponse = null;
        while ((line = in.readLine()) != null) {
            idx = line.indexOf("{");
            if (idx < 0) {
                continue;
            }
            id = line.substring(0, idx);
            json = line.substring(idx);
            System.out.println(line);
            System.out.println(id);
            System.out.println(json);
            indexRequest = client.prepareIndex(index, type).setSource(json)
                    .setId(id);
            bulkRequest.add(indexRequest);
            total++;
            if (total == batch) {
                bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    System.out.println(bulkResponse.buildFailureMessage());
                }
                bulkRequest = client.prepareBulk();
                total = 0;
            }
        }
        if (total != batch) {
            bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                System.out.println(bulkResponse.buildFailureMessage());
            }
        }
        IOUtils.close(in);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Usage: operate index-name type-name file-name");
            System.out.println("operate: export | import");
            return;
        }
//        exportData("logs_20131209", "log_IIS", "F:\\workspace\\kunlun\\kunlun-logger\\data\\data.log");
        String operate = args[0];
        String index = args[1];
        String type = args[2];
        String file = args[3];
        if ("export".equals(operate)) {
            exportData(index, type, file);
        } else {
            importData(index, type, file);
        }
    }
}
