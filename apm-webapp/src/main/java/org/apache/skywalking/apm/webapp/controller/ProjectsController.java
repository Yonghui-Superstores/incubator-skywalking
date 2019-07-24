package org.apache.skywalking.apm.webapp.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.skywalking.apm.webapp.compont.SSOConfiguration;
import org.apache.skywalking.apm.webapp.service.SSOservice;
import org.apache.skywalking.apm.webapp.vo.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Object env = null;
        Object userId = null;
        try {
            HttpSession session = request.getSession();
            // session.getId()
            userId = session.getAttribute("userId");
            env = session.getAttribute("env");
            if (userId == null || env == null) {
                session.invalidate();
                response.setHeader("url", ssoConfiguration.getSsologin());
                response.setHeader("invalid", "true");
                return new R(504, "session is invalid", null, null);
            }
            projects = ssOservice.getProjects(userId.toString(), env.toString());
        } catch (Exception e) {
            logger.error("", e);
            return new R(500, e.getMessage(), null, null);
        }

        return new R(200, "ok", projects, env.toString());
    }
}
