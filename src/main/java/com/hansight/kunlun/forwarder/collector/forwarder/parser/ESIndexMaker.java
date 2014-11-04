package com.hansight.kunlun.forwarder.collector.forwarder.parser;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.hansight.kunlun.forwarder.collector.utils.Pair;

/**
 * Author:zhhui
 * DateTime:2014/8/13 9:15.
 */
public class ESIndexMaker {
    private final static String TIMESTAMP = "@timestamp";
    private final static String DATE = "date";
    private final static String TIME = "time";
    public static final ThreadLocal<SimpleDateFormat> FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        }
    }; //
    private static final ThreadLocal<Grok> grok = new ThreadLocal<Grok>() {
        @Override
        protected Grok initialValue() {
            return Grok.getInstance("%{DATESTAMP}");
        }
    };
    public Pair<String, Date> get(String timestamp) throws ParseException{
        //first check we grok pattern get an datatime as index.
        Match match = grok.get().match(timestamp);
        match.captures(true);
        Map<String, Object> map = match.toMap();

        if (map.size() == 0) {
        //second it may format with long or double
            String suffix;
            try {
                Long time = Long.parseLong(timestamp);
                Date date = new Date(time);
                suffix = FORMAT.get().format(date);
            } catch (NumberFormatException ne) {
                try {
                    Double time = Double.parseDouble(timestamp);
                    time = time * 1000;
                    Date date = new Date(time.longValue());
                    suffix = FORMAT.get().format(date);
                } catch (NumberFormatException dne) {
                    // other we can detect it ,use new date
                    suffix = FORMAT.get().format(new Date());

                }

            }
            // other we can detect it ,use new date
            return new Pair<>(suffix, FORMAT.get().parse(suffix));
        } else {
            return indexSuffix(map);
        }


    }
    /**
     * making a yyyyMMdd format String ，when have error use new Date();
     *
     * @param log Map<String, Object>
     * @return Pair @see   com.hansight.kunlun.utils.Pair
     */
    public Pair<String, Date> indexSuffix(Map<String, Object> log) throws ParseException {
        String suffix;

        Object timestamp = log.get(TIMESTAMP);
        //has timestamp define
        if (timestamp != null) {
            return get(timestamp.toString());
        } else {
            //has data time define
            Object date = log.get(DATE);
            Object time = log.get(TIME);
            if (date != null && time != null && !"".equals(date) && !"".equals(time)) {
                return get(date.toString()+" "+time.toString());
            }
        try {
            // else  we guess it use grok wo define
            suffix = makeIndexSuffix(log);
        } catch (ParseException e) {
            //   // other we can detect it ,use new date
             suffix = FORMAT.get().format(new Date());
        }

        }


        return new Pair<>(suffix, FORMAT.get().parse(suffix));
    }

    private static String makeIndexSuffix(Map<String, Object> log) throws ParseException {
        String result = "";
        String time;
        Object year = log.remove("named_year");
        Object month = log.remove("named_month");
        Object month_num = log.remove("named_month_num");
        Object month_num2 = log.remove("named_month_num2");
        Object month_day = log.remove("named_month_day");
        Object hour = log.remove("named_hour");
        Object minute = log.remove("named_minute");
        Object second = log.remove("named_second");
        if (year != null)
            result += year;
        else throw new ParseException("year you set @timestamp is null", 1);
        result += month(month, month_num, month_num2);
        if (month_day != null)
            result += (month_day.toString().length() > 1 ? "" : "0") + month_day;
        else throw new ParseException("month_day you set @timestamp is null", 1);
        hour = hour == null ? "00" : hour;
        second = second == null ? "00" : second;
        minute = minute == null ? "00" : minute;
        time = result + " " + hour + ":" + minute + ":" + second;
        return time;
    }

    private static String month(Object month, Object month_num, Object month_num2) throws ParseException {
        if (month_num == null && month_num2 == null && month == null)
            throw new ParseException("month you set @timestamp is null", 1);
        String m = null;
        if (month == null && month_num != null && month_num2 == null) {
            m = month_num.toString().trim();
            if (m.length() == 1) {
                m = "0" + m;
            }

        }
        if (month == null && month_num == null) {
            m = month_num2.toString().trim();
            if (m.length() == 1) {
                m = "0" + m;
            }
        }
        if (month != null && month_num == null && month_num2 == null) {
            m = month.toString().trim().toLowerCase();
            switch (m.substring(0, 3)) {
                case "jan": {
                    m = "01";
                    break;
                }
                case "feb": {
                    m = "02";
                    break;
                }
                case "mar": {
                    m = "03";
                    break;
                }
                case "apr": {
                    m = "04";
                    break;
                }
                case "may": {
                    m = "05";
                    break;
                }
                case "jun": {
                    m = "06";
                    break;
                }
                case "jul": {
                    m = "07";
                    break;
                }
                case "aug": {
                    m = "08";
                    break;
                }
                case "sep": {
                    m = "09";
                    break;
                }
                case "oct": {
                    m = "10";
                    break;
                }
                case "nov": {
                    m = "11";
                    break;
                }
                case "dec": {
                    m = "12";
                    break;
                }
            }
        }
        return m;
    }

    public static void main(String[] args) throws ParseException {

   /*  SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy年MM月dd日 DDD HH时mm分ss秒");
        System.out.println("FORMAT.toPattern(); = " + FORMAT.toPattern());
      Date date=  FORMAT.parse("2013年02月28日 星期四 21时19分28秒");
        System.out.println("date = " + date);*/
        Map<String, Object> log = new HashMap<>();
        System.out.println("new Date().getTime() = " + new Date());
        System.out.println("new Date().getTime() = " + new Date().getTime());
        log.put("@timestamp", "1410225299.774338");
        System.out.println("log = " + new ESIndexMaker().indexSuffix(log));
        System.out.println("log = " + log.get("@timestamp"));
    }
}
