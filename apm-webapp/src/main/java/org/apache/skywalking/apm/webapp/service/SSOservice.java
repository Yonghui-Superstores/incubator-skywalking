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
import org.apache.skywalking.apm.webapp.compont.SSOConfiguration;
import org.apache.skywalking.apm.webapp.sso.OpenPrdFeignClient;
import org.apache.skywalking.apm.webapp.sso.SSOFeignClient;
import org.apache.skywalking.apm.webapp.vo.TokenInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liu-XinYuan
 */
@Service
public class SSOservice {
    @Resource
    SSOFeignClient ssoFeignClient;
    @Resource
    OpenPrdFeignClient openPrdFeignClient;
    @Resource
    SSOConfiguration ssoConfiguration;

    public String getUserId(String code, String env) {
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
/*        String clientId;
        switch (env) {
            case "prd":
                clientId = "pskywalking";
                break;
            case "uat":
                clientId = "uskywalking";
                break;
            case "test":
                clientId = "tskywalking";
                break;
            case "dev":
                clientId = "dskywalking";
                break;
            default:
                clientId = "lskywalking";
        }*/
        String encode = "Basic " + encoder.encodeToString((ssoConfiguration.getClientId() + ":" + "secret").getBytes(StandardCharsets.UTF_8));
        // 根据code获取token信息
        TokenInfo tokenInfo = ssoFeignClient.getToken(code, ssoConfiguration.getRegisterurl(), "authorization_code", encode).getBody();
        // 根据token拿到user
        Object user = ssoFeignClient.getUser(tokenInfo.getToken_type() + " " + tokenInfo.getAccess_token()).getBody();
        Gson gson = new Gson();
        JsonElement userS = gson.toJsonTree(user);
        String userId = userS.getAsJsonObject().getAsJsonObject("principal").get("userId").getAsString();
        return userId;
    }

    public List<String> getProjects(String userId) {
        Gson gson = new Gson();
        ResponseEntity<Object> responseEntity = openPrdFeignClient.getProjects(userId);
/*        switch (env) {
            case "prd":
                responseEntity = openPrdFeignClient.getProjects(userId);
                break;
            case "uat":
                responseEntity = openFeignClient.getKFProjects(userId);
                break;
            case "test":
                responseEntity = openFeignClient.getKFProjects(userId);
                break;
            case "dev":
                responseEntity = openFeignClient.getDEVProjects(userId);
                break;
            default:
                responseEntity = openFeignClient.getDEVProjects(userId);
    }*/

        Object body = responseEntity.getBody();
        JsonArray data = gson.toJsonTree(body).getAsJsonObject().getAsJsonArray("data");
        if (data == null) {
            return null;
        }

        List<String> projects = new ArrayList<>();
        for (
                JsonElement element : data) {
            JsonElement code = element.getAsJsonObject().get("code");
            if (code != null) {
                projects.add(code.getAsString());
            }
        }
        return projects;
    }

}