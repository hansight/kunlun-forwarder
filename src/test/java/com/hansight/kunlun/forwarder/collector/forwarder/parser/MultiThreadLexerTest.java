package com.hansight.kunlun.forwarder.collector.forwarder.parser;

import org.junit.Test;

import com.hansight.kunlun.forwarder.collector.common.dao.ElasticSearchCacheDao;
import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.forwarder.lexer.RegexLogLexer;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author: zhhui
 * Date: 2014/9/26
 */
public class MultiThreadLexerTest {

    private Queue<Event> make() {
        Queue<Event> queue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 100; i++) {
            Event event = new Event();
            event.setBody(ByteBuffer.wrap("2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_PB/UI/Base/doc/Images/MessageNew9.gif - 443 - 121.205.10.90 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+8.0;+Windows+NT+5.1;+Trident/4.0;+SV1;+.NET+CLR+2.0.50727;+.NET+CLR+3.0.4506.2152;+.NET+CLR+3.5.30729) UniProc1378975827=133866040880362500;+cmbuser_ID=222.77.22.235-1083435632.30286633::4A9D62BFDCA5376CE14AF38E93DEE4B3;+WTFPC=id=222.77.22.235-1083435632.30286633%3A%3A4A9D62BFDCA5376CE14AF38E93DE:lv=1386604141828:ss=1386604141828;+AuthType=A https://pbsz.ebank.cmbchina.com/CmbBank_PB/UI/PBPC/DebitCard_AccountManager/am_QueryHistoryTrans.aspx pbsz.ebank.cmbchina.com 200 0 0 1351 695 0".getBytes()));
            queue.add(event);

        }
        return queue;
    }

    @Test
    public void test() throws InterruptedException {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {

            RegexLogLexer lexer = new RegexLogLexer("%{IIS_LOG}");
            lexer.setMetric(null);
            lexer.setValueToParse(make());
            lexer.setWriter(new ElasticSearchCacheDao<>(100, "log_iis"));
            threadPool.submit(lexer);
        }
        threadPool.awaitTermination(1, TimeUnit.MINUTES);

    }
}
