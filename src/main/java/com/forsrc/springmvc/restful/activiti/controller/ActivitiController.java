package com.forsrc.springmvc.restful.activiti.controller;


import com.forsrc.springmvc.restful.activiti.service.ActivitiService;
import org.activiti.explorer.conf.DemoDataConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Login controller.
 */
@Controller
@RequestMapping(value = "/activiti/v1.0")
public class ActivitiController {

    @Autowired
    @Resource(name = "activitiService")
    private ActivitiService activitiService;

    @Autowired
    private DemoDataConfiguration demoDataConfiguration;

    /**
     * The Message source.
     */
    @Autowired
    @Resource(name = "messageSource")
    protected MessageSource messageSource;

    private static final String VERSION_V_1_0 = "v1.0";


    /**
     * Init and  model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @RequestMapping(value = {"/init"}, method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ModelAndView init(
            HttpServletRequest request,
            HttpServletResponse response) {

        ModelAndView modelAndView = new ModelAndView();
        Map<String, String> message = new HashMap<String, String>();
        demoDataConfiguration.init();
        message.put("success", "true");
        message.put("version", VERSION_V_1_0);
        response.setStatus(200);
        modelAndView.addObject("return", message);
        return modelAndView;
    }


    public ActivitiService getActivitiService() {
        return activitiService;
    }

    public void setActivitiService(ActivitiService activitiService) {
        this.activitiService = activitiService;
    }

    public DemoDataConfiguration getDemoDataConfiguration() {
        return demoDataConfiguration;
    }

    public void setDemoDataConfiguration(DemoDataConfiguration demoDataConfiguration) {
        this.demoDataConfiguration = demoDataConfiguration;
    }

    /**
     * Gets message source.
     *
     * @return the message source
     */
    public MessageSource getMessageSource() {
        return messageSource;
    }

    /**
     * Sets message source.
     *
     * @param messageSource the message source
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}

