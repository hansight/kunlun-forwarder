package com.hansight.kunlun.forwarder.collector.common.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hansight.kunlun.forwarder.collector.common.model.DataSource;

import java.util.Map;

/**
 * Author:chao_bai DateTime:2014/7/29 16:43.
 */
public class AssetDao extends ESDao<DataSource> implements BaseDao<String, DataSource> {
	private static final Logger LOG = LoggerFactory.getLogger(AssetDao.class);

	@Override
	protected DataSource toObject(Map<String, Object> map, String id) {
		return toObject(DataSource.class.getDeclaredFields(), new DataSource(), map, id);
	}

	@Override
	Map<String, Object> toMap(DataSource t) {
		return toMap(DataSource.class.getDeclaredFields(), t);
	}

}
