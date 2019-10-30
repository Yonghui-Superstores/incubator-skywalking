/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.webapp.sso;

import java.util.List;
import org.apache.skywalking.apm.webapp.vo.TokenInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Liu-XinYuan
 */
@FeignClient(name = "sso", url = "http://api.itwork.yonghui.cn")
@Component
public interface SSOFeignClient {

    @RequestMapping(value = "/oauth/oauth/authorize", method = RequestMethod.GET)
    ResponseEntity<String> checkLogin(@RequestParam(name = "response_type") String responseType,
        @RequestParam(name = "client_id") String clientId,
        @RequestParam(name = "redirect_uri") String redirectUri
    );

    @RequestMapping(value = "/oauth/oauth/token", method = RequestMethod.POST)
    ResponseEntity<TokenInfo> getToken(
        @RequestParam(name = "code") String code,
        @RequestParam(name = "redirect_uri") String redirectUri,
        @RequestParam(name = "grant_type") String grantType,
        @RequestHeader(name = "Authorization") String auth);

    @RequestMapping(value = "/oauth/api/user", method = RequestMethod.POST)
    ResponseEntity<Object> getUser(@RequestHeader(value = "Authorization") String token);

    @RequestMapping(value = "/user/projects", method = RequestMethod.GET)
    ResponseEntity<List<String>> getProjects(@RequestParam(name = "userName") String userName);

}
