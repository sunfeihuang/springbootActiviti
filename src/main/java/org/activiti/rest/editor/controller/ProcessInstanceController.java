package org.activiti.rest.editor.controller;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 流程实例列表
 */
@Controller
@RequestMapping("/processInstance")
public class ProcessInstanceController {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;


    @RequestMapping("/list")
    public String list(ModelMap modelMap, String key, String deploymentId, String userId, String name) {

        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

        if(StringUtils.isNotEmpty(key)){
            processInstanceQuery.processDefinitionKey(key);
        }
        if(StringUtils.isNotEmpty(deploymentId)){
            processInstanceQuery.deploymentId(deploymentId);
        }
        if(StringUtils.isNotEmpty(userId)){
            processInstanceQuery.startedBy(userId);
        }
        if(StringUtils.isNotEmpty(name)){
            processInstanceQuery.processInstanceNameLike(name);
        }
        processInstanceQuery.orderByProcessInstanceId().desc();
        List<ProcessInstance> list = processInstanceQuery.list();

        modelMap.addAttribute("list",list);
        modelMap.addAttribute("key",key);
        modelMap.addAttribute("deploymentId",deploymentId);
        modelMap.addAttribute("userId",userId);
        modelMap.addAttribute("name",name);
        return "html/processInstanceList";
    }

    @RequestMapping("/delete/{id}")
    public @ResponseBody
    String deleteModel(@PathVariable("id")String id){
        runtimeService.deleteProcessInstance(id,"删除原因");
        return "删除成功！";
    }

}
