package com.hansight.kunlun.forwarder.collector.forwarder.parser;


import org.junit.Test;

import com.hansight.kunlun.forwarder.collector.forwarder.lexer.CEFLogLexer;

import java.util.Map;

public class CEFLogLexerTest{
    @Test
    public void testLexer(){
        String [] cases={
                "CEF:0|ArcSight|Logger|5.0.0.5355.2|sensor:115|Logger Internal Event|1| cat=/Monitor/Sensor/Fan5 cs2=Current Value cnt=1 dvc=10.0.0.1 cs3=Ok cs1=null type=0 cs1Label=unit rt=1305034099211 cs3Label=Status cn1Label=value  cs2Label=timeframe",
                "CEF:0| TopSec | tos | |  | accept | low | eventId=6571594769 externaId=122250814 msg=null mrt=1414654265131 proto=ICMP customerId=STIWRIUYBABCAGFgwu5XqtA\\=\\= customerURI=/所有客户/长沙分行 modelConfidence=0 severity=0 relevance=10 assetCritiaclity=0 priority=3 art=1414654265032 " +
                        "cat=ac deviceSeverity=accept act=accept rt=1414653885000 src=99.138.248.18 sourceZoneID=MVCHU%fsAABCCXLV-GNArfg\\=\\= sourceZoneURI=/所有区域/ArcSight 系统/公用位址空间区域/ARIN/80.0.0.0-91.255.255.255 " +
                        "smac=00:1e:bd:fc:02:42 spt=152 sourceGeoCountryCode=US slong=97.0 slat=38.0 dst=99.138.143.3 destinationZoneID=MVCHU5fsAABCCXLV-GNArfg\\=\\= destinationGeoCountryZoneURI=/所有区域/ArcSight 系统/公用位址空间区域/ARIN/80.0.0.0-91.255.255.255 dmac=00:00:se:00:01:63 " +
                        "dpt=9 destinationGeoCountryCode=US dlong=97.0 dlat=38.0 cs1=0 cs2=0 cs3=0 cn1=8051 locality=1 cs1Label=parentid cs2Label=dpiid cs3Label=natid cn1Label=policyid ahost=SZ-SC-17.CMBCHINA>COM " +
                        "agt=10.1.18.170 av=7.0.1.6662.0 atz=Asia/Shanghai aid=#DYYYUUKBABCAA-hpb19jog\\"};
        CEFLogLexer lexer= new CEFLogLexer();
        for(String caze:cases){
          Map<String,Object> log=  lexer.parse(caze);
            System.out.println("log = " + log);
            assert log.size()>2;
        }
    }
}