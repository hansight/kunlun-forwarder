package com.hansight.kunlun.forwarder.collector.forwarder.parser;

import org.junit.Test;

import com.hansight.kunlun.forwarder.collector.common.base.Lexer;
import com.hansight.kunlun.forwarder.collector.common.model.Event;
import com.hansight.kunlun.forwarder.collector.forwarder.lexer.DelimiterLogLexer;
import com.hansight.kunlun.forwarder.collector.utils.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Author:zhhui
 * DateTime:2014/7/25 18:00.
 */
public class LogParserTest {/*
    @Test
    public void testIISLogLexer() throws Exception {
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer("%{IIS_LOG}");
        Event event = new Event();
        Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
        event.setHeader(header);
        event.setBody(ByteBuffer.wrap("2013-12-09 15:59:59 W3SVC1 PBSZ-A 10.0.10.10 GET /CmbBank_PB/UI/Base/doc/Images/MessageNew9.gif - 443 - 121.205.10.90 HTTP/1.1 Mozilla/4.0+(compatible;+MSIE+8.0;+Windows+NT+5.1;+Trident/4.0;+SV1;+.NET+CLR+2.0.50727;+.NET+CLR+3.0.4506.2152;+.NET+CLR+3.5.30729) UniProc1378975827=133866040880362500;+cmbuser_ID=222.77.22.235-1083435632.30286633::4A9D62BFDCA5376CE14AF38E93DEE4B3;+WTFPC=id=222.77.22.235-1083435632.30286633%3A%3A4A9D62BFDCA5376CE14AF38E93DE:lv=1386604141828:ss=1386604141828;+AuthType=A https://pbsz.ebank.cmbchina.com/CmbBank_PB/UI/PBPC/DebitCard_AccountManager/am_QueryHistoryTrans.aspx pbsz.ebank.cmbchina.com 200 0 0 1351 695 0".getBytes()));
        Queue<Event> events = new ConcurrentLinkedDeque<>();
        events.add(event);
        lexer.setValueToParse(events);

        Map<String, Object> log = lexer.call().poll();
        System.out.println("log.toJSONString() = " + log);
        ESDao<Map<String, Object>> writer = new DefaultESDao();
        writer.setIndex("logs_test");
        writer.setType("log_iis");
        writer.save(log);


    }

    @Test
    public void testCSVLogLexer() throws Exception {
        Path path = Paths.get("F:/workspace/logger/data/csv/AdHoc_2014_04_01.csv");
        assert path != null;
        File file = path.toFile();
        assert file.exists() && file.isFile() && file.canRead();
        Lexer<Event, Map<String, Object>> lexer = new DelimiterLogLexer("#,@timestamp,generated_date,product_entity,product,product_ip,product_mac,management_server,malware,endpoint,s_host,user_name,handling,results,detections,entry_type,details".split(","), "\\t");
        //      BufferedReader reader = new BufferedReader(new FileReader(file));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "unicode"));
       *//* CsvReader csvReader = new CsvReader(new FileInputStream(file), '\t', Charset.forName("unicode"));
        if(csvReader.readHeaders()){
            String[] header = csvReader.getHeaders();
            System.out.println("header = " + header);
        }


        while (csvReader.readRecord()){
            String record = csvReader.getRawRecord();
            System.out.println("record = " + record);
        }
*/

    @Test
    public void testCSVLogLexer() throws Exception {
        Path path = Paths.get("F:\\data\\csv\\cmb-sniffer.csv");
        assert path != null;
        File file = path.toFile();
        assert file.exists() && file.isFile() && file.canRead();
        Lexer<Event, Map<String, Object>> lexer = new DelimiterLogLexer("segment,@timestamp,date,time,c_ip,s_ip,c_port,s_port,time_taken,cs_method,sc_status,rsp_text,cs_bytes,cs_host,cs_uri_stem,url,request_via,pragma,transfer_encoding,cs_useragent,cs_referer,accept,accept_charset,accept_encoding,accept_language,expect,from,server,sc_bytes,load_time,keep_alive".split(","), "\t");
        //      BufferedReader reader = new BufferedReader(new FileReader(file));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line = reader.readLine();
        int i = 0;
        while (line != null) {
            System.out.println("line = " + line);
            Event event = new Event();
            Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
            event.setHeader(header);
            event.setBody(ByteBuffer.wrap(line.getBytes()));
            Queue<Event> events = new ConcurrentLinkedDeque<>();
            events.add(event);
            lexer.setValueToParse(events);

            Map<String, Object> log = lexer.parse(event);
            // ESDao<Map<String, Object>> writer = new DefaultESDao();
            Pair<String, Date> pair= new ESIndexMaker().indexSuffix(log);
            System.out.println("pair = " + pair);

           /* writer.setIndex("logs_"
                    + ESIndexMaker.indexSuffix(log));
            writer.setType("log_apache");
            writer.save(log);*/
            System.out.println(i++ + "log.toJSONString() = " + log);
            line = reader.readLine();
        }
    }

    @Test
    public void testSeparator() {
        String log = "#\t已接收\t已生成\t产品实体/端点\t产品\t产品/端点 IP\t产品/端点 MAC\t管理服务器实体\t病毒/恶意软件\t端点\t来源主机\t用户\t处理措施\t结果\t检测数\t 入口类型\t详细信息";
        String[] raws = log.split("\t");
        for (String raw : raws) {
            System.out.println("raw = " + raw);
        }
    }
/*
    @Test
    public void testApacheLogLexer() throws Exception {
        Path path = Paths.get("F:\\workspace\\logger\\data\\apache\\centos1-access_log-20140727");
        assert path != null;
        File reader = path.toFile();
        assert reader.exists() && reader.isFile() && reader.canRead();
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer("%{APACHE}");
        BufferedReader reader = new BufferedReader(new FileReader(reader));
        String line = reader.readLine();
        int i = 0;
        while (line != null) {
            Event event = new Event();
            Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
            event.setHeader(header);
            event.setBody(ByteBuffer.wrap(line.getBytes()));
            Queue<Event> events = new ConcurrentLinkedDeque<>();
            events.add(event);
            lexer.setValueToParse(events);

            Map<String, Object> log = lexer.call().poll();
            *//* ESDao<Map<String, Object>> writer = new DefaultESDao();
            writer.setIndex("logs_"
                    + ParserUtil.indexSuffix(log));
            writer.setType("log_apache");
            writer.save(log);*//*
            System.out.println(i++ + "log.toJSONString() = " + log);
            line = reader.readLine();
        }
    }

    @Test
    public void testRegexLogLexer() throws Exception {
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer("%{COMBINEDAPACHELOG}");
        Event event = new Event();
        Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
        event.setHeader(header);
        event.setBody(ByteBuffer.wrap("112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"".getBytes()));
        Queue<Event> events = new ConcurrentLinkedDeque<>();
        events.add(event);
        lexer.setValueToParse(events);

        Map<String, Object> log = lexer.call().poll();
        System.out.println("log.toJSONString() = " + log);
    }

    @Test
    public void testDefaultParser() throws Exception {
        Path path = Paths.get("F:\\workspace\\logger\\data\\iis\\ex131216.log");
        assert path != null;
        File reader = path.toFile();
        assert reader.exists() && reader.isFile() && reader.canRead();
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer("%{IIS_LOG}");
        BufferedReader reader = new BufferedReader(new FileReader(reader));
        String line = reader.readLine();
        while (line != null) {
            Event event = new Event();
            Map<CharSequence, CharSequence> header = new LinkedHashMap<>();
            event.setHeader(header);
            event.setBody(ByteBuffer.wrap(line.getBytes()));
            Queue<Event> events = new ConcurrentLinkedDeque<>();
            events.add(event);
            lexer.setValueToParse(events);

            Map<String, Object> log = lexer.call().poll();
            if (log == null || log.isEmpty())
                System.out.println("line:" + line);
            line = reader.readLine();
        }

    }


    */
}
