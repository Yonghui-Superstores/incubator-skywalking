package org.apache.skywalking.apm.webapp.controller;

import org.apache.skywalking.apm.webapp.Const;
import org.apache.skywalking.apm.webapp.compont.SSOConfiguration;
import org.apache.skywalking.apm.webapp.service.SSOservice;
import org.apache.skywalking.apm.webapp.vo.R;
import org.apache.skywalking.apm.webapp.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author Liu-XinYuan
 */
@RestController
@RequestMapping("/user")
public class ProjectsController {
    private Logger logger = LoggerFactory.getLogger(ProjectsController.class);

    public Set<String> ADMIN = new HashSet<>();

    public ProjectsController() {
        ADMIN.add("80831143");
        ADMIN.add("80996803");
        ADMIN.add("80996860");
    }

    @Autowired
    SSOservice ssOservice;

    @Autowired
    SSOConfiguration ssoConfiguration;

    @GetMapping(value = "projects")
    public R getProjects(HttpServletRequest request, HttpServletResponse response) {
        List<String> projects;
        User user;
        try {
            HttpSession session = request.getSession();
            // session.getId()
            user = (User) session.getAttribute(Const.SESSION_USER);
            if (user == null) {
                session.invalidate();
                response.setHeader("url", ssoConfiguration.getSsologin());
                response.setHeader("invalid", "true");
                return new R(504, "session is invalid", null);
            }
            projects = ssOservice.getProjects(user.getUserId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new R(500, e.getMessage(), null);
        }
        Map<String, List<String>> data = new HashMap<>();
        data.put("projects", projects);
        return new R(200, "ok", data);
    }

    @GetMapping(value = "admin")
    public R isAdmin(HttpServletRequest request, HttpServletResponse response) {
        boolean admin = false;
        try {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute(Const.SESSION_USER);
            if (user == null) {
                session.invalidate();
                response.setHeader("url", ssoConfiguration.getSsologin());
                response.setHeader("invalid", "true");
                return new R(504, "session is invalid", null);
            } else if (ADMIN.contains(user.getUserName())) {
                admin = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new R(500, e.getMessage(), null);
        }
        Map<String, Boolean> data = new HashMap<>();
        data.put("admin", admin);
        return new R(200, "ok", data);
    }
}