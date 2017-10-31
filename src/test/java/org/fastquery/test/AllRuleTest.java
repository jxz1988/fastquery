/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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

package org.fastquery.test;

import java.lang.reflect.Field;

import org.fastquery.dao.ProductDBServiceTest;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.service.FQuery;
import org.fastquery.service.FQueryResourceImplTest;
import org.fastquery.service.FQueryTest;
import org.fastquery.util.BeanUtilTest;
import org.fastquery.util.FastQueryJSONObject;
import org.fastquery.util.FastQueryJSONObjectTest;
import org.fastquery.util.TypeUtilTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.alibaba.fastjson.JSONObject;

/**
 * 运行所有的测试用例
 * 
 * @author xixifeng (fastquery@126.com)
 */
// 指定运行器
@RunWith(Suite.class)
@SuiteClasses({ FQueryTest.class, StudentDBServiceTest.class, UserInfoDBServiceTest.class, UserInfoDBServiceTest2.class,
		UserInfoDBServiceTest3.class, TypeUtilTest.class, MethodQueryTest.class, QueryByNamedDBExampleTest.class,
		BeanUtilTest.class, PageTest.class, FastQueryJSONObjectTest.class, FQueryResourceImplTest.class,
		SunnyDBServiceTest.class, SaveToIdTest.class, PlaceholderTest.class, DBTest.class, ProductDBServiceTest.class,FQueryPropertiesTest.class  })
public class AllRuleTest {

	@BeforeClass
	public static void beforeClass()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		FQuery.getRepository(UserInfoDBService.class);
		Field jsonObjectField = FastQueryJSONObject.class.getDeclaredField("jsonObject");
		jsonObjectField.setAccessible(true);
		JSONObject jsonObject = (JSONObject) jsonObjectField.get(null);
		jsonObject.put("debug", true);
	}

	@Test
	public void todo() {
	}

}