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

package org.apache.skywalking.apm.webapp.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apache.skywalking.apm.webapp.sso.OpenPrdFeignClient;
import org.apache.skywalking.apm.webapp.sso.OpenFeignClient;
import org.apache.skywalking.apm.webapp.vo.TokenInfo;
import org.apache.skywalking.apm.webapp.sso.SSOFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Liu-XinYuan
 */
@Service
public class SSOservice {
    @Resource SSOFeignClient ssoFeignClient;
    @Resource OpenPrdFeignClient openPrdFeignClient;
    @Resource OpenFeignClient openFeignClient;

    public String getUserId(String code, String env) {
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encode = "Basic " + encoder.encodeToString(("skywalking" + ":" + "secret").getBytes(StandardCharsets.UTF_8));
        // 根据code获取token信息
        TokenInfo tokenInfo = ssoFeignClient.getToken(code, "http://localhost:8080/sso/callback?env=" + env, "authorization_code", encode).getBody();
        // 根据token拿到user
        Object user = ssoFeignClient.getUser(tokenInfo.getToken_type() + " " + tokenInfo.getAccess_token()).getBody();
        Gson gson = new Gson();
        JsonElement userS = gson.toJsonTree(user);
        String userId = userS.getAsJsonObject().getAsJsonObject("principal").get("userId").getAsString();
        return userId;
    }

    public List<String> getProjects(String userId, String env) {
        Gson gson = new Gson();
        ResponseEntity<Object> responseEntity = null;
        switch (env) {
            case "prd":
                responseEntity = openPrdFeignClient.getProjects(userId);
                break;
            case "test":
                responseEntity = openFeignClient.getKFProjects(userId);
                break;
            case "dev":
                responseEntity = openFeignClient.getDEVProjects(userId);
                break;
            default:
                return null;
        }

        Object body = responseEntity.getBody();
        JsonArray data = gson.toJsonTree(body).getAsJsonObject().getAsJsonArray("data");
        if (data == null) {
            return null;
        }
        List<String> projects = new ArrayList<>();
        for (JsonElement element : data) {
            JsonElement code = element.getAsJsonObject().get("code");
            if (code != null) {
                projects.add(code.getAsString());
            }
        }
        return projects;
    }

}
