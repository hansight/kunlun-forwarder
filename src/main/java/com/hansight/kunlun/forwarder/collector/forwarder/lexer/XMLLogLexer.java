package com.hansight.kunlun.forwarder.collector.forwarder.lexer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.kunlun.forwarder.collector.common.exception.CollectorException;
import com.hansight.kunlun.forwarder.collector.common.model.Event;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author:zhhui
 * DateTime:2014/7/23 15:27.
 * 默认按照String 解析一行日志到，json
 */
public class XMLLogLexer extends DefaultLexer<Event, Map<String, Object>> {
    protected final static Logger logger = LoggerFactory.getLogger(XMLLogLexer.class);
    private String encoding;

    public XMLLogLexer() {
        this(null);
    }

    public XMLLogLexer(String encoding) {
        this.encoding = encoding;
        //    this.threadPool = threadPool;
    }

    @Override
    public Map<String, Object> parse(Event event) throws CollectorException {
        String value = new String(event.getBody().array());
        value = value.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
        /**
         * todo Fixed have good idea for this ,may bo noting?
         */
        value = value.replaceAll("(<([^/]*?)>[^<&>]*?)[<&>]{1}([^<&>]*?</\\2>)", "$1$3");
        Map<String, Object> log;
        try {
            log = parseXML(new ByteArrayInputStream(value.trim().getBytes()));
            JSONObject system = (JSONObject) log.get("system");
            JSONObject timecreated = system.getJSONObject("timecreated");
            String systemtime = timecreated.getString("systemtime");
            if (systemtime != null) {
                log.put("@timestamp", systemtime);
            }
            //TODO fixed filter fields
        } catch (DocumentException e) {
            log = new LinkedHashMap<>();
            log.put("data", value);
            log.put("error", "xml parse error please check");
        }
        return log;
      /*  read(null);*/
    }

    public JSONObject parseXML(InputStream in) throws DocumentException {
        SAXReader reader = new SAXReader();
        reader.setEncoding(encoding);
        return (JSONObject) parse(reader.read(in).getRootElement());
    }

    @SuppressWarnings("unchecked")
    private Object parse(Element root) {
        JSONObject current = new JSONObject();
        List<Attribute> attributes = root.attributes();
        List<Element> elements = root.elements();
        if ((attributes == null || attributes.isEmpty()) && (elements == null || elements.isEmpty())) {
            String value = root.getTextTrim();
            if (value != null && !"".equals(value))
                return value;
            return null;
        }
        if (attributes != null && !attributes.isEmpty()) {
            for (Attribute attribute : attributes) {
                current.put(attribute.getName().toLowerCase(), attribute.getValue().trim());
            }
        }

        if (elements == null || elements.isEmpty()) {
            String value = root.getTextTrim();
            if (value != null && !"".equals(value))
                current.put("value", value);
        } else {
            Map<String, Integer> names = new HashMap<>();
            for (Element element : elements) {
                String name = element.getName().toLowerCase();
                Integer value = names.get(name);
                if (value == null) {
                    value = 0;
                }
                names.put(name, ++value);
            }
            for (Element element : elements) {
                String name = element.getName().toLowerCase();


                if (names.get(name) > 1) {
                    JSONArray array = (JSONArray) current.get(name);
                    if (array == null) {
                        array = new JSONArray();
                    }
                    Object value = parse(element);
                    if (value != null) {
                        array.add(parse(element));
                        current.put(name, array);
                    }


                } else {
                    Object value = parse(element);
                    if (value != null) {
                        current.put(name, parse(element));
                    }
                }


            }

        }
        return current;

    }


    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public static void main(String[] args) throws DocumentException {
        XMLLogLexer logLexer = new XMLLogLexer();
        String event1 = "<Event xmlns='http://schemas.microsoft.com/win/2004/08/events/event'><System><Provider Name='MSExchange CmdletLogs'/><EventID Qualifiers='49152'>6</EventID><Level>2</Level><Task>1</Task><Keywords>0x80000000000000</Keywords><TimeCreated SystemTime='2012-02-22T10:41:37.000000000Z'/><EventRecordID>735</EventRecordID><Channel>MSExchange Management</Channel><Computer>scex01.chinasupercloud.com</Computer><Security/></System><EventData><Data>Add-DatabaseAvailabilityGroupServer</Data><Data>{MailboxServer=SCEX02, Identity=DAG1}</Data><Data>chinasupercloud.com/Users/Administrator</Data><Data>S-1-5-21-3107969293-2071314508-2835089583-500</Data><Data>S-1-5-21-3107969293-2071314508-2835089583-500</Data><Data>ServerRemoteHost-EMC</Data><Data>2084</Data><Data></Data><Data>38</Data><Data>00:00:02.2964781</Data><Data>�t*�:'True', Mnߧ6h:SCDC02.chinasupercloud.com, � h@U:scdc01.chinasupercloud.com, � ߧ6h:{ scdc01.chinasupercloud.com }</Data>\n" +
                "<Data>Microsoft.Exchange.Data.DataValidationException: �{:�^'Л< ^' �: DnsHostName ( Microsoft.Exchange.Data.Directory.ADSession.ObjectsFromEntries(SearchResultEntryCollection entries, String originatingServerName, IEnumerable`1 properties, ADRawEntry dummyInstance, CreateObjectDelegate objectCtor, CreateObjectsDelegate arrayCtor) ( Microsoft.Exchange.Data.Directory.ADSession.Find(ADObjectId rootId, String optionalBaseDN, ADObjectId readId, QueryScope scope, QueryFilter filter, SortBy sortBy, Int32 maxResults, IEnumerable`1 properties, CreateObjectDelegate objectCreator, CreateObjectsDelegate arrayCreator, Boolean includeDeletedObjects) ( Microsoft.Exchange.Data.Directory.ADSession.Find(ADObjectId rootId, QueryScope scope, QueryFilter filter, SortBy sortBy, Int32 maxResults, IEnumerable`1 properties, CreateObjectDelegate objectCtor, CreateObjectsDelegate arrayCtor) ( Microsoft.Exchange.Data.Directory.ADSession.Find[TResult](ADObjectId rootId, QueryScope scope, QueryFilter filter, SortBy sortBy, Int32 maxResults, IEnumerable`1 properties) ( Microsoft.Exchange.Data.Directory.SystemConfiguration.ADSystemConfigurationSession.Find[TResult](ADObjectId rootId, QueryScope scope, QueryFilter filter, SortBy sortBy, Int32 maxResults) ( Microsoft.Exchange.Data.Directory.SystemConfiguration.ADSystemConfigurationSession.FindComputerByHostName(ADObjectId domainId, String hostName) ( Microsoft.Exchange.Cluster.Replay.DagTaskHelper.DoesComputerAccountExist(ADSystemConfigurationSession configSession, String cnoName, Boolean&amp; accountEnabled) ( Microsoft.Exchange.Management.SystemConfigurationTasks.AddDatabaseAvailabilityGroupServer.CheckThereIsNoClusterNamed(String dagName) ( Microsoft.Exchange.Management.SystemConfigurationTasks.AddDatabaseAvailabilityGroupServer.FindClusterForDag() ( Microsoft.Exchange.Management.SystemConfigurationTasks.AddDatabaseAvailabilityGroupServer.CheckServerDagClusterMembership() ( Microsoft.Exchange.Management.SystemConfigurationTasks.AddDatabaseAvailabilityGroupServer.FetchOrMakeUpSecretClusterName() ( Microsoft.Exchange.Management.SystemConfigurationTasks.AddDatabaseAvailabilityGroupServer.InternalValidate() ( Microsoft.Exchange.Configuration.Tasks.Task.ProcessRecord()</Data>" +
                "<Data>Context</Data><Data></Data></EventData></Event>";
        System.out.println("\"1.1\".matches() = " + "11".matches(""));
        // event1 = event1.replaceAll("\\d\\\\k0", "$1 </");
        event1 = event1.replaceAll("(<([^/]*?)>[^<&>]*?)[<&>]{1}([^<&>]*?</\\2>)", "$1$3");
        //event1 = StringEscapeUtils.unescapeXml(event1);
        System.out.println("event1 = " + event1);
        logLexer.setEncoding("UTF-8");
        Map<String, Object> log = logLexer.parseXML(new ByteArrayInputStream(event1.trim().getBytes()));
        System.out.println("log = " + log);
    }
}
