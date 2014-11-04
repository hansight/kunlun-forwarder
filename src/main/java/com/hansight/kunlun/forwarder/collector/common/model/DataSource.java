package com.hansight.kunlun.forwarder.collector.common.model;

import java.io.Serializable;

/**
 * 注：
 * 1> index固定格式：logs_yyyyMMdd, 每天一个，日期从数据源@timestamp读取
 * 2> 数据源表将存入关系型数据库(MySql)
 */
public class DataSource implements Serializable{
	private static final long serialVersionUID = -8828233238522914458L;
	
	/**
     * Agent中Kafka用的topic为ID
     */
    /**
     * uuid, 作为kafka的topic，Agent及Forworder的配置
     */
    private String id;
    /**
     * 应用日志类型(eg: iis, apache)，添加特殊的类型：other, 匹配DefaultParser;
     * 命名规范，如趋势科技DeepSecurity的防火墙事件：firewall_ds_trend
     */
    private String category;
    /**
     * 普通日志，安全日志(log_*, event_*)；与category组合ES下某个
     * index的type,如：event_firewall_ds_trend
     */
    private String type;
    /**
     * 如IISParser，解析器的别名；DefaultParser为正则解析
     */
    private String parser;
    /**
     * 当parser为正则解析器时，pattern为正则表达式；
     * 当parser为分隔符解析器时，pattern为固定格式 如下 {separate:',',fields:['field1','field2','field3',...]}；
     * 其它时，暂定为空
     */
    private String pattern;
    /**
     * Agent所在的host
     */
    private String agentHost;
    /**
     * 数据源的host
     */
    private String host;
    /**
     * 数据源的端口
     */
    private String port;
    /**
     * snmp-trap, snmp, syslog-tcp, syslog-udp, reader,ftp,nfs协议
     */
    private String protocol;
    /**
     * 路径(/var/log/message, ftp://192.168.1.2:21/var/log/message)
     */
    private String uri;
    /**
     * utf-8/GBK
     */
    private String encoding;

    public DataSource() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }


    public String getAgentHost() {
        return agentHost;
    }

    public void setAgentHost(String agentHost) {
        this.agentHost = agentHost;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSource)) return false;

        DataSource asset = (DataSource) o;

        if (agentHost != null ? !agentHost.equals(asset.agentHost) : asset.agentHost != null) return false;
        if (category != null ? !category.equals(asset.category) : asset.category != null) return false;
        if (encoding != null ? !encoding.equals(asset.encoding) : asset.encoding != null) return false;
        if (host != null ? !host.equals(asset.host) : asset.host != null) return false;
        if (id != null ? !id.equals(asset.id) : asset.id != null) return false;
        if (parser != null ? !parser.equals(asset.parser) : asset.parser != null) return false;
        if (pattern != null ? !pattern.equals(asset.pattern) : asset.pattern != null) return false;
        if (port != null ? !port.equals(asset.port) : asset.port != null) return false;
        if (protocol != null ? !protocol.equals(asset.protocol) : asset.protocol != null) return false;
        if (type != null ? !type.equals(asset.type) : asset.type != null) return false;
        if (uri != null ? !uri.equals(asset.uri) : asset.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (parser != null ? parser.hashCode() : 0);
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        result = 31 * result + (agentHost != null ? agentHost.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }

	@Override
	public String toString() {
		return "DataSource [id=" + id + ", category=" + category + ", type="
				+ type + ", parser=" + parser + ", pattern=" + pattern
				+ ", agentHost=" + agentHost + ", host=" + host + ", port="
				+ port + ", protocol=" + protocol + ", uri=" + uri
				+ ", encoding=" + encoding + "]";
	}
}
