/*
 * Copyright (c) 2016-2088, fastquery.org and/or its affiliates. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For more information, please see http://www.fastquery.org/.
 * 
 */

package org.fastquery.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fastquery.core.RepositoryException;
import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
class PropertiesUtil {
	
	private PropertiesUtil() {
	}
	
	/**
	 * 配置转换
	 * 
	 * @param fqueryjson fquery.json 输入流
	 * @param fqueryResource fquery资源
	 * @return fastquery.json set结构
	 */
	static Set<FastQueryJson> getFQueryProperties(Resource fqueryResource) {
				
		JSONObject json = getFQJSON(fqueryResource);
		FastQueryJSONObject.setJsonObject(json);
		FastQueryJSONObject.check();
		FastQueryJson[] fqProperties = JSON.toJavaObject(json.getJSONArray("scope"), FastQueryJson[].class);
		String config = null;
		String dataSourceName = null;
		Set<String> basePackages = null;
		List<String> dataSourceNames = new ArrayList<>(); // 用于存储所有的数据源名称,在fastquery.json文件里禁止dataSourceName重复出现
		List<String> bpNames = new ArrayList<>(); // 用于存储所有的basePackage,在fastquery.json文件里禁止basePackage重复出现
		
		Set<FastQueryJson> fqs = new HashSet<>();
		for (FastQueryJson fQueryPropertie : fqProperties) {

			// 顺便校验配置
			config = fQueryPropertie.getConfig();
			dataSourceName = fQueryPropertie.getDataSourceName();
			basePackages = fQueryPropertie.getBasePackages();
			if (config == null || "".equals(config)) {
				throw new RepositoryException("fastquery.json 中的config属性配置错误,提示,不能是空字符且不能为null");
			}
			if ("".equals(dataSourceName)) {
				throw new RepositoryException("fastquery.json 中的dataSourceName配置错误,提示,不能是空字符且不能为null");
			}

			for (String basePackage : basePackages) {
				if (basePackage == null || "".equals(basePackage)) {
					continue;
				}
				bpNames.add(basePackage); // 把所有的basePackage收集在一个集合里,方便校验是否有重复
			}

			switch (config) {
			case "c3p0":
				// 校验是否存在 c3p0-config.xml 文件
				if (!fqueryResource.exist("c3p0-config.xml")) {
					throw new RepositoryException("fastquery.json 配置文件中, config设置了c3p0,因此依赖c3p0-config.xml配置文件,可是没有找到.");
				}
				
				// 校验指定的数据源名称是否正确
				if(dataSourceName!=null && !XMLParse.exists(fqueryResource, "c3p0-config.xml", dataSourceName,"named-config")) {
					throw new RepositoryException("fastquery.json 配置文件中, 指定了数据源为" + dataSourceName + ",而在c3p0-config.xml中,找不到对该数据源的配置.");
				}
				
				break;
			case "druid":
				break;
			case "jdbc":
				// 校验是否存在 jdbc-config.xml 文件
				if (!fqueryResource.exist("jdbc-config.xml")) {
					throw new RepositoryException("fastquery.json 配置文件中, config设置了jdbc,因此依赖jdbc-config.xml配置文件,可是没有找到.");
				}
				// getJdbcConfigs 里面有对流进行关闭
				if(dataSourceName!=null && !XMLParse.exists(fqueryResource, "jdbc-config.xml", dataSourceName,"named-config")) {
					throw new RepositoryException("fastquery.json 配置文件中, 指定了数据源为" + dataSourceName + ",而在jdbc-config.xml中,找不到对该数据源的配置.");
				}
				// 校验指定的数据源名称是否正确
				break;
			default:
				throw new RepositoryException("fastquery.json 配置文件中, config设置了" + config + ",不支持该属性值");
			}

			// 校验数据源的名称是否配置正确

			// 检验 end

			// 收集数据 用做校验
			if (dataSourceName != null) {
				dataSourceNames.add(dataSourceName);
			}
			// 收集数据 用做校验 End

			fqs.add(fQueryPropertie);
		}

		// 校验 fastquery.json
		check(dataSourceNames, bpNames);
		
		return fqs;
	}

	//校验 fastquery.json
	private static void check(List<String> dataSourceNames, List<String> bpNames) {
		// 1. dataSourceNames 不能重复
		for (int i = 0; i < dataSourceNames.size(); i++) {
			if (Collections.frequency(dataSourceNames, dataSourceNames.get(i)) > 1) {
				throw new RepositoryException("fastquery.json 配置文件中 \"dataSourceName\"=\"" + dataSourceNames.get(i) + "\" 不能重复出现.");
			}
		}
		
		// 2. bpNames 不能重复
		for (int j = 0; j < bpNames.size(); j++) {
			if (Collections.frequency(bpNames, bpNames.get(j)) > 1) {
				throw new RepositoryException("fastquery.json 配置文件中, basePackages中的元素\"" + bpNames.get(j) + "\"不能重复出现.");
			}
		}
		
		// 3. 不能出现诸如: org.fastquery.db(包地址) 又配置了它的子类org.fastquery.db.AA
		bpNames.forEach(b1 -> bpNames.forEach(b2 -> {
			if (b2.startsWith(b1 + '.')) {
				throw new RepositoryException("basePackages配置错误: " + b1 + " 已经包含了" + b2 + " 两者只能选一");
			}
		}));
		
		// x. ...
	}

	private static JSONObject getFQJSON(Resource fqueryResource) {
		
		try(InputStream fqueryjson = fqueryResource.getResourceAsStream("fastquery.json");ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			if (fqueryjson == null) {
				throw new RepositoryException("没有找到fastquery.json .");
			}
			
			int b = 0; // 定义尽量跟使用处保持短距离
			// 虽然这样读效率不高,但是针对小文件很合适.
			while ((b = fqueryjson.read()) != -1) {
				byteArrayOutputStream.write(b);
			}
			
			return JSONObject.parseObject(TypeUtil.filterComments(byteArrayOutputStream.toString()));
			
		} catch (IOException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}
}
