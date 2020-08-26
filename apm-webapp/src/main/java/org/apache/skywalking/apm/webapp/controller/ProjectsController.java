package org.apache.skywalking.apm.webapp.controller;

import org.apache.skywalking.apm.webapp.compont.SSOConfiguration;
import org.apache.skywalking.apm.webapp.service.SSOservice;
import org.apache.skywalking.apm.webapp.vo.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author Liu-XinYuan
 */
@RestController
@RequestMapping("/user")
public class ProjectsController {
    private Logger logger = LoggerFactory.getLogger(ProjectsController.class);

    @Autowired
    SSOservice ssOservice;

    @Autowired
    SSOConfiguration ssoConfiguration;

    @GetMapping(value = "projects")
    public R getProjects(HttpServletRequest request, HttpServletResponse response) {
        List<String> projects;
        Object userId;
        try {
            HttpSession session = request.getSession();
            // session.getId()
            userId = session.getAttribute("userId");
            if (userId == null) {
                session.invalidate();
                response.setHeader("url", ssoConfiguration.getSsologin());
                response.setHeader("invalid", "true");
                return new R(504, "session is invalid", null);
            }
            projects = ssOservice.getProjects(userId.toString());
        } catch (Exception e) {
            logger.error("", e);
            return new R(500, e.getMessage(), null);
        }
        return new R(200, "ok", projects);
    }
}